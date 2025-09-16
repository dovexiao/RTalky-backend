package com.rtalky.application.service;

import com.rtalky.application.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * 笔记应用服务接口
 * 协调领域服务和基础设施，处理笔记业务流程
 */
public interface NoteAppService {

    /**
     * 分页获取笔记列表
     *
     * @param sessionToken 会话令牌
     * @param page 页码
     * @param size 每页大小
     * @return 笔记分页列表
     */
    NotePageResponse getNoteList(String sessionToken, int page, int size);

    /**
     * 创建笔记
     *
     * @param sessionToken 会话令牌
     * @param request 创建笔记请求
     * @return 笔记详情
     */
    NoteResponse createNote(String sessionToken, CreateNoteRequest request);

    /**
     * 重命名笔记
     *
     * @param sessionToken 会话令牌
     * @param noteId 笔记ID
     * @param request 重命名请求
     * @return 笔记详情
     */
    NoteResponse renameNote(String sessionToken, String noteId, RenameNoteRequest request);

    /**
     * 修改笔记简介
     *
     * @param sessionToken 会话令牌
     * @param noteId 笔记ID
     * @param request 更新简介请求
     * @return 笔记详情
     */
    NoteResponse updateNoteDescription(String sessionToken, String noteId, UpdateDescriptionRequest request);

    /**
     * 删除笔记
     *
     * @param sessionToken 会话令牌
     * @param noteId 笔记ID
     * @return 删除是否成功
     */
    boolean deleteNote(String sessionToken, String noteId);

    /**
     * 修改笔记（完整更新）
     *
     * @param sessionToken 会话令牌
     * @param noteId 笔记ID
     * @param request 更新笔记请求
     * @return 笔记详情
     */
    NoteResponse updateNote(String sessionToken, String noteId, UpdateNoteRequest request);

    /**
     * 获取笔记详情
     *
     * @param sessionToken 会话令牌
     * @param noteId 笔记ID
     * @return 笔记详情
     */
    NoteResponse getNoteDetail(String sessionToken, String noteId);

    /**
     * 根据标签分页获取笔记列表
     *
     * @param sessionToken 会话令牌
     * @param tag 标签名称
     * @param page 页码
     * @param size 每页大小
     * @return 笔记分页列表
     */
    NotePageResponse getNoteListByTag(String sessionToken, String tag, int page, int size);

    /**
     * 根据标题搜索笔记
     *
     * @param sessionToken 会话令牌
     * @param title 标题关键词
     * @param page 页码
     * @param size 每页大小
     * @return 笔记分页列表
     */
    NotePageResponse searchNotesByTitle(String sessionToken, String title, int page, int size);

    /**
     * 根据内容搜索笔记
     *
     * @param sessionToken 会话令牌
     * @param content 内容关键词
     * @param page 页码
     * @param size 每页大小
     * @return 笔记分页列表
     */
    NotePageResponse searchNotesByContent(String sessionToken, String content, int page, int size);
}
