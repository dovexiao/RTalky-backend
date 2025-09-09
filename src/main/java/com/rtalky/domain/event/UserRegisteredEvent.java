package com.rtalky.domain.event;

import com.rtalky.domain.model.User;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户注册成功领域事件
 * 当新用户完成注册时发布此事件
 */
@Getter
public class UserRegisteredEvent {
    private final String userId;
    private final String phoneNumber;
    private final LocalDateTime registrationTime;
    private final String authMethod;

    public UserRegisteredEvent(User user) {
        this.userId = user.getUserId();
        this.phoneNumber = user.getPhoneNumber();
        this.registrationTime = user.getRegistrationTime();
        this.authMethod = user.getAuthMethod().name();
    }
}
