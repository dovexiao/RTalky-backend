package com.rtalky.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 手机号换绑成功事件
 */
@Getter
public class PhoneChangedEvent extends ApplicationEvent {
    
    private final String userId;
    private final String oldPhoneNumber;
    private final String newPhoneNumber;
    private final LocalDateTime changeTime;
    private final String deviceFingerprint;

    public PhoneChangedEvent(Object source, String userId, String oldPhoneNumber, String newPhoneNumber, 
                           LocalDateTime changeTime, String deviceFingerprint) {
        super(source);
        this.userId = userId;
        this.oldPhoneNumber = oldPhoneNumber;
        this.newPhoneNumber = newPhoneNumber;
        this.changeTime = changeTime;
        this.deviceFingerprint = deviceFingerprint;
    }
}
