package com.rtalky.infrastructure.db.repository;

import com.rtalky.domain.model.Note;
import com.rtalky.domain.repository.NoteRepository;
import com.rtalky.infrastructure.db.repository.jpa.JpaNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 笔记仓储实现
 * 基于JPA的笔记数据持久化实现
 */
@Repository
@RequiredArgsConstructor
public class NoteRepositoryImpl implements NoteRepository {

    private final JpaNoteRepository jpaNoteRepository;

    @Override
    public Optional<Note> findByNoteId(String noteId) {
        // TODO: 实现根据笔记ID查找笔记
        return jpaNoteRepository.findByNoteId(noteId);
    }

    @Override
    public Page<Note> findByUserIdAndDeletedFalse(String userId, Pageable pageable) {
        // TODO: 实现分页查询用户笔记列表
        return jpaNoteRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    @Override
    public Page<Note> findByUserIdAndTagsContainingAndDeletedFalse(String userId, String tag, Pageable pageable) {
        // TODO: 实现根据标签分页查询用户笔记
        return jpaNoteRepository.findByUserIdAndTagsContainingAndDeletedFalse(userId, tag, pageable);
    }

    @Override
    public Note save(Note note) {
        // TODO: 实现保存笔记
        return jpaNoteRepository.save(note);
    }

    @Override
    public void deleteByNoteId(String noteId) {
        // TODO: 实现删除笔记（软删除）
        jpaNoteRepository.deleteByNoteId(noteId);
    }

    @Override
    public boolean existsByNoteId(String noteId) {
        // TODO: 实现检查笔记是否存在
        return jpaNoteRepository.existsByNoteId(noteId);
    }

    @Override
    public long countByUserIdAndDeletedFalse(String userId) {
        // TODO: 实现统计用户笔记数量
        return jpaNoteRepository.countByUserIdAndDeletedFalse(userId);
    }

    @Override
    public Optional<Note> findByUserIdAndNoteIdAndDeletedFalse(String userId, String noteId) {
        // TODO: 实现根据用户ID和笔记ID查找笔记
        return jpaNoteRepository.findByUserIdAndNoteIdAndDeletedFalse(userId, noteId);
    }

    @Override
    public Page<Note> findByUserIdAndTitleContainingAndDeletedFalse(String userId, String title, Pageable pageable) {
        // TODO: 实现根据标题模糊查询用户笔记
        return jpaNoteRepository.findByUserIdAndTitleContainingAndDeletedFalse(userId, title, pageable);
    }

    @Override
    public Page<Note> findByUserIdAndContentContainingAndDeletedFalse(String userId, String content, Pageable pageable) {
        // TODO: 实现根据内容模糊查询用户笔记
        return jpaNoteRepository.findByUserIdAndContentContainingAndDeletedFalse(userId, content, pageable);
    }

    @Override
    public Optional<Note> findByDisplayId(String displayId) {
        // TODO: 实现根据displayId查找笔记
        return jpaNoteRepository.findByDisplayId(displayId);
    }

    @Override
    public boolean existsByDisplayId(String displayId) {
        // TODO: 实现检查displayId是否存在
        return jpaNoteRepository.existsByDisplayId(displayId);
    }
}
