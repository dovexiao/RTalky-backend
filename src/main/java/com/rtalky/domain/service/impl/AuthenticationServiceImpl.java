package com.rtalky.domain.service.impl;

import com.rtalky.application.exception.SmsSendException;
import com.rtalky.domain.event.SmsCodeSentEvent;
import com.rtalky.domain.repository.UserRepository;
import com.rtalky.domain.service.AuthenticationService;
import com.rtalky.domain.model.User;
import com.rtalky.domain.model.AuthMethod;
import com.rtalky.infrastructure.cache.CacheService;
import com.rtalky.infrastructure.external.OneClickAuthClient;
import com.rtalky.infrastructure.external.SmsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.NumberParseException;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final SmsClient smsClient;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;
    private final OneClickAuthClient oneClickAuthClient;
    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${sms.code.expire-minutes:5}")
    private int smsCodeExpireMinutes;

    @Value("${sms.code.digits:6}")
    private int smsCodeDigits;

    public AuthenticationServiceImpl(
        SmsClient smsClient,
        CacheService cacheService,
        ApplicationEventPublisher eventPublisher,
        OneClickAuthClient oneClickAuthClient, UserRepository userRepository
    ) {
        this.smsClient = smsClient;
        this.cacheService = cacheService;
        this.eventPublisher = eventPublisher;
        this.oneClickAuthClient = oneClickAuthClient;
        this.userRepository = userRepository;
    }

    @Override
    public boolean verifySmsCode(String phoneNumber, String smsCode, String deviceFingerprint) {
        // 从缓存获取验证码（使用复合键：手机号+设备指纹）
        String cacheKey = "sms:code:" + phoneNumber + ":" + deviceFingerprint;
        String storedCode = cacheService.get(cacheKey, String.class);

        if (storedCode == null) {
            log.warn("短信验证码不存在或已过期: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            return false;
        }

        boolean isValid = storedCode.equals(smsCode);
        if (isValid) {
            // 验证成功后删除验证码
            cacheService.delete(cacheKey);
            log.info("短信验证码验证成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
        } else {
            log.warn("短信验证码验证失败: phoneNumber={}, deviceFingerprint={}, 输入验证码={}, 正确验证码={}",
                    phoneNumber, deviceFingerprint, smsCode, storedCode);
        }

        return isValid;
    }

    @Override
    public boolean verifyOneClickToken(String phoneNumber, String token, String deviceFingerprint) {
        try {
            // 调用一键登录客户端验证令牌
            OneClickAuthClient.AuthResult result = oneClickAuthClient.verifyToken(token);

            if (!result.success()) {
                log.warn("一键登录令牌验证失败: phoneNumber={}, error={}",
                        phoneNumber, result.errorMessage());
                return false;
            }

            // 验证手机号是否匹配
            if (!phoneNumber.equals(result.phoneNumber())) {
                log.warn("一键登录手机号不匹配: 请求手机号={}, 令牌手机号={}",
                        phoneNumber, result.phoneNumber());
                return false;
            }

            log.info("一键登录令牌验证成功: phoneNumber={}", phoneNumber);
            return true;
        } catch (Exception e) {
            log.error("一键登录令牌验证异常: phoneNumber={}, error={}",
                    phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifyPassword(String phoneNumber, String password) {
        try {
            // 1. 验证手机号格式
            if (!isValidPhoneNumber(phoneNumber)) {
                log.warn("密码验证失败: 手机号格式无效 - {}", phoneNumber);
                return false;
            }

            // 2. 验证密码格式（至少6位）
            if (password == null || password.length() < 6) {
                log.warn("密码验证失败: 密码长度不足 - phoneNumber={}", phoneNumber);
                return false;
            }

            // 3. 从数据库查询用户信息
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElse(null);

            if (user == null) {
                log.warn("密码验证失败: 用户不存在 - phoneNumber={}", phoneNumber);
                return false;
            }

            // 4. 检查用户是否设置了密码
            if (!user.hasPassword()) {
                log.warn("密码验证失败: 用户未设置密码 - userId={}, phoneNumber={}",
                        user.getUserId(), phoneNumber);
                return false;
            }

            // 5. 验证密码（实际项目中应使用加密比较）
            boolean passwordMatches = verifyPasswordHash(password, user.getPassword());

            if (passwordMatches) {
                log.info("密码验证成功: userId={}, phoneNumber={}", user.getUserId(), phoneNumber);
            } else {
                log.warn("密码验证失败: 密码不匹配 - userId={}, phoneNumber={}",
                        user.getUserId(), phoneNumber);
            }

            return passwordMatches;
        } catch (Exception e) {
            log.error("密码验证异常: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean userExists(String phoneNumber) {
        // TODO: 实现用户存在性检查逻辑（需要访问数据库）
        // 临时实现
        return false;
    }

    @Override
    public boolean sendSmsCode(String phoneNumber, String deviceFingerprint) {
        try {
            // 1. 验证手机号格式
            if (!isValidPhoneNumber(phoneNumber)) {
                log.warn("手机号格式无效: {}", phoneNumber);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER,
                    "手机号格式无效，请检查手机号是否正确");
            }

            // 2. 检查是否已有未过期的验证码（防止重复发送）
            String cacheKey = "sms:code:" + phoneNumber + ":" + deviceFingerprint;
            if (cacheService.hasKey(cacheKey)) {
                log.warn("手机号+设备指纹组合已有未过期的验证码: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.SMS_ALREADY_SENT,
                    "验证码已发送，请稍后再试");
            }

            // 3. 生成随机验证码
            String smsCode = generateRandomCode();

            // 4. 存储验证码到Redis（使用配置的过期时间）
            try {
                cacheService.set(cacheKey, smsCode, smsCodeExpireMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("存储验证码到缓存失败: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.CACHE_ERROR,
                    "缓存服务异常，请稍后重试", e);
            }

            // 5. 调用短信客户端发送验证码
            boolean sendResult = smsClient.sendVerificationCode(phoneNumber, smsCode);

            if (sendResult) {
                log.info("短信验证码发送成功: phoneNumber={}, code={}, deviceFingerprint={}", phoneNumber, smsCode, deviceFingerprint);

                // 6. 发布短信发送事件（用于统计和审计）
                SmsCodeSentEvent event = new SmsCodeSentEvent(phoneNumber, LocalDateTime.now(), deviceFingerprint);
                eventPublisher.publishEvent(event);

                return true;
            } else {
                // 发送失败时删除已存储的验证码
                try {
                    cacheService.delete(cacheKey);
                } catch (Exception e) {
                    log.warn("删除失败的验证码缓存时出错: phoneNumber={}, error={}", phoneNumber, e.getMessage());
                }
                log.error("短信验证码发送失败: phoneNumber={}", phoneNumber);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.SMS_SEND_FAILED,
                    "短信发送失败，请稍后重试");
            }
        } catch (SmsSendException e) {
            // 重新抛出短信发送异常
            throw e;
        } catch (Exception e) {
            log.error("发送短信验证码异常: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            throw new SmsSendException(SmsSendException.SmsSendErrorCode.SMS_SERVICE_ERROR,
                "短信服务异常，请稍后重试", e);
        }
    }

    /**
     * 生成随机数字验证码
     * 验证码位数从配置文件读取
     */
    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < smsCodeDigits; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 验证手机号格式
     * 使用Google libphonenumber库支持国际标准E.164格式
     * 支持所有国际标准格式：+86 138 0013 8000, +8613800138000, 13800138000等
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String trimmedPhone = phoneNumber.trim();

        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

            // 直接使用库方法解析和验证，支持所有国际标准格式
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(trimmedPhone, null);
            boolean isValid = phoneUtil.isValidNumber(parsedNumber);

            if (isValid) {
                log.debug("手机号验证通过: {} -> 国家代码: {}, 手机号: {}",
                    trimmedPhone, parsedNumber.getCountryCode(), parsedNumber.getNationalNumber());
            } else {
                log.debug("手机号验证失败: {}", trimmedPhone);
            }

            return isValid;

        } catch (NumberParseException e) {
            log.debug("手机号解析失败: {}, 错误: {}", trimmedPhone, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("手机号验证异常: {}, error: {}", trimmedPhone, e.getMessage());
            return false;
        }
    }

    /**
     * 验证密码哈希
     * 实际使用BCrypt安全哈希算法
     */
    private boolean verifyPasswordHash(String inputPassword, String storedHash) {
        return passwordEncoder.matches(inputPassword, storedHash);
    }

    @Override
    public boolean sendPasswordSmsCode(String phoneNumber, String deviceFingerprint) {
        try {
            // 1. 验证手机号格式
            if (!isValidPhoneNumber(phoneNumber)) {
                log.warn("手机号格式无效: {}", phoneNumber);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.INVALID_PHONE_NUMBER,
                    "手机号格式无效，请检查手机号是否正确");
            }

            // 2. 检查是否已有未过期的密码设置验证码（防止重复发送）
            String cacheKey = "sms:code:password:" + phoneNumber + ":" + deviceFingerprint;
            if (cacheService.hasKey(cacheKey)) {
                log.warn("手机号+设备指纹组合已有未过期的密码设置验证码: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.SMS_ALREADY_SENT,
                    "验证码已发送，请稍后再试");
            }

            // 3. 生成随机验证码
            String smsCode = generateRandomCode();

            // 4. 存储验证码到Redis（使用配置的过期时间）
            try {
                cacheService.set(cacheKey, smsCode, smsCodeExpireMinutes, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("存储密码设置验证码到缓存失败: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.CACHE_ERROR,
                    "缓存服务异常，请稍后重试", e);
            }

            // 5. 调用短信客户端发送验证码
            boolean sendResult = smsClient.sendVerificationCode(phoneNumber, smsCode);

            if (sendResult) {
                log.info("密码设置短信验证码发送成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);

                // 6. 发布短信发送事件
                eventPublisher.publishEvent(new SmsCodeSentEvent(
                    phoneNumber, LocalDateTime.now(), deviceFingerprint));

                return true;
            } else {
                log.error("密码设置短信验证码发送失败: phoneNumber={}", phoneNumber);
                return false;
            }

        } catch (SmsSendException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("发送密码设置短信验证码异常: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            throw new SmsSendException(SmsSendException.SmsSendErrorCode.UNKNOWN_ERROR,
                "发送验证码时发生异常，请稍后重试", e);
        }
    }

    @Override
    public boolean verifyPasswordSmsCode(String phoneNumber, String smsCode, String deviceFingerprint) {
        // 从缓存获取密码设置验证码（使用复合键：手机号+设备指纹）
        String cacheKey = "sms:code:password:" + phoneNumber + ":" + deviceFingerprint;
        String storedCode = cacheService.get(cacheKey, String.class);

        if (storedCode == null) {
            log.warn("密码设置短信验证码不存在或已过期: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            return false;
        }

        boolean isValid = storedCode.equals(smsCode);
        if (isValid) {
            // 验证成功后删除验证码
            cacheService.delete(cacheKey);
            log.info("密码设置短信验证码验证成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
        } else {
            log.warn("密码设置短信验证码验证失败: phoneNumber={}, deviceFingerprint={}, 输入验证码={}, 正确验证码={}",
                    phoneNumber, deviceFingerprint, smsCode, storedCode);
        }

        return isValid;
    }

    @Override
    public boolean setPassword(String phoneNumber, String newPassword) {
        try {
            // 1. 验证手机号格式
            if (!isValidPhoneNumber(phoneNumber)) {
                log.warn("密码设置失败: 手机号格式无效 - {}", phoneNumber);
                return false;
            }

            // 2. 从数据库查询用户信息
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElse(null);

            if (user == null) {
                log.warn("密码设置失败: 用户不存在 - phoneNumber={}", phoneNumber);
                return false;
            }

            // 3. 检查用户状态
            if (!user.isAccountActive()) {
                log.warn("密码设置失败: 用户账号不可用 - userId={}, phoneNumber={}", user.getUserId(), phoneNumber);
                return false;
            }

            // 4. 设置密码（newPassword已经是哈希后的密码）
            user.changePassword(newPassword);
            user.addAuthMethod(AuthMethod.PASSWORD); // 添加密码认证方式

            // 5. 保存用户信息
            userRepository.save(user);

            log.info("密码设置成功: userId={}, phoneNumber={}", user.getUserId(), phoneNumber);
            return true;

        } catch (Exception e) {
            log.error("密码设置异常: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }


    @Override
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        try {
            // 1. 验证用户对象
            if (user == null) {
                log.warn("密码修改失败: 用户对象为空");
                return false;
            }

            // 2. 检查用户是否设置了密码
            if (!user.hasPassword()) {
                log.warn("密码修改失败: 用户未设置密码 - userId={}",
                        user.getUserId());
                return false;
            }

            // 3. 验证旧密码
            boolean oldPasswordValid = verifyPasswordHash(oldPassword, user.getPassword());
            if (!oldPasswordValid) {
                log.warn("密码修改失败: 旧密码不正确 - userId={}",
                        user.getUserId());
                return false;
            }

            // 4. 设置新密码（哈希存储）
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.changePassword(hashedPassword);

            // 5. 保存用户信息
            userRepository.save(user);

            log.info("密码修改成功: userId={}", user.getUserId());
            return true;

        } catch (Exception e) {
            log.error("密码修改异常: userId={}, error={}", user.getUserId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cachePasswordForSetup(String phoneNumber, String deviceFingerprint, String newPassword) {
        try {
            // 使用复合键：手机号+设备指纹
            String cacheKey = "password:setup:" + phoneNumber + ":" + deviceFingerprint;

            // 缓存密码，设置过期时间为验证码过期时间+5分钟，避免边界情况
            int passwordExpireMinutes = smsCodeExpireMinutes + 5;
            cacheService.set(cacheKey, newPassword, passwordExpireMinutes, TimeUnit.MINUTES);

            log.info("密码设置新密码缓存成功: phoneNumber={}, deviceFingerprint={}, expireMinutes={}",
                phoneNumber, deviceFingerprint, passwordExpireMinutes);
            return true;

        } catch (Exception e) {
            log.error("密码设置新密码缓存失败: phoneNumber={}, deviceFingerprint={}, error={}",
                phoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getCachedPasswordForSetup(String phoneNumber, String deviceFingerprint) {
        try {
            String cacheKey = "password:setup:" + phoneNumber + ":" + deviceFingerprint;
            String cachedPassword = cacheService.get(cacheKey, String.class);

            if (cachedPassword != null) {
                log.debug("获取密码设置新密码缓存成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            } else {
                log.warn("密码设置新密码缓存不存在或已过期: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            }

            return cachedPassword;

        } catch (Exception e) {
            log.error("获取密码设置新密码缓存失败: phoneNumber={}, deviceFingerprint={}, error={}",
                phoneNumber, deviceFingerprint, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean clearCachedPasswordForSetup(String phoneNumber, String deviceFingerprint) {
        try {
            String cacheKey = "password:setup:" + phoneNumber + ":" + deviceFingerprint;
            cacheService.delete(cacheKey);

            log.info("清除密码设置新密码缓存成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            return true;

        } catch (Exception e) {
            log.error("清除密码设置新密码缓存失败: phoneNumber={}, deviceFingerprint={}, error={}",
                phoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean sendDeletionSmsCode(String phoneNumber, String deviceFingerprint) {
        try {
            // 生成验证码
            String smsCode = generateRandomCode();
            
            // 缓存验证码
            String cacheKey = "sms:code:deletion:" + phoneNumber + ":" + deviceFingerprint;
            cacheService.set(cacheKey, smsCode, smsCodeExpireMinutes, TimeUnit.MINUTES);
            
            // 发送短信
            boolean result = smsClient.sendVerificationCode(phoneNumber, smsCode);
            
            if (result) {
                // 发布事件
                eventPublisher.publishEvent(new SmsCodeSentEvent(phoneNumber, LocalDateTime.now(), deviceFingerprint));
                log.info("账号注销短信验证码发送成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            } else {
                log.error("账号注销短信验证码发送失败: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("账号注销短信验证码发送异常: phoneNumber={}, deviceFingerprint={}, error={}", 
                phoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifyDeletionSmsCode(String phoneNumber, String smsCode, String deviceFingerprint) {
        try {
            String cacheKey = "sms:code:deletion:" + phoneNumber + ":" + deviceFingerprint;
            String cachedCode = cacheService.get(cacheKey, String.class);
            
            if (cachedCode == null) {
                log.warn("账号注销短信验证码不存在或已过期: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
                return false;
            }
            
            if (!cachedCode.equals(smsCode)) {
                log.warn("账号注销短信验证码错误: phoneNumber={}, deviceFingerprint={}, expected={}, actual={}", 
                    phoneNumber, deviceFingerprint, cachedCode, smsCode);
                return false;
            }
            
            // 验证成功后清除验证码
            cacheService.delete(cacheKey);
            log.info("账号注销短信验证码验证成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
            return true;
            
        } catch (Exception e) {
            log.error("账号注销短信验证码验证异常: phoneNumber={}, deviceFingerprint={}, error={}", 
                phoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendPhoneChangeSmsCode(String userId, String newPhoneNumber, String deviceFingerprint) {
        try {
            String smsCode = generateRandomCode();
            String cacheKey = "sms:code:phone_change:" + newPhoneNumber + ":" + deviceFingerprint;
            cacheService.set(cacheKey, smsCode, smsCodeExpireMinutes, TimeUnit.MINUTES);
            
            boolean result = smsClient.sendVerificationCode(newPhoneNumber, smsCode);
            if (result) {
                eventPublisher.publishEvent(new SmsCodeSentEvent(newPhoneNumber, LocalDateTime.now(), deviceFingerprint));
                log.info("换绑手机号短信验证码发送成功: userId={}, newPhoneNumber={}, deviceFingerprint={}", 
                    userId, newPhoneNumber, deviceFingerprint);
            } else {
                log.error("换绑手机号短信验证码发送失败: userId={}, newPhoneNumber={}, deviceFingerprint={}", 
                    userId, newPhoneNumber, deviceFingerprint);
            }
            return result;
        } catch (Exception e) {
            log.error("换绑手机号短信验证码发送异常: userId={}, newPhoneNumber={}, deviceFingerprint={}, error={}", 
                userId, newPhoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifyPhoneChangeSmsCode(String newPhoneNumber, String smsCode, String deviceFingerprint) {
        try {
            String cacheKey = "sms:code:phone_change:" + newPhoneNumber + ":" + deviceFingerprint;
            String cachedCode = cacheService.get(cacheKey, String.class);
            
            if (cachedCode == null) {
                log.warn("换绑手机号短信验证码不存在或已过期: newPhoneNumber={}, deviceFingerprint={}", 
                    newPhoneNumber, deviceFingerprint);
                return false;
            }
            
            if (!cachedCode.equals(smsCode)) {
                log.warn("换绑手机号短信验证码错误: newPhoneNumber={}, deviceFingerprint={}, expected={}, actual={}", 
                    newPhoneNumber, deviceFingerprint, cachedCode, smsCode);
                return false;
            }
            
            // 验证成功后清除验证码
            cacheService.delete(cacheKey);
            log.info("换绑手机号短信验证码验证成功: newPhoneNumber={}, deviceFingerprint={}", 
                newPhoneNumber, deviceFingerprint);
            return true;
            
        } catch (Exception e) {
            log.error("换绑手机号短信验证码验证异常: newPhoneNumber={}, deviceFingerprint={}, error={}", 
                newPhoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cacheNewPhoneForChange(String userId, String newPhoneNumber, String deviceFingerprint) {
        try {
            String cacheKey = "phone_change:new_phone:" + userId + ":" + deviceFingerprint;
            // 缓存时间为验证码过期时间 + 5分钟
            int cacheMinutes = smsCodeExpireMinutes + 5;
            cacheService.set(cacheKey, newPhoneNumber, cacheMinutes, TimeUnit.MINUTES);
            log.info("换绑手机号缓存成功: userId={}, newPhoneNumber={}, deviceFingerprint={}, expireMinutes={}", 
                userId, newPhoneNumber, deviceFingerprint, cacheMinutes);
            return true;
        } catch (Exception e) {
            log.error("换绑手机号缓存异常: userId={}, newPhoneNumber={}, deviceFingerprint={}, error={}", 
                userId, newPhoneNumber, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getCachedNewPhoneForChange(String userId, String deviceFingerprint) {
        try {
            String cacheKey = "phone_change:new_phone:" + userId + ":" + deviceFingerprint;
            String cachedPhoneNumber = cacheService.get(cacheKey, String.class);
            if (cachedPhoneNumber != null) {
                log.info("获取换绑手机号缓存成功: userId={}, deviceFingerprint={}, newPhoneNumber={}", 
                    userId, deviceFingerprint, cachedPhoneNumber);
            } else {
                log.warn("换绑手机号缓存不存在或已过期: userId={}, deviceFingerprint={}", userId, deviceFingerprint);
            }
            return cachedPhoneNumber;
        } catch (Exception e) {
            log.error("获取换绑手机号缓存异常: userId={}, deviceFingerprint={}, error={}", 
                userId, deviceFingerprint, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean clearCachedNewPhoneForChange(String userId, String deviceFingerprint) {
        try {
            String cacheKey = "phone_change:new_phone:" + userId + ":" + deviceFingerprint;
            cacheService.delete(cacheKey);
            log.info("清除换绑手机号缓存成功: userId={}, deviceFingerprint={}", userId, deviceFingerprint);
            return true;
        } catch (Exception e) {
            log.error("清除换绑手机号缓存异常: userId={}, deviceFingerprint={}, error={}", 
                userId, deviceFingerprint, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void clearUserSessions(String userId) {
        try {
            String userSessionsKey = "user_sessions:" + userId;
            Set<Object> userSessions = cacheService.setMembers(userSessionsKey);
            
            if (userSessions != null && !userSessions.isEmpty()) {
                // 删除所有会话
                for (Object sessionToken : userSessions) {
                    cacheService.delete("session:" + sessionToken.toString());
                }
                // 清空用户会话集合
                cacheService.delete(userSessionsKey);
                log.info("清理用户会话成功: userId={}, sessionCount={}", userId, userSessions.size());
            } else {
                log.debug("用户没有活跃会话: userId={}", userId);
            }
        } catch (Exception e) {
            log.warn("清理用户会话时发生异常: userId={}", userId, e);
        }
    }

    @Override
    public void addUserSession(String userId, String sessionToken) {
        try {
            String userSessionsKey = "user_sessions:" + userId;
            cacheService.setAdd(userSessionsKey, sessionToken);
            // 设置过期时间（与会话过期时间一致）
            cacheService.expire(userSessionsKey, 24, TimeUnit.HOURS);
            log.debug("添加用户会话成功: userId={}, sessionToken={}", userId, sessionToken);
        } catch (Exception e) {
            log.warn("添加用户会话时发生异常: userId={}, sessionToken={}", userId, sessionToken, e);
        }
    }

    @Override
    public void removeUserSession(String userId, String sessionToken) {
        try {
            String userSessionsKey = "user_sessions:" + userId;
            cacheService.setRemove(userSessionsKey, sessionToken);
            log.debug("移除用户会话成功: userId={}, sessionToken={}", userId, sessionToken);
        } catch (Exception e) {
            log.warn("移除用户会话时发生异常: userId={}, sessionToken={}", userId, sessionToken, e);
        }
    }

    @Override
    public Set<String> getUserSessions(String userId) {
        try {
            String userSessionsKey = "user_sessions:" + userId;
            Set<Object> sessions = cacheService.setMembers(userSessionsKey);
            Set<String> result = new HashSet<>();
            if (sessions != null) {
                for (Object session : sessions) {
                    result.add(session.toString());
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("获取用户会话时发生异常: userId={}", userId, e);
            return new HashSet<>();
        }
    }
}
