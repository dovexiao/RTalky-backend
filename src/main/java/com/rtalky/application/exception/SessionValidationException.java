package com.rtalky.application.exception;

import lombok.Getter;

/**
 * 会话验证业务异常
 */
@Getter
public class SessionValidationException extends RuntimeException {

    private final SessionValidationErrorCode errorCode;

    public SessionValidationException(SessionValidationErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SessionValidationException(SessionValidationErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 会话验证错误码枚举
     */
    @Getter
    public enum SessionValidationErrorCode {
        UNKNOWN_ERROR("SESSION_0001", "未知错误"),
        TOKEN_REQUIRED("SESSION_0002", "会话令牌不能为空"),
        INVALID_TOKEN_FORMAT("SESSION_0003", "会话令牌格式无效"),
        TOKEN_NOT_FOUND("SESSION_0004", "会话令牌不存在"),
        TOKEN_EXPIRED("SESSION_0005", "会话令牌已过期"),
        TOKEN_INVALID("SESSION_0006", "会话令牌无效"),
        USER_NOT_FOUND("SESSION_0007", "用户不存在"),
        USER_DISABLED("SESSION_0008", "用户已被禁用"),
        USER_LOCKED("SESSION_0009", "用户已被锁定"),
        CACHE_ERROR("SESSION_0010", "缓存服务异常"),
        AUTHORIZATION_HEADER_MISSING("SESSION_0011", "缺少Authorization头"),
        AUTHORIZATION_HEADER_INVALID("SESSION_0012", "Authorization头格式无效");

        private final String code;
        private final String description;

        SessionValidationErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}
