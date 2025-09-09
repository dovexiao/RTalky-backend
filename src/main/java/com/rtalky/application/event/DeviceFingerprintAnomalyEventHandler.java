package com.rtalky.application.event;

import com.rtalky.domain.event.DeviceFingerprintAnomalyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 设备指纹异常事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceFingerprintAnomalyEventHandler {

    /**
     * 处理设备指纹异常事件 - 安全告警
     */
    @EventListener
    public void handleSecurityAlert(DeviceFingerprintAnomalyEvent event) {
        try {
            log.warn("设备指纹异常安全告警: deviceFingerprint={}, type={}, details={}",
                    event.getDeviceFingerprint(), event.getAnomalyType(), event.getDetails());

            // 发送安全告警
            // securityAlertService.sendDeviceAnomalyAlert(event.getDeviceFingerprint(),
            //                                          event.getPhoneNumber(), event.getAnomalyType(),
            //                                          event.getDetails(), event.getDetectedTime());

        } catch (Exception e) {
            log.error("安全告警处理失败: deviceFingerprint={}", event.getDeviceFingerprint(), e);
        }
    }

    /**
     * 处理设备指纹异常事件 - 风险处置
     */
    @Async
    @EventListener
    public void handleRiskDisposal(DeviceFingerprintAnomalyEvent event) {
        try {
            log.warn("执行设备指纹异常风险处置: deviceFingerprint={}, type={}",
                    event.getDeviceFingerprint(), event.getAnomalyType());

            // 根据异常类型执行相应的风险处置
            switch (event.getAnomalyType()) {
                case "RATE_LIMIT_EXCEEDED":
                    // 限制设备访问
                    // deviceRestrictionService.restrictDevice(event.getDeviceFingerprint(), 24);
                    break;
                case "MULTI_ACCOUNT":
                    // 标记可疑设备
                    // deviceRiskService.markSuspiciousDevice(event.getDeviceFingerprint());
                    break;
                case "FINGERPRINT_TAMPERING":
                    // 永久封禁设备
                    // deviceBanService.banDevicePermanently(event.getDeviceFingerprint());
                    break;
            }

        } catch (Exception e) {
            log.error("风险处置处理失败: deviceFingerprint={}", event.getDeviceFingerprint(), e);
        }
    }
}
