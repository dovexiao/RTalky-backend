package com.rtalky.infrastructure.device.exception;

/**
 * 设备限制异常
 */
public class DeviceLimitedException extends RuntimeException {

    public DeviceLimitedException(String message) {
        super(message);
    }

    public DeviceLimitedException(String message, Throwable cause) {
        super(message, cause);
    }
}
