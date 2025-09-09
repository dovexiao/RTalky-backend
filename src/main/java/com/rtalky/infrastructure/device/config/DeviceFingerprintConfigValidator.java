package com.rtalky.infrastructure.device.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 设备指纹配置验证器
 * 在应用启动完成后验证配置的合理性
 */
@Slf4j
@Component
public class DeviceFingerprintConfigValidator {

    private final DeviceFingerprintProperties properties;

    @Autowired
    public DeviceFingerprintConfigValidator(DeviceFingerprintProperties properties) {
        this.properties = properties;
    }

    /**
     * 应用启动完成后验证配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        log.info("开始验证设备指纹配置...");

        try {
            validateRateLimitConfig();
            validateRestrictionConfig();
            validateAlgorithmConfig();
            
            log.info("设备指纹配置验证完成，所有配置项均有效");
        } catch (Exception e) {
            log.error("设备指纹配置验证失败: {}", e.getMessage());
            // 在开发环境中可以抛出异常，生产环境中可以记录日志后继续运行
            if (isDevelopmentEnvironment()) {
                throw new RuntimeException("设备指纹配置验证失败", e);
            }
        }
    }

    /**
     * 验证限流配置
     */
    private void validateRateLimitConfig() {
        log.debug("验证限流配置...");

        // 验证短信发送限流配置
        validateOperationLimit("短信发送", properties.getRateLimit().getSendSms());
        validateOperationLimit("设备短信发送", properties.getRateLimit().getSendSmsDevice());

        // 验证登录尝试限流配置
        validateOperationLimit("登录尝试", properties.getRateLimit().getLoginAttempt());
        validateOperationLimit("设备登录尝试", properties.getRateLimit().getLoginAttemptDevice());

        // 验证配置的合理性
        validateRateLimitLogic();
    }

    /**
     * 验证操作限流配置
     */
    private void validateOperationLimit(String operationName, DeviceFingerprintProperties.OperationLimit limit) {
        if (limit == null) {
            throw new IllegalArgumentException(operationName + "限流配置不能为空");
        }

        if (limit.getMaxAttempts() <= 0) {
            throw new IllegalArgumentException(operationName + "最大尝试次数必须大于0");
        }

        if (limit.getTimeWindow() <= 0) {
            throw new IllegalArgumentException(operationName + "时间窗口必须大于0");
        }

        if (limit.getTimeWindow() > 1440) {
            throw new IllegalArgumentException(operationName + "时间窗口不能超过24小时(1440分钟)");
        }

        log.debug("{}限流配置验证通过: 最大尝试次数={}, 时间窗口={}分钟", 
                operationName, limit.getMaxAttempts(), limit.getTimeWindow());
    }

    /**
     * 验证限流逻辑的合理性
     */
    private void validateRateLimitLogic() {
        // 验证设备级别限制应该大于等于手机号级别限制
        if (properties.getRateLimit().getSendSmsDevice().getMaxAttempts() < 
            properties.getRateLimit().getSendSms().getMaxAttempts()) {
            log.warn("设备短信发送限制({})小于手机号限制({})，可能导致设备限制过于严格",
                    properties.getRateLimit().getSendSmsDevice().getMaxAttempts(),
                    properties.getRateLimit().getSendSms().getMaxAttempts());
        }

        if (properties.getRateLimit().getLoginAttemptDevice().getMaxAttempts() < 
            properties.getRateLimit().getLoginAttempt().getMaxAttempts()) {
            log.warn("设备登录尝试限制({})小于手机号限制({})，可能导致设备限制过于严格",
                    properties.getRateLimit().getLoginAttemptDevice().getMaxAttempts(),
                    properties.getRateLimit().getLoginAttempt().getMaxAttempts());
        }

        // 验证时间窗口的合理性
        if (properties.getRateLimit().getSendSms().getTimeWindow() < 5) {
            log.warn("短信发送时间窗口({}分钟)过短，可能导致用户体验不佳",
                    properties.getRateLimit().getSendSms().getTimeWindow());
        }

        if (properties.getRateLimit().getLoginAttempt().getTimeWindow() < 5) {
            log.warn("登录尝试时间窗口({}分钟)过短，可能导致用户体验不佳",
                    properties.getRateLimit().getLoginAttempt().getTimeWindow());
        }
    }

    /**
     * 验证限制惩罚配置
     */
    private void validateRestrictionConfig() {
        log.debug("验证限制惩罚配置...");

        if (properties.getRestriction() == null) {
            throw new IllegalArgumentException("限制惩罚配置不能为空");
        }

        if (properties.getRestriction().getDurationHours() <= 0) {
            throw new IllegalArgumentException("限制时长必须大于0小时");
        }

        if (properties.getRestriction().getDurationHours() > 720) {
            throw new IllegalArgumentException("限制时长不能超过30天(720小时)");
        }

        log.debug("限制惩罚配置验证通过: 限制时长={}小时", 
                properties.getRestriction().getDurationHours());
    }

    /**
     * 验证算法配置
     */
    private void validateAlgorithmConfig() {
        log.debug("验证算法配置...");

        if (properties.getAlgorithm() == null || properties.getAlgorithm().trim().isEmpty()) {
            throw new IllegalArgumentException("算法类型不能为空");
        }

        // 验证算法是否支持
        String algorithm = properties.getAlgorithm().toUpperCase();
        if (!algorithm.equals("SHA-256") && !algorithm.equals("SHA-1") && !algorithm.equals("MD5")) {
            log.warn("不支持的算法类型: {}，建议使用 SHA-256", algorithm);
        }

        log.debug("算法配置验证通过: 算法类型={}", properties.getAlgorithm());
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevelopmentEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile == null || profile.equals("dev") || profile.equals("development");
    }
}
