package com.rtalky.infrastructure.device.config;

import com.rtalky.infrastructure.device.DeviceFingerprintService;
import com.rtalky.infrastructure.device.DeviceInfoParser;
import com.rtalky.infrastructure.device.impl.DeviceFingerprintServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 设备指纹服务配置
 */
@Configuration
public class DeviceFingerprintConfig {

    @Bean
    public DeviceFingerprintService deviceFingerprintService(RedisTemplate<String, String> redisTemplate,
                                                           DeviceFingerprintProperties properties) {
        return new DeviceFingerprintServiceImpl(redisTemplate, properties);
    }

    @Bean
    public DeviceInfoParser deviceInfoParser() {
        return new DeviceInfoParser();
    }
}
