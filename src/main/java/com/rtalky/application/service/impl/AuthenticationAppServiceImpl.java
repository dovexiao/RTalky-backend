package com.rtalky.application.service.impl;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.LoginResponse;
import com.rtalky.application.dto.SmsSendRequest;
import com.rtalky.application.dto.UserInfoResponse;
import com.rtalky.application.dto.UpdateUserInfoRequest;
import com.rtalky.application.dto.PasswordSmsSendRequest;
import com.rtalky.application.dto.PasswordSetRequest;
import com.rtalky.application.dto.PasswordChangeRequest;
import com.rtalky.application.dto.PasswordResponse;
import com.rtalky.application.dto.AccountDeletionSmsRequest;
import com.rtalky.application.dto.AccountDeletionRequest;
import com.rtalky.application.dto.AccountDeletionResponse;
import com.rtalky.application.dto.SmsSendResponse;
import com.rtalky.application.util.PasswordValidator;
import com.rtalky.application.exception.SmsSendException;
import com.rtalky.application.exception.SmsLoginException;
import com.rtalky.application.exception.PasswordLoginException;
import com.rtalky.application.exception.SessionValidationException;
import com.rtalky.application.service.AuthenticationAppService;
import com.rtalky.domain.event.UserLoggedInEvent;
import com.rtalky.domain.event.UserRegisteredEvent;
import com.rtalky.domain.event.UserDeletedEvent;
import com.rtalky.domain.model.AuthMethod;
import com.rtalky.domain.model.UserStatus;
import com.rtalky.domain.model.LoginRecord;
import com.rtalky.domain.model.User;
import com.rtalky.domain.repository.LoginRecordRepository;
import com.rtalky.domain.repository.UserRepository;
import com.rtalky.domain.service.AuthenticationService;
import com.rtalky.infrastructure.cache.CacheService;
import com.rtalky.infrastructure.device.DeviceFingerprintService;
import com.rtalky.infrastructure.device.DeviceInfoParser;
import com.rtalky.interfaces.converter.ResponseBuilder;
import com.rtalky.application.service.login.LoginHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationAppServiceImpl implements AuthenticationAppService {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final LoginRecordRepository loginRecordRepository;
    private final DeviceFingerprintService deviceFingerprintService;
    private final DeviceInfoParser deviceInfoParser;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;
    private final java.util.List<LoginHandler> loginHandlers;
    private final ResponseBuilder responseBuilder;

    @Value("${app.session.expire-hours:24}")
    private int sessionExpireHours;

    @Override
    @Transactional
    public boolean sendSmsCode(SmsSendRequest request) {
        try {
            // 验证协议同意状态
            if (!request.getAgreedToTerms()) {
                    throw new SmsSendException(SmsSendException.SmsSendErrorCode.AGREEMENT_NOT_ACCEPTED,
                        "必须同意用户协议和隐私政策");
                }

                // 验证手机号
                if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                    throw new SmsSendException(SmsSendException.SmsSendErrorCode.PHONE_NUMBER_REQUIRED,
                        "手机号不能为空");
            }

            // 生成纯设备指纹用于限流
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(standardizeDeviceInfo(request.getDeviceInfo()));

            // 检查设备和手机号限制
            if (deviceFingerprintService.isDeviceLimited(deviceFingerprint, request.getPhoneNumber(), "SEND_SMS")) {
                    throw new SmsSendException(SmsSendException.SmsSendErrorCode.DEVICE_LIMITED,
                        "设备或手机号操作受限，请稍后再试");
            }

            // 发送短信验证码
            boolean sendResult = authenticationService.sendSmsCode(request.getPhoneNumber(), deviceFingerprint);

            if (sendResult) {
                // 记录设备操作
                deviceFingerprintService.recordDeviceOperation(deviceFingerprint, request.getPhoneNumber(), "SEND_SMS");
                log.info("短信验证码发送成功: 手机号={}", request.getPhoneNumber());
            } else {
                // 发送失败，抛出异常
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.SMS_SEND_FAILED,
                    "短信验证码发送失败，请稍后重试");
            }

            return sendResult;

        } catch (SmsSendException e) {
            // 重新抛出短信发送异常
            throw e;
        } catch (Exception e) {
            // 其他异常包装为短信发送异常
            log.error("短信发送过程中发生未知异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            throw new SmsSendException(SmsSendException.SmsSendErrorCode.UNKNOWN_ERROR,
                "短信发送过程中发生异常，请稍后重试", e);
        }
    }

    @Override
    @Transactional
    public LoginResponse smsLogin(LoginRequest request) {
        String deviceFingerprint = null;
        String deviceInfo = null;
        try {
            // 验证认证方式
            if (request.getAuthMethod() != AuthMethod.SMS) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.AUTH_METHOD_MISMATCH,
                    "认证方式不匹配，当前请求不是短信登录");
            }

            // 验证协议同意状态
            if (!request.getAgreedToTerms()) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.AGREEMENT_NOT_ACCEPTED,
                    "必须同意用户协议和隐私政策");
            }

            // 验证手机号
            if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.PHONE_NUMBER_REQUIRED,
                    "手机号不能为空");
            }

            // 验证短信验证码
            if (request.getSmsCode() == null || request.getSmsCode().trim().isEmpty()) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.SMS_CODE_REQUIRED,
                    "短信验证码不能为空");
            }

            // 生成纯设备指纹用于限流
            deviceInfo = standardizeDeviceInfo(request.getDeviceInfo());
            deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(deviceInfo);

            // 检查设备限制
            if (deviceFingerprintService.isDeviceLimited(deviceFingerprint, request.getPhoneNumber(), "LOGIN_ATTEMPT")) {
                throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.DEVICE_LIMITED,
                    "设备或手机号操作受限，请稍后再试");
            }

            // 验证短信验证码（失败也会在finally中计数）
            boolean isValid = authenticationService.verifySmsCode(
                request.getPhoneNumber(), request.getSmsCode(), deviceFingerprint);

            if (!isValid) {
                log.warn("短信验证码验证失败: 手机号={}", request.getPhoneNumber());
                    throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.SMS_CODE_INVALID,
                        "短信验证码错误或已过期");
            }

            // 路由到对应登录处理器
            LoginHandler handler = resolveLoginHandler(AuthMethod.SMS);
            User user = handler.handle(request.getPhoneNumber(), deviceFingerprint, request.getAgreedToTerms(), deviceInfo);

            // 更新最后登录时间并保存
            user.updateLastLoginTime();
            userRepository.save(user);

            // 生成会话令牌
            String sessionToken = generateSessionToken(user);

            // 发布事件
            publishLoginEvent(user, AuthMethod.SMS, deviceFingerprint, false);

            // 成功登录记录
            createLoginRecord(user, AuthMethod.SMS, deviceFingerprint, true, null, deviceInfo);

            // 构建响应
            return responseBuilder.buildLoginResponse(user, sessionToken, sessionExpireHours);
        } catch (SmsLoginException e) {
            // 失败登录记录
            tryRecordFailureLogin(AuthMethod.SMS, request.getPhoneNumber(), deviceFingerprint, e.getMessage(), deviceInfo);
            // 重新抛出短信登录异常
            throw e;
        } catch (Exception e) {
            // 其他异常包装为短信登录异常
            log.error("短信登录过程中发生未知异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            tryRecordFailureLogin(AuthMethod.SMS, request.getPhoneNumber(), deviceFingerprint, e.getMessage(), deviceInfo);
            throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.UNKNOWN_ERROR,
                "短信登录过程中发生异常，请稍后重试", e);
        } finally {
            // 无论成功失败，尝试计数一次
            if (deviceFingerprint != null && request.getPhoneNumber() != null) {
                deviceFingerprintService.recordDeviceOperation(deviceFingerprint, request.getPhoneNumber(), "LOGIN_ATTEMPT");
            }
        }
    }

    @Override
    @Transactional
    public LoginResponse passwordLogin(LoginRequest request) {
        String deviceFingerprint = null;
        String deviceInfo = null;
        try {
            // 验证认证方式
            if (request.getAuthMethod() != AuthMethod.PASSWORD) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.AUTH_METHOD_MISMATCH,
                    "认证方式不匹配，当前请求不是密码登录");
            }

            // 验证协议同意状态
            if (!request.getAgreedToTerms()) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.AGREEMENT_NOT_ACCEPTED,
                    "必须同意用户协议和隐私政策");
            }

            // 验证手机号
            if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.PHONE_NUMBER_REQUIRED,
                    "手机号不能为空");
            }

            // 验证密码
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.PASSWORD_REQUIRED,
                    "密码不能为空");
            }

            // 生成纯设备指纹用于限流
            deviceInfo = standardizeDeviceInfo(request.getDeviceInfo());
            deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(deviceInfo);

            // 检查设备限制
            if (deviceFingerprintService.isDeviceLimited(deviceFingerprint, request.getPhoneNumber(), "LOGIN_ATTEMPT")) {
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.DEVICE_LIMITED,
                    "设备或手机号操作受限，请稍后再试");
            }

            // 验证密码（失败也会在finally中计数）
            boolean isValid = authenticationService.verifyPassword(request.getPhoneNumber(), request.getPassword());
            if (!isValid) {
                log.warn("密码验证失败: 手机号={}", request.getPhoneNumber());
                throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.PASSWORD_INVALID,
                    "密码错误");
            }

            // 路由到对应登录处理器
            LoginHandler handler = resolveLoginHandler(AuthMethod.PASSWORD);
            User user = handler.handle(request.getPhoneNumber(), deviceFingerprint, request.getAgreedToTerms(), deviceInfo);

            // 更新最后登录时间并保存
            user.updateLastLoginTime();
            userRepository.save(user);

            // 生成会话令牌
            String sessionToken = generateSessionToken(user);

            // 发布事件
            publishLoginEvent(user, AuthMethod.PASSWORD, deviceFingerprint, false);

            // 成功登录记录
            createLoginRecord(user, AuthMethod.PASSWORD, deviceFingerprint, true, null, deviceInfo);

            // 执行登录逻辑
            log.info("密码登录成功: phoneNumber={}, userId={}", request.getPhoneNumber(), user.getUserId());
            return responseBuilder.buildLoginResponse(user, sessionToken, sessionExpireHours);

        } catch (PasswordLoginException e) {
            // 失败登录记录
            tryRecordFailureLogin(AuthMethod.PASSWORD, request.getPhoneNumber(), deviceFingerprint, e.getMessage(), deviceInfo);
            // 重新抛出密码登录异常
            throw e;
        } catch (Exception e) {
            // 其他异常包装为密码登录异常
            log.error("密码登录过程中发生未知异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            tryRecordFailureLogin(AuthMethod.PASSWORD, request.getPhoneNumber(), deviceFingerprint, e.getMessage(), deviceInfo);
            throw new PasswordLoginException(PasswordLoginException.PasswordLoginErrorCode.UNKNOWN_ERROR,
                "密码登录过程中发生异常，请稍后重试", e);
        } finally {
            // 无论成功失败，尝试计数一次
            if (deviceFingerprint != null && request.getPhoneNumber() != null) {
                deviceFingerprintService.recordDeviceOperation(deviceFingerprint, request.getPhoneNumber(), "LOGIN_ATTEMPT");
            }
        }
    }

    @Override
    @Transactional
    public boolean logout(String sessionToken) {
        // 从缓存中删除会话
        boolean removed = cacheService.delete("session:" + sessionToken);
        if (removed) {
            log.info("用户退出登录: sessionToken={}", sessionToken);
        }
        return removed;
    }

    @Override
    public String validateSession(String sessionToken) {
        try {
            // 验证会话令牌
            if (sessionToken == null || sessionToken.trim().isEmpty()) {
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED,
                    "会话令牌不能为空");
            }

        // 从缓存中获取用户ID
            String userId = cacheService.get("session:" + sessionToken, String.class);

            if (userId == null) {
                log.warn("会话令牌不存在或已过期: sessionToken={}", sessionToken);
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND,
                    "会话令牌不存在或已过期");
            }

            // 验证用户是否存在且状态正常
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("会话对应的用户不存在: sessionToken={}, userId={}", sessionToken, userId);
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND,
                    "用户不存在");
            }

            User user = userOptional.get();
            if (!user.isAccountActive()) {
                if (user.getStatus() == UserStatus.DISABLED) {
                    throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.USER_DISABLED,
                        "用户已被禁用");
                } else if (user.getStatus() == UserStatus.LOCKED) {
                    throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.USER_LOCKED,
                        "用户已被锁定");
                } else {
                    throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.USER_DISABLED,
                        "用户状态异常: " + user.getStatus());
                }
            }

            log.debug("会话验证成功: sessionToken={}, userId={}", sessionToken, userId);
            return userId;

        } catch (SessionValidationException e) {
            // 重新抛出会话验证异常
            throw e;
        } catch (Exception e) {
            // 其他异常包装为会话验证异常
            log.error("会话验证过程中发生未知异常: sessionToken={}, error={}", sessionToken, e.getMessage(), e);
            throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                "会话验证过程中发生异常，请稍后重试", e);
        }
    }

    @Override
    public UserInfoResponse.UserInfo getUserInfo(String sessionToken) {
        try {
            // 验证会话令牌并获取用户ID
            String userId = validateSession(sessionToken);

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("用户不存在: userId={}", userId);
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND,
                    "用户不存在");
            }

            User user = userOptional.get();
            log.debug("获取用户信息成功: userId={}", userId);

            return new UserInfoResponse.UserInfo(
                user.getUserId(),
                user.getNickname(),
                user.getBio(),
                user.getAvatar(),
                user.hasPassword()
            );

        } catch (SessionValidationException e) {
            // 重新抛出会话验证异常
            throw e;
        } catch (Exception e) {
            // 其他异常包装为会话验证异常
            log.error("获取用户信息过程中发生未知异常: sessionToken={}, error={}", sessionToken, e.getMessage(), e);
            throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                "获取用户信息过程中发生异常，请稍后重试", e);
        }
    }

    @Override
    @Transactional
    public UserInfoResponse.UserInfo updateUserInfo(String sessionToken, UpdateUserInfoRequest request) {
        try {
            // 验证会话令牌并获取用户ID
            String userId = validateSession(sessionToken);

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("用户不存在: userId={}", userId);
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND,
                    "用户不存在");
            }

            User user = userOptional.get();

            // 检查是否有字段需要更新
            boolean hasUpdates = false;

            // 更新昵称（如果提供且不为空）
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname().trim());
                hasUpdates = true;
                log.debug("更新用户昵称: userId={}, nickname={}", userId, request.getNickname());
            }

            // 更新个人简介（如果提供）
            if (request.getBio() != null) {
                user.setBio(request.getBio().trim());
                hasUpdates = true;
                log.debug("更新用户简介: userId={}, bio={}", userId, request.getBio());
            }

            // 更新头像（如果提供且不为空）
            if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
                // 验证base64格式
                String avatarData = request.getAvatar().trim();
                if (isValidBase64Image(avatarData)) {
                    // 检查头像数据大小（限制为500KB的base64数据，约375KB的原始图片）
                    if (avatarData.length() > 500000) {
                        log.warn("头像数据过大: userId={}, size={} bytes", userId, avatarData.length());
                        throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                            "头像数据过大，请选择较小的图片（建议小于375KB）");
                    }

                    user.setAvatar(avatarData);
                    hasUpdates = true;
                    log.debug("更新用户头像: userId={}, avatarSize={} bytes", userId, avatarData.length());
                } else {
                    log.warn("无效的头像base64数据: userId={}", userId);
                    throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                        "头像格式无效，请提供有效的base64图片数据");
                }
            }

            if (!hasUpdates) {
                log.warn("没有提供任何需要更新的字段: userId={}", userId);
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                    "没有提供任何需要更新的字段");
            }

            // 保存用户信息
            userRepository.save(user);
            log.info("更新用户信息成功: userId={}, nickname={}, bio={}, avatar={}",
                    userId, user.getNickname(), user.getBio(),
                    user.getAvatar() != null && user.getAvatar().length() > 20 ?
                            user.getAvatar().substring(0, 20) + "..." :
                            user.getAvatar());


            return new UserInfoResponse.UserInfo(
                user.getUserId(),
                user.getNickname(),
                user.getBio(),
                user.getAvatar(),
                user.hasPassword()
            );

        } catch (SessionValidationException e) {
            // 重新抛出会话验证异常
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 数据库约束异常，通常是字段长度超限
            log.error("更新用户信息数据库约束异常: sessionToken={}, error={}", sessionToken, e.getMessage(), e);
            if (e.getMessage().contains("avatar") && e.getMessage().contains("too long")) {
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                    "头像数据过大，请选择较小的图片（建议小于375KB）");
            } else {
                throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                    "数据保存失败，请检查输入数据", e);
            }
        } catch (Exception e) {
            // 其他异常包装为会话验证异常
            log.error("更新用户信息过程中发生未知异常: sessionToken={}, error={}", sessionToken, e.getMessage(), e);
            throw new SessionValidationException(SessionValidationException.SessionValidationErrorCode.UNKNOWN_ERROR,
                "更新用户信息过程中发生异常，请稍后重试", e);
        }
    }

    /**
     * 验证base64图片格式
     */
    private boolean isValidBase64Image(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return false;
        }

        // 检查是否包含data URL前缀
        if (base64Data.startsWith("data:image/")) {
            // 提取base64部分
            int commaIndex = base64Data.indexOf(',');
            if (commaIndex > 0) {
                String mimeType = base64Data.substring(5, commaIndex);
                String base64Part = base64Data.substring(commaIndex + 1);

                // 验证MIME类型
                if (!mimeType.matches("image/(jpeg|jpg|png|gif|webp)")) {
                    return false;
                }

                // 验证base64格式
                return isValidBase64(base64Part);
            }
        }

        // 直接base64数据
        return isValidBase64(base64Data);
    }

    /**
     * 验证base64字符串格式
     */
    private boolean isValidBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }

        // 检查长度是否为4的倍数
        if (base64.length() % 4 != 0) {
            return false;
        }

        // 检查是否只包含base64字符
        return base64.matches("^[A-Za-z0-9+/]*={0,2}$");
    }

    // 解析并获取登录处理器
    private LoginHandler resolveLoginHandler(AuthMethod method) {
        for (LoginHandler handler : loginHandlers) {
            if (handler.supports(method)) {
                return handler;
            }
        }
        throw new IllegalStateException("未找到对应的登录处理器: " + method);
    }

    // 失败登录记录（最佳努力）
    private void tryRecordFailureLogin(AuthMethod authMethod, String phoneNumber, String deviceFingerprint, String reason, String deviceInfo) {
        try {
            Optional<User> userOptional = (phoneNumber != null) ? userRepository.findByPhoneNumber(phoneNumber) : Optional.empty();
            if (userOptional.isPresent()) {
                createLoginRecord(userOptional.get(), authMethod, deviceFingerprint, false, reason, deviceInfo);
            }
        } catch (Exception ignore) {
            // ignore logging failures
        }
    }

    /**
     * 解析设备信息并标准化设备信息格式：设备ID/型号/OS/版本/APP版本
     */
    private String standardizeDeviceInfo(String deviceInfo) {
        DeviceInfoParser.DeviceInfo parsedDeviceInfo = deviceInfoParser.parseDeviceInfo(deviceInfo);

        return String.format("%s/%s/%s/%s/%s",
                parsedDeviceInfo.getDeviceId() != null ? parsedDeviceInfo.getDeviceId() : "unknown",
                parsedDeviceInfo.getDeviceModel() != null ? parsedDeviceInfo.getDeviceModel() : "unknown",
                parsedDeviceInfo.getOsType() != null ? parsedDeviceInfo.getOsType() : "unknown",
                parsedDeviceInfo.getOsVersion() != null ? parsedDeviceInfo.getOsVersion() : "unknown",
                parsedDeviceInfo.getAppVersion() != null ? parsedDeviceInfo.getAppVersion() : "unknown");
    }

    /**
     * 创建登录记录
     */
    private void createLoginRecord(User user, AuthMethod authMethod, String deviceFingerprint,
                                   boolean success, String failureReason, String deviceInfo) {
        LoginRecord record = success ?
                LoginRecord.successRecord(user.getUserId(), authMethod, deviceFingerprint,
                        getClientIp(), deviceInfo) :
                LoginRecord.failureRecord(user.getUserId(), authMethod, deviceFingerprint,
                        getClientIp(), deviceInfo, failureReason);

        loginRecordRepository.save(record);
    }

    /**
     * 生成会话令牌
     */
    private String generateSessionToken(User user) {
        String sessionToken = UUID.randomUUID().toString();
        // 存储会话到缓存（使用配置的过期时间）
        cacheService.set("session:" + sessionToken, user.getUserId(), sessionExpireHours, TimeUnit.HOURS);
        return sessionToken;
    }

    /**
     * 发布登录事件
     */
    private void publishLoginEvent(User user, AuthMethod authMethod, String deviceFingerprint, boolean isNewUser) {
        UserLoggedInEvent event = new UserLoggedInEvent(
                user.getUserId(), authMethod, LocalDateTime.now(), deviceFingerprint, getClientIp());
        eventPublisher.publishEvent(event);

        // 如果是新注册用户，发布注册事件
        if (isNewUser) {
            UserRegisteredEvent registeredEvent = new UserRegisteredEvent(user);
            eventPublisher.publishEvent(registeredEvent);
        }
    }

    /**
     * 发布用户注销事件
     */
    private void publishUserDeletedEvent(User user, String deviceFingerprint) {
        UserDeletedEvent event = new UserDeletedEvent(this, user, deviceFingerprint);
        eventPublisher.publishEvent(event);
        log.debug("发布用户注销事件: userId={}, phoneNumber={}, deletionId={}", 
            user.getUserId(), user.getPhoneNumber(), user.getDeletionId());
    }

    /**
     * 获取客户端IP（实际项目中应从请求中获取）
     */
    private String getClientIp() {
        // TODO: 从SecurityContext或请求头中获取真实IP
        return "127.0.0.1";
    }

    @Override
    public PasswordResponse sendPasswordSmsCode(String sessionToken, PasswordSmsSendRequest request) {
        try {
            log.info("请求发送密码设置短信验证码: phoneNumber={}", request.getPhoneNumber());

            // 验证会话并获取用户ID
            String userId = validateSession(sessionToken);

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("发送密码设置短信失败: 用户不存在 - userId={}", userId);
                return new PasswordResponse(false, "用户不存在", LocalDateTime.now());
            }

            User user = userOptional.get();

            // 验证手机号一致性
            if (!user.getPhoneNumber().equals(request.getPhoneNumber())) {
                log.warn("发送密码设置短信失败: 手机号不匹配 - userId={}, sessionPhone={}, requestPhone={}",
                    userId, user.getPhoneNumber(), request.getPhoneNumber());
                return new PasswordResponse(false, "手机号不匹配", LocalDateTime.now());
            }

            // 规范化设备信息
            String deviceInfo = standardizeDeviceInfo(request.getDeviceInfo());

            // 生成设备指纹
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(deviceInfo);

            // 检查设备限制
            if (deviceFingerprintService.isDeviceLimited(deviceFingerprint, request.getPhoneNumber(), "SEND_SMS")) {
                log.warn("密码设置短信发送受限: phoneNumber={}, deviceFingerprint={}", request.getPhoneNumber(), deviceFingerprint);
                return new PasswordResponse(false, "操作受限，请稍后再试", LocalDateTime.now());
            }

            // 验证密码强度
            PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(
                request.getNewPassword(), user.getPhoneNumber(), user.getUserId());

            if (!validationResult.isValid()) {
                log.warn("密码设置短信发送失败: 密码强度不符合要求 - userId={}, phoneNumber={}, reason={}",
                    userId, request.getPhoneNumber(), validationResult.getMessage());
                return new PasswordResponse(false, validationResult.getMessage(), LocalDateTime.now());
            }

            // 哈希新密码
            String hashedPassword = authenticationService.hashPassword(request.getNewPassword());

            // 缓存新密码
            boolean cacheResult = authenticationService.cachePasswordForSetup(
                request.getPhoneNumber(), deviceFingerprint, hashedPassword);

            if (!cacheResult) {
                log.error("密码设置新密码缓存失败: phoneNumber={}, deviceFingerprint={}",
                    request.getPhoneNumber(), deviceFingerprint);
                return new PasswordResponse(false, "系统异常，请稍后重试", LocalDateTime.now());
            }

            // 调用领域服务发送短信
            boolean result = authenticationService.sendPasswordSmsCode(request.getPhoneNumber(), deviceFingerprint);

            if (result) {
                // 记录设备操作
                deviceFingerprintService.recordDeviceOperation(deviceFingerprint, request.getPhoneNumber(), "SEND_SMS");
                log.info("密码设置短信验证码发送成功: phoneNumber={}", request.getPhoneNumber());
                return new PasswordResponse(true, "短信验证码发送成功", LocalDateTime.now());
            } else {
                // 发送失败，清除缓存的密码
                authenticationService.clearCachedPasswordForSetup(request.getPhoneNumber(), deviceFingerprint);
                log.error("密码设置短信验证码发送失败: phoneNumber={}", request.getPhoneNumber());
                return new PasswordResponse(false, "短信验证码发送失败，请稍后重试", LocalDateTime.now());
            }

        } catch (SessionValidationException e) {
            log.warn("密码设置短信发送会话验证异常: error={}", e.getMessage());
            return new PasswordResponse(false, e.getMessage(), LocalDateTime.now());
        } catch (SmsSendException e) {
            log.warn("密码设置短信发送业务异常: phoneNumber={}, error={}", request.getPhoneNumber(), e.getMessage());
            return new PasswordResponse(false, e.getMessage(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("密码设置短信发送系统异常: phoneNumber={}, error={}", request.getPhoneNumber(), e.getMessage(), e);
            return new PasswordResponse(false, "系统异常，请稍后重试", LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public PasswordResponse setPassword(String sessionToken, PasswordSetRequest request) {
        try {
            log.info("请求设置密码: phoneNumber={}", request.getPhoneNumber());

            // 验证会话令牌并获取用户ID
            String userId = validateSession(sessionToken);

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("设置密码失败: 用户不存在 - userId={}", userId);
                return new PasswordResponse(false, "用户不存在", LocalDateTime.now());
            }

            User user = userOptional.get();

            // 验证手机号一致性
            if (!user.getPhoneNumber().equals(request.getPhoneNumber())) {
                log.warn("设置密码失败: 手机号不匹配 - userId={}, sessionPhone={}, requestPhone={}",
                    userId, user.getPhoneNumber(), request.getPhoneNumber());
                return new PasswordResponse(false, "手机号不匹配", LocalDateTime.now());
            }

            // 检查用户状态
            if (!user.isAccountActive()) {
                log.warn("设置密码失败: 用户账号不可用 - userId={}, status={}", userId, user.getStatus());
                return new PasswordResponse(false, "用户账号不可用", LocalDateTime.now());
            }

            // 生成设备指纹用于验证码校验（统一先标准化）
            String deviceInfo = standardizeDeviceInfo(request.getDeviceInfo());
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(deviceInfo);

            // 验证短信验证码
            boolean smsValid = authenticationService.verifyPasswordSmsCode(
                request.getPhoneNumber(), request.getSmsCode(), deviceFingerprint);

            if (!smsValid) {
                log.warn("设置密码失败: 短信验证码无效 - userId={}, phoneNumber={}", userId, request.getPhoneNumber());
                return new PasswordResponse(false, "短信验证码无效或已过期", LocalDateTime.now());
            }

            // 从缓存获取新密码
            String cachedPassword = authenticationService.getCachedPasswordForSetup(
                request.getPhoneNumber(), deviceFingerprint);

            if (cachedPassword == null) {
                log.warn("设置密码失败: 缓存的密码不存在或已过期 - userId={}, phoneNumber={}", userId, request.getPhoneNumber());
                return new PasswordResponse(false, "密码设置会话已过期，请重新开始", LocalDateTime.now());
            }

            // 设置密码
            boolean setResult = authenticationService.setPassword(request.getPhoneNumber(), cachedPassword);

            if (setResult) {
                // 设置成功后清除缓存的密码
                authenticationService.clearCachedPasswordForSetup(request.getPhoneNumber(), deviceFingerprint);
                log.info("密码设置成功: userId={}, phoneNumber={}", userId, request.getPhoneNumber());
                return new PasswordResponse(true, "密码设置成功", LocalDateTime.now());
            } else {
                log.error("密码设置失败: userId={}, phoneNumber={}", userId, request.getPhoneNumber());
                return new PasswordResponse(false, "密码设置失败，请稍后重试", LocalDateTime.now());
            }

        } catch (SessionValidationException e) {
            log.warn("设置密码会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
            return new PasswordResponse(false, e.getMessage(), LocalDateTime.now());
        } catch (SmsSendException e) {
            log.warn("设置密码短信验证异常: phoneNumber={}, errorCode={}, message={}",
                request.getPhoneNumber(), e.getErrorCode().getCode(), e.getMessage());
            return new PasswordResponse(false, e.getMessage(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("设置密码系统异常: phoneNumber={}, error={}",
                request.getPhoneNumber(), e.getMessage(), e);
            return new PasswordResponse(false, "系统异常，请稍后重试", LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public PasswordResponse changePassword(String sessionToken, PasswordChangeRequest request) {
        try {
            log.info("请求修改密码");

            // 验证会话令牌并获取用户ID
            String userId = validateSession(sessionToken);

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("修改密码失败: 用户不存在 - userId={}", userId);
                return new PasswordResponse(false, "用户不存在", LocalDateTime.now());
            }

            User user = userOptional.get();

            // 检查用户是否已设置密码
            if (!user.hasPassword()) {
                log.warn("修改密码失败: 用户未设置密码 - userId={}", userId);
                return new PasswordResponse(false, "请先设置密码", LocalDateTime.now());
            }

            // 验证密码强度
            PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(
                request.getNewPassword(), user.getPhoneNumber(), user.getUserId());

            if (!validationResult.isValid()) {
                log.warn("修改密码失败: 密码强度不符合要求 - userId={}, reason={}", userId, validationResult.getMessage());
                return new PasswordResponse(false, validationResult.getMessage(), LocalDateTime.now());
            }

            // 修改密码（直接传递用户对象，避免重复查询）
            boolean changeResult = authenticationService.changePassword(
                user, request.getOldPassword(), request.getNewPassword());

            if (changeResult) {
                log.info("密码修改成功: userId={}", userId);
                return new PasswordResponse(true, "密码修改成功", LocalDateTime.now());
            } else {
                log.warn("密码修改失败: 旧密码不正确 - userId={}", userId);
                return new PasswordResponse(false, "旧密码不正确", LocalDateTime.now());
            }

        } catch (SessionValidationException e) {
            log.warn("修改密码会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
            return new PasswordResponse(false, e.getMessage(), LocalDateTime.now());
        } catch (SmsSendException e) {
            log.warn("修改密码短信验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
            return new PasswordResponse(false, e.getMessage(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("修改密码系统异常: error={}", e.getMessage(), e);
            return new PasswordResponse(false, "系统异常，请稍后重试", LocalDateTime.now());
        }
    }

    @Override
    public SmsSendResponse sendDeletionSmsCode(String sessionToken, AccountDeletionSmsRequest request) {
        try {
            log.info("请求发送账号注销短信验证码");

            // 验证会话令牌并获取用户ID
            String userId = validateSession(sessionToken);

            // 验证是否已同意注销说明
            if (request.getAgreedToDeletion() == null || !request.getAgreedToDeletion()) {
                log.warn("发送账号注销短信验证码失败: 未同意注销说明 - userId={}", userId);
                throw new IllegalArgumentException("必须同意注销说明");
            }

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("发送账号注销短信验证码失败: 用户不存在 - userId={}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOptional.get();

            // 检查设备限流
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(standardizeDeviceInfo(request.getDeviceInfo()));
            if (deviceFingerprintService.isDeviceLimited(deviceFingerprint, user.getPhoneNumber(), "SEND_SMS")) {
                log.warn("发送账号注销短信验证码失败: 设备操作受限 - userId={}, deviceFingerprint={}", userId, deviceFingerprint);
                throw new SecurityException("操作受限，请稍后再试");
            }

            // 发送短信验证码
            boolean result = authenticationService.sendDeletionSmsCode(user.getPhoneNumber(), deviceFingerprint);

            if (result) {
                // 记录设备操作
                deviceFingerprintService.recordDeviceOperation(deviceFingerprint, user.getPhoneNumber(), "SEND_SMS");
                log.info("账号注销短信验证码发送成功: userId={}, phoneNumber={}", userId, user.getPhoneNumber());
                return new SmsSendResponse(true, "验证码发送成功", LocalDateTime.now(), 6);
            } else {
                log.error("账号注销短信验证码发送失败: userId={}, phoneNumber={}", userId, user.getPhoneNumber());
                throw new SmsSendException(SmsSendException.SmsSendErrorCode.SMS_SEND_FAILED, "验证码发送失败");
            }

        } catch (SessionValidationException e) {
            log.warn("发送账号注销短信验证码会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("发送账号注销短信验证码参数异常: message={}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("发送账号注销短信验证码安全异常: message={}", e.getMessage());
            throw e;
        } catch (SmsSendException e) {
            log.warn("发送账号注销短信验证码短信发送异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发送账号注销短信验证码系统异常: error={}", e.getMessage(), e);
            throw new SmsSendException(SmsSendException.SmsSendErrorCode.UNKNOWN_ERROR, "系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional
    public AccountDeletionResponse submitDeletion(String sessionToken, AccountDeletionRequest request) {
        try {
            log.info("请求提交账号注销申请");

            // 验证会话令牌并获取用户ID
            String userId = validateSession(sessionToken);

            // 获取用户信息
            Optional<User> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("提交账号注销申请失败: 用户不存在 - userId={}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOptional.get();

            // 验证短信验证码
            String deviceFingerprint = deviceFingerprintService.generateDeviceFingerprint(standardizeDeviceInfo(request.getDeviceInfo()));
            boolean smsValid = authenticationService.verifyDeletionSmsCode(user.getPhoneNumber(), request.getSmsCode(), deviceFingerprint);
            if (!smsValid) {
                log.warn("提交账号注销申请失败: 短信验证码错误或已过期 - userId={}", userId);
                throw new SecurityException("短信验证码错误或已过期");
            }

            // 软删除用户数据
            user.markAsDeleted();
            user.setDeletionTime(LocalDateTime.now());
            user.setDeletionReason(request.getDeletionReason());
            user.setDeletionId(UUID.randomUUID().toString());
            userRepository.save(user);

            // 失效当前会话
            String sessionKey = "session:" + sessionToken;
            cacheService.delete(sessionKey);
            log.debug("失效当前会话: sessionKey={}", sessionKey);

            // 清理验证码/密码缓存
            String smsCodeKey = "sms:code:" + user.getPhoneNumber() + ":" + deviceFingerprint;
            String passwordKey = "password:setup:" + user.getPhoneNumber() + ":" + deviceFingerprint;
            cacheService.delete(smsCodeKey);
            cacheService.delete(passwordKey);
            log.debug("清理验证码和密码缓存: smsCodeKey={}, passwordKey={}", smsCodeKey, passwordKey);

            // 清理计数键
            String phoneSmsKey = "opcount:sms:phone:" + user.getPhoneNumber();
            String phoneLoginKey = "opcount:login:phone:" + user.getPhoneNumber();
            String deviceSmsKey = "opcount:sms:device:" + deviceFingerprint;
            String deviceLoginKey = "opcount:login:device:" + deviceFingerprint;
            cacheService.delete(phoneSmsKey);
            cacheService.delete(phoneLoginKey);
            cacheService.delete(deviceSmsKey);
            cacheService.delete(deviceLoginKey);
            log.debug("清理计数键: phoneSmsKey={}, phoneLoginKey={}, deviceSmsKey={}, deviceLoginKey={}", 
                phoneSmsKey, phoneLoginKey, deviceSmsKey, deviceLoginKey);

            // 清理限制键
            String deviceLoginLimitKey = "limit:login:device:" + deviceFingerprint;
            String deviceSmsLimitKey = "limit:sms:device:" + deviceFingerprint;
            String phoneLoginLimitKey = "limit:login:phone:" + user.getPhoneNumber();
            String phoneSmsLimitKey = "limit:sms:phone:" + user.getPhoneNumber();
            cacheService.delete(deviceLoginLimitKey);
            cacheService.delete(deviceSmsLimitKey);
            cacheService.delete(phoneLoginLimitKey);
            cacheService.delete(phoneSmsLimitKey);
            log.debug("清理限制键: deviceLoginLimitKey={}, deviceSmsLimitKey={}, phoneLoginLimitKey={}, phoneSmsLimitKey={}", 
                deviceLoginLimitKey, deviceSmsLimitKey, phoneLoginLimitKey, phoneSmsLimitKey);

            // 发布用户注销事件
            publishUserDeletedEvent(user, deviceFingerprint);

            // 记录操作日志
            log.info("用户账号注销成功: userId={}, phoneNumber={}, reason={}, deletionId={}",
                userId, user.getPhoneNumber(), request.getDeletionReason(), user.getDeletionId());

            return new AccountDeletionResponse(true, "账号注销成功", LocalDateTime.now(), user.getDeletionId());

        } catch (SessionValidationException e) {
            log.warn("提交账号注销申请会话验证异常: errorCode={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("提交账号注销申请参数异常: message={}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("提交账号注销申请安全异常: message={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("提交账号注销申请系统异常: error={}", e.getMessage(), e);
            throw new RuntimeException("系统异常，请稍后重试");
        }
    }
}
