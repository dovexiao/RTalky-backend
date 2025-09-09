package com.rtalky.application.exception;

import lombok.Getter;

/**
 * 短信登录业务异常
 */
@Getter
public class SmsLoginException extends RuntimeException {

    private final SmsLoginErrorCode errorCode;

    public SmsLoginException(SmsLoginErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SmsLoginException(SmsLoginErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 短信登录错误码枚举
     */
    @Getter
    public enum SmsLoginErrorCode {
        UNKNOWN_ERROR("SMS_LOGIN_0001", "未知错误"),
        INVALID_PHONE_NUMBER("SMS_LOGIN_0002", "手机号格式无效"),
        PHONE_NUMBER_REQUIRED("SMS_LOGIN_0003", "手机号不能为空"),
        SMS_CODE_REQUIRED("SMS_LOGIN_0004", "短信验证码不能为空"),
        AGREEMENT_NOT_ACCEPTED("SMS_LOGIN_0005", "必须同意用户协议和隐私政策"),
        DEVICE_LIMITED("SMS_LOGIN_0006", "设备或手机号操作受限"),
        SMS_CODE_INVALID("SMS_LOGIN_0007", "短信验证码错误或已过期"),
        SMS_CODE_EXPIRED("SMS_LOGIN_0008", "短信验证码已过期"),
        SMS_CODE_NOT_FOUND("SMS_LOGIN_0009", "短信验证码不存在"),
        AUTH_METHOD_MISMATCH("SMS_LOGIN_0010", "认证方式不匹配"),
        ACCOUNT_DISABLED("SMS_LOGIN_0011", "账号已被禁用"),
        ACCOUNT_LOCKED("SMS_LOGIN_0012", "账号已被锁定"),
        CACHE_ERROR("SMS_LOGIN_0013", "缓存服务异常"),
        USER_SERVICE_ERROR("SMS_LOGIN_0014", "用户服务异常");

        private final String code;
        private final String description;

        SmsLoginErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}
