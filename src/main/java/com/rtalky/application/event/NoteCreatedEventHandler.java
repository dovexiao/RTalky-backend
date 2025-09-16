package com.rtalky.application.event;

import com.rtalky.domain.event.NoteCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 笔记创建事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteCreatedEventHandler {

    /**
     * 处理笔记创建事件 - 记录统计
     */
    @EventListener
    public void handleNoteStatistics(NoteCreatedEvent event) {
        try {
            log.info("记录笔记创建统计: noteId={}, displayId={}, userId={}, title={}", 
                    event.getNoteId(), event.getDisplayId(), event.getUserId(), event.getTitle());

            // TODO: 实现笔记创建统计逻辑
            // noteStatisticsService.recordNoteCreated(event.getUserId(), event.getNoteId());

        } catch (Exception e) {
            log.error("记录笔记创建统计失败: noteId={}", event.getNoteId(), e);
        }
    }

    /**
     * 处理笔记创建事件 - 更新搜索索引
     */
    @Async
    @EventListener
    public void handleSearchIndexUpdate(NoteCreatedEvent event) {
        try {
            log.info("更新搜索索引: noteId={}, displayId={}, userId={}", event.getNoteId(), event.getDisplayId(), event.getUserId());

            // TODO: 实现搜索索引更新逻辑
            // searchIndexService.addNoteToIndex(event.getNoteId(), event.getUserId());

        } catch (Exception e) {
            log.error("更新搜索索引失败: noteId={}", event.getNoteId(), e);
        }
    }

    /**
     * 处理笔记创建事件 - 发送通知
     */
    @Async
    @EventListener
    public void handleNotification(NoteCreatedEvent event) {
        try {
            log.info("发送笔记创建通知: noteId={}, displayId={}, userId={}", event.getNoteId(), event.getDisplayId(), event.getUserId());

            // TODO: 实现通知发送逻辑
            // notificationService.sendNoteCreatedNotification(event.getUserId(), event.getNoteId());

        } catch (Exception e) {
            log.error("发送笔记创建通知失败: noteId={}", event.getNoteId(), e);
        }
    }
}
