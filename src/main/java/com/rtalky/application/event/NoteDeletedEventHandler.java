package com.rtalky.application.event;

import com.rtalky.domain.event.NoteDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 笔记删除事件处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteDeletedEventHandler {

    /**
     * 处理笔记删除事件 - 记录统计
     */
    @EventListener
    public void handleNoteStatistics(NoteDeletedEvent event) {
        try {
            log.info("记录笔记删除统计: noteId={}, displayId={}, userId={}, title={}", 
                    event.getNoteId(), event.getDisplayId(), event.getUserId(), event.getTitle());

            // TODO: 实现笔记删除统计逻辑
            // noteStatisticsService.recordNoteDeleted(event.getUserId(), event.getNoteId());

        } catch (Exception e) {
            log.error("记录笔记删除统计失败: noteId={}", event.getNoteId(), e);
        }
    }

    /**
     * 处理笔记删除事件 - 更新搜索索引
     */
    @Async
    @EventListener
    public void handleSearchIndexUpdate(NoteDeletedEvent event) {
        try {
            log.info("从搜索索引中移除笔记: noteId={}, displayId={}, userId={}", event.getNoteId(), event.getDisplayId(), event.getUserId());

            // TODO: 实现搜索索引更新逻辑
            // searchIndexService.removeNoteFromIndex(event.getNoteId(), event.getUserId());

        } catch (Exception e) {
            log.error("从搜索索引中移除笔记失败: noteId={}", event.getNoteId(), e);
        }
    }

    /**
     * 处理笔记删除事件 - 清理相关数据
     */
    @Async
    @EventListener
    public void handleDataCleanup(NoteDeletedEvent event) {
        try {
            log.info("清理笔记相关数据: noteId={}, displayId={}, userId={}", event.getNoteId(), event.getDisplayId(), event.getUserId());

            // TODO: 实现数据清理逻辑
            // dataCleanupService.cleanupNoteData(event.getNoteId(), event.getUserId());

        } catch (Exception e) {
            log.error("清理笔记相关数据失败: noteId={}", event.getNoteId(), e);
        }
    }
}
