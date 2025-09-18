package com.rtalky.infrastructure.device.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 设备指纹配置属性
 * 从 application.yml 读取限流配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "device.fingerprint")
@Validated
public class DeviceFingerprintProperties {

    /**
     * 算法类型
     */
    private String algorithm = "SHA-256";

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 限制惩罚配置
     */
    private Restriction restriction = new Restriction();

    @Data
    public static class RateLimit {
        /**
         * 手机号短信发送限流配置
         */
        private OperationLimit sendSms = new OperationLimit(5, 1440);

        /**
         * 设备短信发送限流配置
         */
        private OperationLimit sendSmsDevice = new OperationLimit(10, 1440);

        /**
         * 手机号登录尝试限流配置
         */
        private OperationLimit loginAttempt = new OperationLimit(10, 1440);

        /**
         * 设备登录尝试限流配置
         */
        private OperationLimit loginAttemptDevice = new OperationLimit(20, 1440);

        /**
         * 手机号换绑操作限流配置
         */
        private OperationLimit phoneChange = new OperationLimit(3, 1440);

        /**
         * 设备换绑操作限流配置
         */
        private OperationLimit phoneChangeDevice = new OperationLimit(5, 1440);
    }

    @Data
    public static class OperationLimit {
        /**
         * 最大尝试次数
         */
        @Min(value = 1, message = "最大尝试次数必须大于0")
        @Max(value = 50, message = "最大尝试次数不能超过50")
        private int maxAttempts;

        /**
         * 时间窗口（分钟）
         */
        @Min(value = 1, message = "时间窗口必须大于0分钟")
        @Max(value = 1440, message = "时间窗口不能超过24小时")
        private int timeWindow;

        public OperationLimit() {}

        public OperationLimit(int maxAttempts, int timeWindow) {
            this.maxAttempts = maxAttempts;
            this.timeWindow = timeWindow;
        }
    }

    @Data
    public static class Restriction {
        /**
         * 超限后限制时长（小时）
         */
        @Min(value = 1, message = "限制时长必须大于0小时")
        @Max(value = 720, message = "限制时长不能超过30天")
        private int durationHours = 24;
    }
}
