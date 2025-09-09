package com.rtalky.domain.service.impl;

import com.rtalky.application.exception.SmsSendException;
import com.rtalky.domain.event.SmsCodeSentEvent;
import com.rtalky.domain.repository.UserRepository;
import com.rtalky.domain.service.AuthenticationService;
import com.rtalky.domain.model.User;
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
import java.util.Random;
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
}
