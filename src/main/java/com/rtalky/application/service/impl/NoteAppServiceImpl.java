package com.rtalky.application.service.impl;

import com.rtalky.application.dto.*;
import com.rtalky.application.exception.NoteException;
import com.rtalky.application.service.AuthenticationAppService;
import com.rtalky.application.service.NoteAppService;
import com.rtalky.domain.event.NoteCreatedEvent;
import com.rtalky.domain.event.NoteDeletedEvent;
import com.rtalky.domain.event.NoteUpdatedEvent;
import com.rtalky.domain.model.Note;
import com.rtalky.domain.repository.NoteRepository;
import com.rtalky.domain.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 笔记应用服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteAppServiceImpl implements NoteAppService {

    private final NoteRepository noteRepository;
    private final NoteService noteService;
    private final AuthenticationAppService authenticationAppService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public NotePageResponse getNoteList(String sessionToken, int page, int size) {
        // TODO: 实现分页获取笔记列表逻辑
        log.info("获取笔记列表: page={}, size={}", page, size);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 构建分页参数
        Pageable pageable = PageRequest.of(page, size);
        
        // 3. 查询笔记列表
        Page<Note> notePage = noteRepository.findByUserIdAndDeletedFalse(userId, pageable);
        
        // 4. 转换为响应DTO
        Page<NoteListResponse> responsePage = notePage.map(this::convertToNoteListResponse);
        
        return NotePageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public NoteResponse createNote(String sessionToken, CreateNoteRequest request) {
        // TODO: 实现创建笔记逻辑
        log.info("创建笔记: title={}", request.getTitle());
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 验证业务规则
        if (!noteService.validateTitle(request.getTitle())) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_TITLE_INVALID, "笔记标题格式不正确");
        }
        if (!noteService.validateContent(request.getContent())) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_CONTENT_INVALID, "笔记内容格式不正确");
        }
        if (request.getDescription() != null && !noteService.validateDescription(request.getDescription())) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_DESCRIPTION_INVALID, "笔记简介格式不正确");
        }
        if (request.getTags() != null && !noteService.validateTags(request.getTags())) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_TAGS_INVALID, "标签格式不正确");
        }
        
        // 3. 检查笔记数量限制
        if (noteService.isNoteCountExceeded(userId)) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_COUNT_EXCEEDED, "笔记数量已达上限");
        }
        
        // 4. 创建笔记
        Note note = Note.newNote(userId, request.getTitle(), request.getDescription(), 
                                request.getContent(), request.getTags());
        
        // 5. 保存笔记
        Note savedNote = noteRepository.save(note);
        
        // 6. 生成displayId
        savedNote.generateDisplayId();
        savedNote = noteRepository.save(savedNote);
        
        // 7. 发布笔记创建事件
        eventPublisher.publishEvent(new NoteCreatedEvent(
            savedNote.getNoteId(), savedNote.getDisplayId(), savedNote.getUserId(), savedNote.getTitle(), savedNote.getCreatedTime()));
        
        // 8. 返回响应
        return convertToNoteResponse(savedNote);
    }

    @Override
    @Transactional
    public NoteResponse renameNote(String sessionToken, String noteId, RenameNoteRequest request) {
        // TODO: 实现重命名笔记逻辑
        log.info("重命名笔记: noteId={}, newTitle={}", noteId, request.getTitle());
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 验证权限
        if (!noteService.hasPermission(userId, noteId)) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ACCESS_DENIED, "无权限操作该笔记");
        }
        
        // 3. 查找笔记
        Optional<Note> noteOpt = noteRepository.findByNoteId(noteId);
        if (noteOpt.isEmpty()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_NOT_FOUND, "笔记不存在");
        }
        
        Note note = noteOpt.get();
        if (note.isDeleted()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ALREADY_DELETED, "笔记已被删除");
        }
        
        // 4. 更新标题
        note.updateTitle(request.getTitle());
        
        // 5. 保存笔记
        Note savedNote = noteRepository.save(note);
        
        // 6. 发布笔记更新事件
        eventPublisher.publishEvent(new NoteUpdatedEvent(
            savedNote.getNoteId(), savedNote.getDisplayId(), savedNote.getUserId(), savedNote.getTitle(), savedNote.getUpdatedTime()));
        
        // 7. 返回响应
        return convertToNoteResponse(savedNote);
    }

    @Override
    @Transactional
    public NoteResponse updateNoteDescription(String sessionToken, String noteId, UpdateDescriptionRequest request) {
        // TODO: 实现修改笔记简介逻辑
        log.info("修改笔记简介: noteId={}", noteId);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 验证权限
        if (!noteService.hasPermission(userId, noteId)) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ACCESS_DENIED, "无权限操作该笔记");
        }
        
        // 3. 查找笔记
        Optional<Note> noteOpt = noteRepository.findByNoteId(noteId);
        if (noteOpt.isEmpty()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_NOT_FOUND, "笔记不存在");
        }
        
        Note note = noteOpt.get();
        if (note.isDeleted()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ALREADY_DELETED, "笔记已被删除");
        }
        
        // 4. 更新简介
        note.updateDescription(request.getDescription());
        
        // 5. 保存笔记
        Note savedNote = noteRepository.save(note);
        
        // 6. 发布笔记更新事件
        eventPublisher.publishEvent(new NoteUpdatedEvent(
            savedNote.getNoteId(), savedNote.getDisplayId(), savedNote.getUserId(), savedNote.getTitle(), savedNote.getUpdatedTime()));
        
        // 7. 返回响应
        return convertToNoteResponse(savedNote);
    }

    @Override
    @Transactional
    public boolean deleteNote(String sessionToken, String noteId) {
        // TODO: 实现删除笔记逻辑
        log.info("删除笔记: noteId={}", noteId);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 验证权限
        if (!noteService.hasPermission(userId, noteId)) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ACCESS_DENIED, "无权限操作该笔记");
        }
        
        // 3. 查找笔记
        Optional<Note> noteOpt = noteRepository.findByNoteId(noteId);
        if (noteOpt.isEmpty()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_NOT_FOUND, "笔记不存在");
        }
        
        Note note = noteOpt.get();
        if (note.isDeleted()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ALREADY_DELETED, "笔记已被删除");
        }
        
        // 4. 软删除笔记
        note.markAsDeleted();
        
        // 5. 保存笔记
        noteRepository.save(note);
        
        // 6. 发布笔记删除事件
        eventPublisher.publishEvent(new NoteDeletedEvent(
            note.getNoteId(), note.getDisplayId(), note.getUserId(), note.getTitle(), LocalDateTime.now()));
        
        return true;
    }

    @Override
    @Transactional
    public NoteResponse updateNote(String sessionToken, String noteId, UpdateNoteRequest request) {
        // TODO: 实现修改笔记逻辑
        log.info("修改笔记: noteId={}", noteId);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 验证权限
        if (!noteService.hasPermission(userId, noteId)) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ACCESS_DENIED, "无权限操作该笔记");
        }
        
        // 3. 查找笔记
        Optional<Note> noteOpt = noteRepository.findByNoteId(noteId);
        if (noteOpt.isEmpty()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_NOT_FOUND, "笔记不存在");
        }
        
        Note note = noteOpt.get();
        if (note.isDeleted()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ALREADY_DELETED, "笔记已被删除");
        }
        
        boolean hasChanges = false;
        
        // 4. 检查是否有实质修改
        if (request.getTitle() != null && !request.getTitle().equals(note.getTitle())) {
            // 验证标题格式
            if (!noteService.validateTitle(request.getTitle())) {
                throw new NoteException(NoteException.NoteErrorCode.NOTE_TITLE_INVALID, "笔记标题格式不正确");
            }
            note.updateTitle(request.getTitle());
            hasChanges = true;
        }
        
        if (request.getDescription() != null && !request.getDescription().equals(note.getDescription())) {
            // 验证简介格式
            if (!noteService.validateDescription(request.getDescription())) {
                throw new NoteException(NoteException.NoteErrorCode.NOTE_DESCRIPTION_INVALID, "笔记简介格式不正确");
            }
            note.updateDescription(request.getDescription());
            hasChanges = true;
        }
        
        if (request.getContent() != null && !request.getContent().equals(note.getContent())) {
            // 验证内容格式
            if (!noteService.validateContent(request.getContent())) {
                throw new NoteException(NoteException.NoteErrorCode.NOTE_CONTENT_INVALID, "笔记内容格式不正确");
            }
            note.updateContent(request.getContent());
            hasChanges = true;
        }
        
        if (request.getTags() != null && !request.getTags().equals(note.getTagsList())) {
            // 验证标签格式
            if (!noteService.validateTags(request.getTags())) {
                throw new NoteException(NoteException.NoteErrorCode.NOTE_TAGS_INVALID, "标签格式不正确");
            }
            note.updateTags(request.getTags());
            hasChanges = true;
        }
        
        // 5. 如果没有实质修改，返回特殊错误码
        if (!hasChanges) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_UPDATE_NO_CHANGES, "没有实质修改内容");
        }
        
        // 6. 保存笔记
        Note savedNote = noteRepository.save(note);
        
        // 7. 发布笔记更新事件
        eventPublisher.publishEvent(new NoteUpdatedEvent(
            savedNote.getNoteId(), savedNote.getDisplayId(), savedNote.getUserId(), savedNote.getTitle(), savedNote.getUpdatedTime()));
        
        // 8. 返回响应
        return convertToNoteResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteDetail(String sessionToken, String noteId) {
        // TODO: 实现获取笔记详情逻辑
        log.info("获取笔记详情: noteId={}", noteId);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 验证权限
        if (!noteService.hasPermission(userId, noteId)) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ACCESS_DENIED, "无权限操作该笔记");
        }
        
        // 3. 查找笔记
        Optional<Note> noteOpt = noteRepository.findByNoteId(noteId);
        if (noteOpt.isEmpty()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_NOT_FOUND, "笔记不存在");
        }
        
        Note note = noteOpt.get();
        if (note.isDeleted()) {
            throw new NoteException(NoteException.NoteErrorCode.NOTE_ALREADY_DELETED, "笔记已被删除");
        }
        
        // 4. 返回响应
        return convertToNoteResponse(note);
    }

    @Override
    @Transactional(readOnly = true)
    public NotePageResponse getNoteListByTag(String sessionToken, String tag, int page, int size) {
        // TODO: 实现根据标签分页获取笔记列表逻辑
        log.info("根据标签获取笔记列表: tag={}, page={}, size={}", tag, page, size);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 构建分页参数
        Pageable pageable = PageRequest.of(page, size);
        
        // 3. 查询笔记列表
        Page<Note> notePage = noteRepository.findByUserIdAndTagsContainingAndDeletedFalse(userId, tag, pageable);
        
        // 4. 转换为响应DTO
        Page<NoteListResponse> responsePage = notePage.map(this::convertToNoteListResponse);
        
        return NotePageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotePageResponse searchNotesByTitle(String sessionToken, String title, int page, int size) {
        // TODO: 实现根据标题搜索笔记逻辑
        log.info("根据标题搜索笔记: title={}, page={}, size={}", title, page, size);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 构建分页参数
        Pageable pageable = PageRequest.of(page, size);
        
        // 3. 查询笔记列表
        Page<Note> notePage = noteRepository.findByUserIdAndTitleContainingAndDeletedFalse(userId, title, pageable);
        
        // 4. 转换为响应DTO
        Page<NoteListResponse> responsePage = notePage.map(this::convertToNoteListResponse);
        
        return NotePageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotePageResponse searchNotesByContent(String sessionToken, String content, int page, int size) {
        // TODO: 实现根据内容搜索笔记逻辑
        log.info("根据内容搜索笔记: content={}, page={}, size={}", content, page, size);
        
        // 1. 验证会话令牌
        String userId = authenticationAppService.validateSession(sessionToken);
        
        // 2. 构建分页参数
        Pageable pageable = PageRequest.of(page, size);
        
        // 3. 查询笔记列表
        Page<Note> notePage = noteRepository.findByUserIdAndContentContainingAndDeletedFalse(userId, content, pageable);
        
        // 4. 转换为响应DTO
        Page<NoteListResponse> responsePage = notePage.map(this::convertToNoteListResponse);
        
        return NotePageResponse.from(responsePage);
    }

    /**
     * 转换为笔记列表响应DTO
     */
    private NoteListResponse convertToNoteListResponse(Note note) {
        NoteListResponse response = new NoteListResponse();
        response.setNoteId(note.getNoteId());
        response.setDisplayId(note.getDisplayId());
        response.setTitle(note.getTitle());
        response.setDescription(note.getDescription());
        response.setTags(note.getTagsList());
        response.setCreatedTime(note.getCreatedTime());
        response.setUpdatedTime(note.getUpdatedTime());
        return response;
    }

    /**
     * 转换为笔记详情响应DTO
     */
    private NoteResponse convertToNoteResponse(Note note) {
        NoteResponse response = new NoteResponse();
        response.setNoteId(note.getNoteId());
        response.setDisplayId(note.getDisplayId());
        response.setTitle(note.getTitle());
        response.setDescription(note.getDescription());
        response.setContent(note.getContent());
        response.setTags(note.getTagsList());
        response.setCreatedTime(note.getCreatedTime());
        response.setUpdatedTime(note.getUpdatedTime());
        return response;
    }
}
