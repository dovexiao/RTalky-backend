package com.rtalky.application.event;

import com.rtalky.domain.event.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户登录事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoggedInEventHandler {

    /**
     * 处理用户登录成功事件 - 审计日志
     */
    @EventListener
    public void handleLoginAudit(UserLoggedInEvent event) {
        try {
            log.info("记录登录审计日志: userId={}, authMethod={}, deviceFingerprint={}",
                    event.getUserId(), event.getAuthMethod(), event.getDeviceFingerprint());

            // 记录详细的登录审计信息
            // auditService.logLogin(event.getUserId(), event.getAuthMethod(),
            //                     event.getLoginTime(), event.getIpAddress(),
            //                     event.getDeviceFingerprint());

        } catch (Exception e) {
            log.error("记录登录审计日志失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户登录成功事件 - 登录统计
     */
    @Async
    @EventListener
    public void handleLoginStatistics(UserLoggedInEvent event) {
        try {
            log.info("更新用户登录统计: userId={}", event.getUserId());

            // 更新用户最后登录时间
            // userRepository.updateLastLoginTime(event.getUserId());

            // 记录登录次数统计
            // statisticsService.recordLogin(event.getUserId(), event.getAuthMethod());

        } catch (Exception e) {
            log.error("更新登录统计失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 处理用户登录成功事件 - 安全监控
     */
    @Async
    @EventListener
    public void handleSecurityMonitoring(UserLoggedInEvent event) {
        try {
            log.info("执行安全监控检查: userId={}, ip={}, device={}",
                    event.getUserId(), event.getIpAddress(), event.getDeviceFingerprint());

            // 检查异常登录行为
            // securityService.checkSuspiciousLogin(event.getUserId(), event.getIpAddress(),
            //                                    event.getDeviceFingerprint(), event.getLoginTime());

        } catch (Exception e) {
            log.error("安全监控检查失败: userId={}", event.getUserId(), e);
        }
    }
}
