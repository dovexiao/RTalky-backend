package com.rtalky.domain.event;

import com.rtalky.domain.model.AuthMethod;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户登录成功领域事件
 * 当用户成功登录时发布此事件
 */
@Getter
public class UserLoggedInEvent {
    private final String userId;
    private final AuthMethod authMethod;
    private final LocalDateTime loginTime;
    private final String deviceFingerprint;
    private final String ipAddress;

    public UserLoggedInEvent(String userId, AuthMethod authMethod, LocalDateTime loginTime,
                             String deviceFingerprint, String ipAddress) {
        this.userId = userId;
        this.authMethod = authMethod;
        this.loginTime = loginTime;
        this.deviceFingerprint = deviceFingerprint;
        this.ipAddress = ipAddress;
    }
}
