package com.rtalky.application.exception;

import lombok.Getter;

/**
 * 密码登录业务异常
 */
@Getter
public class PasswordLoginException extends RuntimeException {

    private final PasswordLoginErrorCode errorCode;

    public PasswordLoginException(PasswordLoginErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PasswordLoginException(PasswordLoginErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 密码登录错误码枚举
     */
    @Getter
    public enum PasswordLoginErrorCode {
        UNKNOWN_ERROR("PASSWORD_LOGIN_0001", "未知错误"),
        INVALID_PHONE_NUMBER("PASSWORD_LOGIN_0002", "手机号格式无效"),
        PHONE_NUMBER_REQUIRED("PASSWORD_LOGIN_0003", "手机号不能为空"),
        PASSWORD_REQUIRED("PASSWORD_LOGIN_0004", "密码不能为空"),
        AGREEMENT_NOT_ACCEPTED("PASSWORD_LOGIN_0005", "必须同意用户协议和隐私政策"),
        DEVICE_LIMITED("PASSWORD_LOGIN_0006", "设备或手机号操作受限"),
        PASSWORD_INVALID("PASSWORD_LOGIN_0007", "密码错误"),
        USER_NOT_FOUND("PASSWORD_LOGIN_0008", "用户不存在"),
        AUTH_METHOD_MISMATCH("PASSWORD_LOGIN_0009", "认证方式不匹配"),
        ACCOUNT_DISABLED("PASSWORD_LOGIN_0010", "账号已被禁用"),
        ACCOUNT_LOCKED("PASSWORD_LOGIN_0011", "账号已被锁定"),
        PASSWORD_NOT_SET("PASSWORD_LOGIN_0012", "请先设置密码"),
        CACHE_ERROR("PASSWORD_LOGIN_0013", "缓存服务异常"),
        USER_SERVICE_ERROR("PASSWORD_LOGIN_0014", "用户服务异常"),
        PHONE_RESTRICTED("PASSWORD_LOGIN_0015", "手机号被限制");

        private final String code;
        private final String description;

        PasswordLoginErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}
