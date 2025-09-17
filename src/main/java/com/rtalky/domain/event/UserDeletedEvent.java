package com.rtalky.domain.event;

import com.rtalky.domain.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 用户注销事件
 * 当用户账号被软删除时发布此事件，用于异步处理相关资源清理
 */
@Getter
public class UserDeletedEvent extends ApplicationEvent {

    private final String userId;
    private final String phoneNumber;
    private final String deviceFingerprint;
    private final String deletionReason;
    private final String deletionId;
    private final LocalDateTime deletionTime;

    public UserDeletedEvent(Object source, User user, String deviceFingerprint) {
        super(source);
        this.userId = user.getUserId();
        this.phoneNumber = user.getPhoneNumber();
        this.deviceFingerprint = deviceFingerprint;
        this.deletionReason = user.getDeletionReason();
        this.deletionId = user.getDeletionId();
        this.deletionTime = user.getDeletionTime();
    }
}
