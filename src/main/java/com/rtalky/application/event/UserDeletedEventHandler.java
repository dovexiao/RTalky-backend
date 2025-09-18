package com.rtalky.application.event;

import com.rtalky.domain.event.UserDeletedEvent;
import com.rtalky.domain.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户注销事件处理器
 * 异步处理用户注销后的资源清理工作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedEventHandler {

    private final NoteRepository noteRepository;

    /**
     * 处理用户注销事件
     * 异步执行，避免影响主流程性能
     */
    @EventListener
    @Async
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        try {
            log.info("开始处理用户注销事件: userId={}, phoneNumber={}, deletionId={}", 
                event.getUserId(), event.getPhoneNumber(), event.getDeletionId());

            // 1) 批量软删除该用户的所有笔记（幂等）
            noteRepository.softDeleteByUserId(event.getUserId());
            log.info("已批量软删除用户笔记: userId={}", event.getUserId());

            // 2) 预留：清理与笔记相关缓存/索引（当前项目未实现缓存/索引，保留占位）
            // TODO: 清理笔记列表、计数等缓存键（按实际CacheService命名约定）
            // TODO: 下线搜索/推荐索引中的该用户笔记

            // 3) 保留其他资源清理占位（原注释）
            // TODO: 清理用户相关的其他缓存/第三方绑定/关系数据/风控/定时任务等

            log.info("用户注销事件处理完成: userId={}, phoneNumber={}, deletionId={}", 
                event.getUserId(), event.getPhoneNumber(), event.getDeletionId());

        } catch (Exception e) {
            log.error("处理用户注销事件异常: userId={}, phoneNumber={}, deletionId={}, error={}", 
                event.getUserId(), event.getPhoneNumber(), event.getDeletionId(), e.getMessage(), e);
            // 注意：事件处理异常不应影响主流程，只记录日志
        }
    }
}
