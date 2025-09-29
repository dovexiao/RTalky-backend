package com.rtalky.domain.service.impl;

import com.rtalky.domain.repository.NoteRepository;
import com.rtalky.domain.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 笔记领域服务实现
 * 实现笔记相关的业务逻辑验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    // 笔记数量限制
    private static final long MAX_NOTE_COUNT = 1000L;

    @Override
    public boolean validateTitle(String title) {
        // TODO: 实现标题验证逻辑
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        return title.length() <= 20;
    }

    @Override
    public boolean validateContent(String content) {
        // TODO: 实现内容验证逻辑
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        // 内容字数不限，但可以设置最大长度限制
        return content.length() <= 100000; // 10万字符限制
    }

    @Override
    public boolean validateDescription(String description) {
        // TODO: 实现简介验证逻辑
        if (description == null) {
            return true; // 简介可以为空
        }
        return description.length() <= 50;
    }

    @Override
    public boolean validateTags(List<String> tags) {
        // TODO: 实现标签验证逻辑
        if (tags == null) {
            return true; // 标签可以为空
        }

        // 检查标签数量
        if (tags.size() > 20) {
            return false;
        }

        // 检查每个标签的长度
        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                return false; // 标签不能为空
            }
            if (tag.length() > 20) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasPermission(String userId, String noteId) {
        // TODO: 实现权限验证逻辑
        return noteRepository.findByUserIdAndNoteIdAndDeletedFalse(userId, noteId).isPresent();
    }

    @Override
    public boolean noteExists(String noteId) {
        // TODO: 实现笔记存在性检查
        return noteRepository.existsByNoteId(noteId);
    }

    @Override
    public boolean isNoteCountExceeded(String userId) {
        // TODO: 实现笔记数量限制检查
        long currentCount = noteRepository.countByUserIdAndDeletedFalse(userId);
        return currentCount >= MAX_NOTE_COUNT;
    }
}
