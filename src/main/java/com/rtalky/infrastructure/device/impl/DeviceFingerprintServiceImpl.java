package com.rtalky.infrastructure.device.impl;

import com.rtalky.infrastructure.device.DeviceFingerprintService;
import com.rtalky.infrastructure.device.config.DeviceFingerprintProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 设备指纹服务实现
 * 使用SHA-256生成设备指纹，Redis实现限流计数
 */
@Slf4j
@Service
public class DeviceFingerprintServiceImpl implements DeviceFingerprintService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOps;
    private final DeviceFingerprintProperties properties;

    @Autowired
    public DeviceFingerprintServiceImpl(RedisTemplate<String, String> redisTemplate,
                                      DeviceFingerprintProperties properties) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.properties = properties;
    }

    @Override
    public String generateDeviceFingerprint(String deviceInfo) {
        try {
            // 仅使用设备信息生成指纹
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(deviceInfo.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String fingerprint = hexString.toString();
            log.debug("生成纯设备指纹: {}, 设备信息: {}", fingerprint, deviceInfo);

            return fingerprint;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256算法不可用", e);
            throw new RuntimeException("设备指纹生成失败", e);
        }
    }

    @Override
    public String generateCompositeFingerprint(String deviceInfo, String phoneNumber) {
        try {
            // 组合设备信息和手机号生成指纹
            String compositeData = deviceInfo + (phoneNumber != null ? phoneNumber : "");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(compositeData.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String fingerprint = hexString.toString();
            log.debug("生成设备指纹: {}, 原始数据: {}", fingerprint, compositeData);

            return fingerprint;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256算法不可用", e);
            throw new RuntimeException("设备指纹生成失败", e);
        }
    }

    @Override
    public boolean verifyFingerprint(String fingerprint, String deviceInfo) {
        // 重新生成指纹进行比对
        String generatedFingerprint = generateDeviceFingerprint(deviceInfo);
        boolean matches = generatedFingerprint.equals(fingerprint);

        if (!matches) {
            log.warn("设备指纹验证失败: 提供的={}, 生成的={}", fingerprint, generatedFingerprint);
        }

        return matches;
    }

    @Override
    public boolean isDeviceLimited(String fingerprint, String phoneNumber, String operation) {
        // 检查设备级别限制
        String deviceKey = "limit:" + getBusinessType(operation + "_PER_DEVICE") + ":device:" + fingerprint;
        if ("true".equals(valueOps.get(deviceKey))) {
            log.warn("设备已被限制: {}", fingerprint);
            return true;
        }

        // 检查手机号级别限制
        if (phoneNumber != null) {
            String phoneKey = "limit:" + getBusinessType(operation) + ":phone:" + phoneNumber;
            if ("true".equals(valueOps.get(phoneKey))) {
                log.warn("手机号已被限制: {}", phoneNumber);
                return true;
            }
        }

        return false;
    }

    @Override
    public void recordDeviceOperation(String fingerprint, String phoneNumber, String operation) {
        // 根据操作类型获取对应的限流配置
        DeviceFingerprintProperties.OperationLimit config = getRateLimitConfig(operation);
        DeviceFingerprintProperties.OperationLimit configDevice = getRateLimitConfig(operation + "_PER_DEVICE");

        if (config == null) {
            log.warn("未找到操作类型的限流配置: {}", operation);
            return;
        }

        if (configDevice == null) {
            log.warn("未找到设备操作类型的限流配置: {}", operation + "_PER_DEVICE");
            return;
        }

        // 记录设备操作计数
        String deviceOpKey = "opcount:" + getBusinessType(operation + "_PER_DEVICE") + ":device:" + operation + ":" + fingerprint;
        long deviceCount = incrementAndGet(deviceOpKey, configDevice.getTimeWindow());

        // 记录手机号操作计数
        if (phoneNumber != null) {
            String phoneOpKey = "opcount:" + getBusinessType(operation) + ":phone:" + operation + ":" + phoneNumber;
            long phoneCount = incrementAndGet(phoneOpKey, config.getTimeWindow());

            // 检查是否超过限制
            if (phoneCount >= config.getMaxAttempts()) {
                String phoneLimitKey = "limit:" + getBusinessType(operation) + ":phone:" + phoneNumber;
                valueOps.set(phoneLimitKey, "true", Duration.ofHours(24));
                log.warn("手机号 {}, 操作达到或者超过限制，已被限制24小时: 操作: {}, 次数: {}",
                        phoneNumber, operation, phoneCount);
            }
        }

        // 检查设备是否超过限制
        if (deviceCount >= configDevice.getMaxAttempts()) {
            String deviceLimitKey = "limit:" + getBusinessType(operation + "_PER_DEVICE") + ":device:" + fingerprint;
            valueOps.set(deviceLimitKey, "true", Duration.ofHours(24));
            log.warn("设备 {}, 操作达到或者超过限制，已被限制24小时: 操作: {}, 次数: {}",
                    fingerprint, operation, deviceCount);
        }
    }

    @Override
    public int getOperationCount(String fingerprint, String phoneNumber, String operation, int timeWindow) {
        String key;
        if (phoneNumber != null) {
            key = "opcount:phone:" + operation + ":" + phoneNumber;
        } else {
            key = "opcount:device:" + operation + ":" + fingerprint;
        }

        String countStr = valueOps.get(key);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    /**
     * 递增计数并设置过期时间
     */
    private long incrementAndGet(String key, int expireMinutes) {
        Long count = valueOps.increment(key);
        // 每次递增后都刷新过期时间
        redisTemplate.expire(key, expireMinutes, TimeUnit.MINUTES);
        return count != null ? count : 0;
    }

    /**
     * 定期清理过期限流记录
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void cleanupExpiredLimits() {
        log.info("开始清理过期的设备限制记录");

        // 查找所有限制键
        String pattern = "limit:*";
        redisTemplate.keys(pattern).forEach(key -> {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl != null && ttl < 0) {
                redisTemplate.delete(key);
                log.debug("清理过期限制键: {}", key);
            }
        });

        log.info("设备限制记录清理完成");
    }

    /**
     * 根据操作类型获取对应的限流配置
     */
    private DeviceFingerprintProperties.OperationLimit getRateLimitConfig(String operation) {
        return switch (operation) {
            case "SEND_SMS" -> properties.getRateLimit().getSendSms();
            case "SEND_SMS_PER_DEVICE" -> properties.getRateLimit().getSendSmsDevice();
            case "LOGIN_ATTEMPT" -> properties.getRateLimit().getLoginAttempt();
            case "LOGIN_ATTEMPT_PER_DEVICE" -> properties.getRateLimit().getLoginAttemptDevice();
            default -> null;
        };
    }

    /**
     * 根据操作类型获取业务类型
     */
    private String getBusinessType(String operation) {
        return switch (operation) {
            case "SEND_SMS", "SEND_SMS_PER_DEVICE" -> "sms";
            case "LOGIN_ATTEMPT", "LOGIN_ATTEMPT_PER_DEVICE" -> "login";
            default -> "unknown";
        };
    }
}
