package com.rtalky.application.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 笔记分页响应DTO
 */
@Data
public class NotePageResponse {

    private List<NoteListResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    public static NotePageResponse from(Page<NoteListResponse> page) {
        NotePageResponse response = new NotePageResponse();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }
}
