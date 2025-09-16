package com.rtalky.infrastructure.db.repository.jpa;

import com.rtalky.domain.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 笔记JPA仓储接口
 * 基于Spring Data JPA的笔记数据访问接口
 */
@Repository
public interface JpaNoteRepository extends JpaRepository<Note, Long> {

    /**
     * 根据笔记ID查找笔记
     */
    Optional<Note> findByNoteId(String noteId);

    /**
     * 分页查询用户笔记列表（未删除）
     */
    Page<Note> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * 根据标签分页查询用户笔记（未删除）
     * 暂时使用简单的LIKE查询，后续可以优化为JSON查询
     */
    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.deleted = false AND n.tags LIKE %:tag%")
    Page<Note> findByUserIdAndTagsContainingAndDeletedFalse(@Param("userId") String userId, 
                                                           @Param("tag") String tag, 
                                                           Pageable pageable);

    /**
     * 检查笔记是否存在
     */
    boolean existsByNoteId(String noteId);

    /**
     * 统计用户笔记数量（未删除）
     */
    long countByUserIdAndDeletedFalse(String userId);

    /**
     * 根据用户ID和笔记ID查找笔记（未删除）
     */
    Optional<Note> findByUserIdAndNoteIdAndDeletedFalse(String userId, String noteId);

    /**
     * 根据标题模糊查询用户笔记（未删除）
     */
    Page<Note> findByUserIdAndTitleContainingAndDeletedFalse(String userId, String title, Pageable pageable);

    /**
     * 根据内容模糊查询用户笔记（未删除）
     */
    Page<Note> findByUserIdAndContentContainingAndDeletedFalse(String userId, String content, Pageable pageable);

    /**
     * 软删除笔记
     */
    @Modifying
    @Query("UPDATE Note n SET n.deleted = true, n.updatedTime = CURRENT_TIMESTAMP WHERE n.noteId = :noteId")
    void deleteByNoteId(@Param("noteId") String noteId);

    /**
     * 根据displayId查找笔记
     */
    Optional<Note> findByDisplayId(String displayId);

    /**
     * 检查displayId是否存在
     */
    boolean existsByDisplayId(String displayId);
}
