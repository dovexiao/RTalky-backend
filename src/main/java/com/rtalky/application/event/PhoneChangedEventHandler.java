package com.rtalky.application.event;

import com.rtalky.domain.event.PhoneChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 手机号换绑事件处理器
 */
@Component
@Slf4j
public class PhoneChangedEventHandler {

    @Async
    @EventListener
    public void handlePhoneChangedEvent(PhoneChangedEvent event) {
        log.info("异步处理手机号换绑事件: userId={}, oldPhone={}, newPhone={}, changeTime={}",
            event.getUserId(), event.getOldPhoneNumber(), event.getNewPhoneNumber(), event.getChangeTime());

        // TODO:
        // 1. 更新其他相关表的手机号字段
        // 2. 发送换绑成功通知
        // 3. 记录审计日志
        // 4. 清理其他相关缓存
        // 5. 通知第三方系统手机号变更
        // 6. 更新用户资料中的手机号显示

        log.info("手机号换绑事件异步处理完成: userId={}, oldPhone={}, newPhone={}", 
            event.getUserId(), event.getOldPhoneNumber(), event.getNewPhoneNumber());
    }
}
