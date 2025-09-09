package com.rtalky.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 异步任务执行器配置
    // 可以使用默认配置，也可以自定义线程池
}
