package com.rtalky.application.event;

import com.rtalky.domain.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户注册事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventHandler {

    /**
     * 处理用户注册成功事件 - 发送欢迎消息
     */
    @Async
    @EventListener
    public void handleWelcomeMessage(UserRegisteredEvent event) {
        try {
            log.info("发送欢迎消息给新用户: userId={}, phoneNumber={}",
                    event.getUserId(), event.getPhoneNumber());

            // 模拟发送欢迎短信
            // smsClient.sendWelcomeMessage(event.getPhoneNumber(), event.getUserId());

            log.info("欢迎消息发送成功: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("发送欢迎消息失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户注册成功事件 - 初始化用户资料
     */
    @Async
    @EventListener
    public void handleProfileInitialization(UserRegisteredEvent event) {
        try {
            log.info("初始化用户资料: userId={}", event.getUserId());

            // 初始化默认昵称（手机号脱敏）
            String maskedPhone = event.getPhoneNumber().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            // userRepository.updateNickname(event.getUserId(), "用户_" + maskedPhone);

            log.info("用户资料初始化完成: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("初始化用户资料失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户注册成功事件 - 注册统计
     */
    @EventListener
    public void handleRegistrationStatistics(UserRegisteredEvent event) {
        try {
            log.info("记录用户注册统计: userId={}, authMethod={}",
                    event.getUserId(), event.getAuthMethod());

            // 记录注册渠道统计
            // statisticsService.recordRegistration(event.getAuthMethod());

        } catch (Exception e) {
            log.error("记录注册统计失败: userId={}", event.getUserId(), e);
        }
    }
}
