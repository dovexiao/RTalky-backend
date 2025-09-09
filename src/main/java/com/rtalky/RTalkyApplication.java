package com.rtalky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.rtalky.domain.repository")
@EntityScan(basePackages = "com.rtalky.domain.model") // 添加实体扫描
@EnableConfigurationProperties // 启用配置属性扫描
public class RTalkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(RTalkyApplication.class, args);
    }
}
