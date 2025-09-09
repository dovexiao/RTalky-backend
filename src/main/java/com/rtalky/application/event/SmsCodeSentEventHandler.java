package com.rtalky.application.event;

import com.rtalky.domain.event.SmsCodeSentEvent;
import com.rtalky.domain.service.SmsStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 短信发送事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsCodeSentEventHandler {

    private final SmsStatisticsService smsStatisticsService;

    /**
     * 处理短信发送事件 - 发送统计
     */
    @EventListener
    public void handleSendStatistics(SmsCodeSentEvent event) {
        try {
            log.info("记录短信发送统计: phoneNumber={}, deviceFingerprint={}",
                    event.getPhoneNumber(), event.getDeviceFingerprint());

            // 记录短信发送成功统计
            smsStatisticsService.recordSmsSuccess(event.getPhoneNumber(), event.getDeviceFingerprint());

        } catch (Exception e) {
            log.error("短信发送统计记录失败: phoneNumber={}", event.getPhoneNumber(), e);
        }
    }

    /**
     * 处理短信发送事件 - 成本核算
     */
    @Async
    @EventListener
    public void handleCostAccounting(SmsCodeSentEvent event) {
        try {
            log.info("短信成本核算: phoneNumber={}, deviceFingerprint={}", 
                    event.getPhoneNumber(), event.getDeviceFingerprint());

            // 成本核算逻辑已在 SmsStatistics 中自动计算
            // 这里可以添加额外的成本分析逻辑

        } catch (Exception e) {
            log.error("短信成本核算失败: phoneNumber={}", event.getPhoneNumber(), e);
        }
    }
}
