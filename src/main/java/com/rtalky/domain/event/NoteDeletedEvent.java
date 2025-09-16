package com.rtalky.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记删除事件
 */
@Getter
@RequiredArgsConstructor
public class NoteDeletedEvent {
    
    private final String noteId;
    private final String displayId;
    private final String userId;
    private final String title;
    private final LocalDateTime deletedTime;
}
