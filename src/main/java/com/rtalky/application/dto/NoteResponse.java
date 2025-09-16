package com.rtalky.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情响应DTO
 */
@Data
public class NoteResponse {

    private String noteId;
    private String displayId;
    private String title;
    private String description;
    private String content;
    private List<String> tags;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
