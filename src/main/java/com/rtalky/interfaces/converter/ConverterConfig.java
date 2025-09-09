package com.rtalky.interfaces.converter;

import com.rtalky.infrastructure.device.DeviceInfoParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 转换器配置
 */
@Configuration
public class ConverterConfig {

//    private final DeviceInfoParser deviceInfoParser;
//
//    public ConverterConfig(DeviceInfoParser deviceInfoParser) {
//        this.deviceInfoParser = deviceInfoParser;
//    }

    @Bean
    public LoginConverter loginConverter() {
        return LoginConverter.INSTANCE;
    }

//    @Bean
//    public DeviceInfoConverter deviceInfoConverter() {
//        return new DeviceInfoConverter(deviceInfoParser);
//    }

    @Bean
    public AuthParameterConverter authParameterConverter() {
        return new AuthParameterConverter();
    }

    // ResponseBuilder 现在通过 @Component("rtalkyResponseBuilder") 自动注册
    // 不需要手动创建 Bean

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
