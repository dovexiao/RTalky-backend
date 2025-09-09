package com.rtalky.domain.event;

import com.rtalky.domain.model.AuthMethod;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户登录失败领域事件
 * 当用户登录失败时发布此事件，用于安全审计和风险控制
 */
@Getter
public class LoginFailedEvent {
    private final String phoneNumber;
    private final AuthMethod authMethod;
    private final LocalDateTime attemptTime;
    private final String deviceFingerprint;
    private final String ipAddress;
    private final String failureReason;

    public LoginFailedEvent(String phoneNumber, AuthMethod authMethod, LocalDateTime attemptTime,
                            String deviceFingerprint, String ipAddress, String failureReason) {
        this.phoneNumber = phoneNumber;
        this.authMethod = authMethod;
        this.attemptTime = attemptTime;
        this.deviceFingerprint = deviceFingerprint;
        this.ipAddress = ipAddress;
        this.failureReason = failureReason;
    }
}
