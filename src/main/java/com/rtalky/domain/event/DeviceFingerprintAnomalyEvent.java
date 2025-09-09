package com.rtalky.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 设备指纹异常领域事件
 * 当检测到可疑的设备指纹行为时发布此事件
 */
@Getter
public class DeviceFingerprintAnomalyEvent {
    private final String deviceFingerprint;
    private final String phoneNumber;
    private final LocalDateTime detectedTime;
    private final String anomalyType;
    private final String details;

    public DeviceFingerprintAnomalyEvent(String deviceFingerprint, String phoneNumber,
                                         LocalDateTime detectedTime, String anomalyType, String details) {
        this.deviceFingerprint = deviceFingerprint;
        this.phoneNumber = phoneNumber;
        this.detectedTime = detectedTime;
        this.anomalyType = anomalyType;
        this.details = details;
    }
}
