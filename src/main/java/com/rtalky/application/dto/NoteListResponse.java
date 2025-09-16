package com.rtalky.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记列表响应DTO
 */
@Data
public class NoteListResponse {

    private String noteId;
    private String displayId;
    private String title;
    private String description;
    private List<String> tags;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
