package com.rtalky.domain.service;

import java.util.List;

/**
 * 笔记领域服务接口
 * 处理核心笔记业务逻辑，不依赖外部实现细节
 */
public interface NoteService {

    /**
     * 验证笔记标题
     *
     * @param title 笔记标题
     * @return 验证是否通过
     */
    boolean validateTitle(String title);

    /**
     * 验证笔记内容
     *
     * @param content 笔记内容
     * @return 验证是否通过
     */
    boolean validateContent(String content);

    /**
     * 验证笔记简介
     *
     * @param description 笔记简介
     * @return 验证是否通过
     */
    boolean validateDescription(String description);

    /**
     * 验证标签列表
     *
     * @param tags 标签列表
     * @return 验证是否通过
     */
    boolean validateTags(List<String> tags);

    /**
     * 检查用户是否有权限操作笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 是否有权限
     */
    boolean hasPermission(String userId, String noteId);

    /**
     * 检查笔记是否存在且未删除
     *
     * @param noteId 笔记ID
     * @return 是否存在
     */
    boolean noteExists(String noteId);

    /**
     * 检查用户笔记数量是否超限
     *
     * @param userId 用户ID
     * @return 是否超限
     */
    boolean isNoteCountExceeded(String userId);
}
