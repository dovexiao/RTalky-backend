package com.rtalky.application.event;

import com.rtalky.domain.event.NoteUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 笔记更新事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteUpdatedEventHandler {

    /**
     * 处理笔记更新事件 - 记录统计
     */
    @EventListener
    public void handleNoteStatistics(NoteUpdatedEvent event) {
        try {
            log.info("记录笔记更新统计: noteId={}, displayId={}, userId={}, title={}", 
                    event.getNoteId(), event.getDisplayId(), event.getUserId(), event.getTitle());

            // TODO: 实现笔记更新统计逻辑
            // noteStatisticsService.recordNoteUpdated(event.getUserId(), event.getNoteId());

        } catch (Exception e) {
            log.error("记录笔记更新统计失败: noteId={}", event.getNoteId(), e);
        }
    }

    /**
     * 处理笔记更新事件 - 更新搜索索引
     */
    @Async
    @EventListener
    public void handleSearchIndexUpdate(NoteUpdatedEvent event) {
        try {
            log.info("更新搜索索引: noteId={}, displayId={}, userId={}", event.getNoteId(), event.getDisplayId(), event.getUserId());

            // TODO: 实现搜索索引更新逻辑
            // searchIndexService.updateNoteInIndex(event.getNoteId(), event.getUserId());

        } catch (Exception e) {
            log.error("更新搜索索引失败: noteId={}", event.getNoteId(), e);
        }
    }

    /**
     * 处理笔记更新事件 - 版本控制
     */
    @Async
    @EventListener
    public void handleVersionControl(NoteUpdatedEvent event) {
        try {
            log.info("记录笔记版本: noteId={}, userId={}", event.getNoteId(), event.getUserId());

            // TODO: 实现版本控制逻辑
            // versionControlService.createNoteVersion(event.getNoteId(), event.getUserId());

        } catch (Exception e) {
            log.error("记录笔记版本失败: noteId={}", event.getNoteId(), e);
        }
    }
}
