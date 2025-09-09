package com.rtalky.application.event;

import com.rtalky.domain.event.LoginFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录失败事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailedEventHandler {

    /**
     * 处理登录失败事件 - 风险控制
     */
    @EventListener
    public void handleRiskControl(LoginFailedEvent event) {
        try {
            log.warn("登录失败风险控制: phoneNumber={}, authMethod={}, reason={}",
                    event.getPhoneNumber(), event.getAuthMethod(), event.getFailureReason());

            // 记录失败次数并检查是否需要限制
            // riskControlService.recordFailedAttempt(event.getPhoneNumber(),
            //                                     event.getDeviceFingerprint(), event.getAuthMethod());

        } catch (Exception e) {
            log.error("风险控制处理失败: phoneNumber={}", event.getPhoneNumber(), e);
        }
    }

    /**
     * 处理登录失败事件 - 告警通知
     */
    @Async
    @EventListener
    public void handleAlertNotification(LoginFailedEvent event) {
        try {
            // 只有特定类型的失败才发送告警
            if (shouldSendAlert(event)) {
                log.warn("发送登录失败告警: phoneNumber={}, reason={}",
                        event.getPhoneNumber(), event.getFailureReason());

                // 发送告警通知
                // alertService.sendLoginFailureAlert(event.getPhoneNumber(),
                //                                 event.getAuthMethod(), event.getFailureReason(),
                //                                 event.getIpAddress(), event.getDeviceFingerprint());
            }
        } catch (Exception e) {
            log.error("告警通知处理失败: phoneNumber={}", event.getPhoneNumber(), e);
        }
    }

    /**
     * 判断是否需要发送告警
     */
    private boolean shouldSendAlert(LoginFailedEvent event) {
        // 根据失败原因判断是否需要告警
        return event.getFailureReason() != null &&
                (event.getFailureReason().contains("密码错误") ||
                        event.getFailureReason().contains("验证码错误") ||
                        event.getFailureReason().contains("设备限制"));
    }
}
