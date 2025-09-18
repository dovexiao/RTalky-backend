package com.rtalky.domain.repository;

import com.rtalky.domain.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 笔记仓储接口
 * 定义笔记数据的持久化操作，不包含具体实现
 */
public interface NoteRepository {

    /**
     * 根据笔记ID查找笔记
     *
     * @param noteId 笔记ID
     * @return 笔记对象（如果存在）
     */
    Optional<Note> findByNoteId(String noteId);

    /**
     * 分页查询用户笔记列表（未删除）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 笔记分页列表
     */
    Page<Note> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * 根据标签分页查询用户笔记（未删除）
     *
     * @param userId 用户ID
     * @param tag 标签名称
     * @param pageable 分页参数
     * @return 笔记分页列表
     */
    Page<Note> findByUserIdAndTagsContainingAndDeletedFalse(String userId, String tag, Pageable pageable);

    /**
     * 保存笔记
     *
     * @param note 笔记对象
     * @return 保存后的笔记对象
     */
    Note save(Note note);

    /**
     * 删除笔记（软删除）
     *
     * @param noteId 笔记ID
     */
    void deleteByNoteId(String noteId);

    /**
     * 检查笔记是否存在
     *
     * @param noteId 笔记ID
     * @return 是否存在
     */
    boolean existsByNoteId(String noteId);

    /**
     * 统计用户笔记数量（未删除）
     *
     * @param userId 用户ID
     * @return 笔记数量
     */
    long countByUserIdAndDeletedFalse(String userId);

    /**
     * 根据用户ID和笔记ID查找笔记（未删除）
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 笔记对象（如果存在）
     */
    Optional<Note> findByUserIdAndNoteIdAndDeletedFalse(String userId, String noteId);

    /**
     * 根据displayId查找笔记
     *
     * @param displayId 显示ID
     * @return 笔记对象（如果存在）
     */
    Optional<Note> findByDisplayId(String displayId);

    /**
     * 检查displayId是否存在
     *
     * @param displayId 显示ID
     * @return 是否存在
     */
    boolean existsByDisplayId(String displayId);

    /**
     * 根据标题模糊查询用户笔记（未删除）
     *
     * @param userId 用户ID
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 笔记分页列表
     */
    Page<Note> findByUserIdAndTitleContainingAndDeletedFalse(String userId, String title, Pageable pageable);

    /**
     * 根据内容模糊查询用户笔记（未删除）
     *
     * @param userId 用户ID
     * @param content 内容关键词
     * @param pageable 分页参数
     * @return 笔记分页列表
     */
    Page<Note> findByUserIdAndContentContainingAndDeletedFalse(String userId, String content, Pageable pageable);

    /**
     * 按用户ID批量软删除笔记
     * @param userId 用户ID
     */
    void softDeleteByUserId(String userId);
}
