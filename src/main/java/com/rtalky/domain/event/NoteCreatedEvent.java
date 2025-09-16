package com.rtalky.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记创建事件
 */
@Getter
@RequiredArgsConstructor
public class NoteCreatedEvent {
    
    private final String noteId;
    private final String displayId;
    private final String userId;
    private final String title;
    private final LocalDateTime createdTime;
}
