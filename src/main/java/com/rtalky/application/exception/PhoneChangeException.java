package com.rtalky.application.exception;

import lombok.Getter;

/**
 * 换绑手机号业务异常
 */
@Getter
public class PhoneChangeException extends RuntimeException {

    private final PhoneChangeErrorCode errorCode;

    public PhoneChangeException(PhoneChangeErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PhoneChangeException(PhoneChangeErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 换绑手机号错误码枚举
     */
    @Getter
    public enum PhoneChangeErrorCode {
        UNKNOWN_ERROR("PHONE_CHANGE_0001", "未知错误"),
        PHONE_SAME("PHONE_CHANGE_0002", "新手机号与当前手机号相同"),
        PHONE_ALREADY_EXISTS("PHONE_CHANGE_0003", "新手机号已被其他用户使用"),
        PHONE_RESTRICTED("PHONE_CHANGE_0004", "新手机号被限制"),
        SMS_CODE_INVALID("PHONE_CHANGE_0005", "短信验证码错误或已过期"),
        SMS_CODE_EXPIRED("PHONE_CHANGE_0006", "短信验证码已过期"),
        SMS_CODE_NOT_FOUND("PHONE_CHANGE_0007", "短信验证码不存在"),
        USER_NOT_FOUND("PHONE_CHANGE_0008", "用户不存在"),
        SESSION_EXPIRED("PHONE_CHANGE_0009", "会话已过期"),
        DEVICE_LIMITED("PHONE_CHANGE_0010", "设备操作受限"),
        PHONE_LIMITED("PHONE_CHANGE_0011", "手机号操作受限"),
        CACHE_ERROR("PHONE_CHANGE_0012", "缓存服务异常"),
        SYSTEM_ERROR("PHONE_CHANGE_0013", "系统异常");

        private final String code;
        private final String description;

        PhoneChangeErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}
