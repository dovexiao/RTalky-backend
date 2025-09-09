package com.rtalky.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 短信验证码发送领域事件
 * 当短信验证码成功发送时发布此事件
 */
@Getter
public class SmsCodeSentEvent {
    private final String phoneNumber;
    private final LocalDateTime sendTime;
    private final String deviceFingerprint;

    public SmsCodeSentEvent(String phoneNumber, LocalDateTime sendTime, String deviceFingerprint) {
        this.phoneNumber = phoneNumber;
        this.sendTime = sendTime;
        this.deviceFingerprint = deviceFingerprint;
    }
}
