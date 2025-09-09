package com.rtalky.infrastructure.cache;

/**
 * 缓存键生成器
 * 统一管理缓存键的生成规则
 */
public class CacheKeyGenerator {

    // 短信验证码缓存键前缀
    public static final String SMS_CODE_PREFIX = "sms:code:";

    // 设备指纹限流缓存键前缀
    public static final String DEVICE_LIMIT_PREFIX = "limit:device:";

    // 手机号限流缓存键前缀
    public static final String PHONE_LIMIT_PREFIX = "limit:phone:";

    // 设备操作计数缓存键前缀
    public static final String DEVICE_OP_COUNT_PREFIX = "opcount:device:";

    // 手机号操作计数缓存键前缀
    public static final String PHONE_OP_COUNT_PREFIX = "opcount:phone:";

    // 用户会话缓存键前缀
    public static final String USER_SESSION_PREFIX = "session:user:";

    // 用户信息缓存键前缀
    public static final String USER_INFO_PREFIX = "user:info:";

    /**
     * 生成短信验证码缓存键
     */
    public static String smsCodeKey(String phoneNumber) {
        return SMS_CODE_PREFIX + phoneNumber;
    }

    /**
     * 生成设备限制缓存键
     */
    public static String deviceLimitKey(String deviceFingerprint) {
        return DEVICE_LIMIT_PREFIX + deviceFingerprint;
    }

    /**
     * 生成手机号限制缓存键
     */
    public static String phoneLimitKey(String phoneNumber) {
        return PHONE_LIMIT_PREFIX + phoneNumber;
    }

    /**
     * 生成设备操作计数缓存键
     */
    public static String deviceOpCountKey(String operation, String deviceFingerprint) {
        return DEVICE_OP_COUNT_PREFIX + operation + ":" + deviceFingerprint;
    }

    /**
     * 生成手机号操作计数缓存键
     */
    public static String phoneOpCountKey(String operation, String phoneNumber) {
        return PHONE_OP_COUNT_PREFIX + operation + ":" + phoneNumber;
    }

    /**
     * 生成用户会话缓存键
     */
    public static String userSessionKey(String sessionId) {
        return USER_SESSION_PREFIX + sessionId;
    }

    /**
     * 生成用户信息缓存键
     */
    public static String userInfoKey(String userId) {
        return USER_INFO_PREFIX + userId;
    }
}
