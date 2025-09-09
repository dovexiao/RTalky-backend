package com.rtalky.infrastructure.cache.exception;

/**
 * 缓存异常
 */
public class CacheException extends RuntimeException {

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
