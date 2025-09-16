package com.rtalky.interfaces.rest;

import com.rtalky.application.dto.*;
import com.rtalky.application.exception.SessionValidationException;
import com.rtalky.application.exception.NoteException;
import com.rtalky.application.service.NoteAppService;
import com.rtalky.interfaces.converter.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记REST控制器
 * 处理所有笔记相关的HTTP请求
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "笔记管理", description = "笔记相关接口，包括创建、查询、更新、删除等操作")
public class NoteController {

    private final NoteAppService noteAppService;

    @Qualifier("rtalkyResponseBuilder")
    private final ResponseBuilder responseBuilder;

    /**
     * 分页获取笔记列表
     */
    @GetMapping
    @Operation(
        summary = "分页获取笔记列表",
        description = "分页获取当前用户的笔记列表，支持按创建时间倒序排列"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NotePageResponse.class),
                examples = @ExampleObject(
                    name = "获取成功",
                    value = "{\"content\":[{\"noteId\":\"N123456789\",\"displayId\":\"N001\",\"title\":\"我的笔记\",\"description\":\"笔记简介\",\"tags\":[\"标签1\",\"标签2\"],\"createdTime\":\"2024-01-01T12:00:00\",\"updatedTime\":\"2024-01-01T12:00:00\"}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"hasNext\":false,\"hasPrevious\":false}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "分页参数错误",
                        value = "{\"success\":false,\"message\":\"每页大小不能超过50\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "会话无效",
                        value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"获取笔记列表过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<NotePageResponse> getNoteList(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(description = "页码，从0开始", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小，最大50", example = "20")
        @RequestParam(defaultValue = "20") @Max(50) int size
    ) {
        log.info("获取笔记列表请求: page={}, size={}", page, size);

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNotePageFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNotePageFailureResponse("Authorization头格式无效"));
            }

            NotePageResponse response = noteAppService.getNoteList(sessionToken, page, size);
            return ResponseEntity.ok(responseBuilder.buildNotePageSuccessResponse("获取笔记列表成功", response));

        } catch (SessionValidationException e) {
            log.warn("获取笔记列表业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNotePageFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNotePageFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNotePageFailureResponse(e.getMessage()));
            }
        } catch (Exception e) {
            log.error("获取笔记列表系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNotePageFailureResponse("获取笔记列表过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 创建笔记
     */
    @PostMapping
    @Operation(
        summary = "创建笔记",
        description = "创建新的笔记，包含标题、简介、内容和标签"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NoteResponse.class),
                examples = @ExampleObject(
                    name = "创建成功",
                    value = "{\"noteId\":\"N123456789\",\"displayId\":\"N001\",\"title\":\"我的笔记\",\"description\":\"笔记简介\",\"content\":\"笔记内容\",\"tags\":[\"标签1\",\"标签2\"],\"createdTime\":\"2024-01-01T12:00:00\",\"updatedTime\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                    name = "参数校验失败",
                    value = "{\"success\":false,\"message\":\"笔记标题不能为空\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记数量超限",
                        value = "{\"success\":false,\"message\":\"笔记数量已达上限\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                    name = "会话无效",
                    value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"创建笔记过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<NoteResponse> createNote(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(
            description = "创建笔记请求参数",
            required = true,
            schema = @Schema(implementation = CreateNoteRequest.class)
        )
        @Valid @RequestBody CreateNoteRequest request
    ) {
        log.info("创建笔记请求: title={}", request.getTitle());

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("Authorization头格式无效"));
            }

            NoteResponse response = noteAppService.createNote(sessionToken, request);
            return ResponseEntity.ok(responseBuilder.buildNoteSuccessResponse("创建笔记成功", response));

        } catch (SessionValidationException e) {
            log.warn("创建笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (NoteException e) {
            log.warn("创建笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_TITLE_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_CONTENT_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_DESCRIPTION_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_TAGS_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_TITLE_REQUIRED ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_CONTENT_REQUIRED) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ACCESS_DENIED) {
                // 权限错误，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_COUNT_EXCEEDED) {
                // 业务限制，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.warn("创建笔记参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("创建笔记状态错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("创建笔记系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNoteFailureResponse("创建笔记过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 重命名笔记
     */
    @PutMapping("/{noteId}/title")
    @Operation(
        summary = "重命名笔记",
        description = "修改指定笔记的标题"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "重命名成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NoteResponse.class),
                examples = @ExampleObject(
                    name = "重命名成功",
                    value = "{\"noteId\":\"N123456789\",\"displayId\":\"N001\",\"title\":\"新标题\",\"description\":\"笔记简介\",\"content\":\"笔记内容\",\"tags\":[\"标签1\",\"标签2\"],\"createdTime\":\"2024-01-01T12:00:00\",\"updatedTime\":\"2024-01-01T12:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记ID格式错误",
                        value = "{\"success\":false,\"message\":\"笔记ID格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "标题格式错误",
                        value = "{\"success\":false,\"message\":\"笔记标题格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "会话无效",
                        value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "无权限访问",
                        value = "{\"success\":false,\"message\":\"无权限操作该笔记\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "笔记不存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "笔记不存在",
                        value = "{\"success\":false,\"message\":\"笔记不存在\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记已删除",
                        value = "{\"success\":false,\"message\":\"笔记已被删除\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"重命名笔记过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<NoteResponse> renameNote(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(description = "笔记ID", required = true, example = "N123456789")
        @PathVariable String noteId,
        @Parameter(
            description = "重命名请求参数",
            required = true,
            schema = @Schema(implementation = RenameNoteRequest.class)
        )
        @Valid @RequestBody RenameNoteRequest request
    ) {
        log.info("重命名笔记请求: noteId={}, newTitle={}", noteId, request.getTitle());

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("Authorization头格式无效"));
            }

            // 验证noteId格式
            if (noteId == null || noteId.trim().isEmpty() || !noteId.startsWith("N")) {
                log.warn("笔记ID格式错误: {}", noteId);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("笔记ID格式不正确"));
            }

            NoteResponse response = noteAppService.renameNote(sessionToken, noteId, request);
            return ResponseEntity.ok(responseBuilder.buildNoteSuccessResponse("重命名笔记成功", response));

        } catch (SessionValidationException e) {
            log.warn("重命名笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (NoteException e) {
            log.warn("重命名笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_TITLE_INVALID) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ACCESS_DENIED) {
                // 权限错误，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_NOT_FOUND ||
                       e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ALREADY_DELETED) {
                // 笔记不存在，返回404
                return ResponseEntity.status(404)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.warn("重命名笔记参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.warn("重命名笔记权限错误: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("重命名笔记系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNoteFailureResponse("重命名笔记过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 修改笔记简介
     */
    @PutMapping("/{noteId}/description")
    @Operation(
        summary = "修改笔记简介",
        description = "修改指定笔记的简介"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "修改成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NoteResponse.class),
                examples = @ExampleObject(
                    name = "修改成功",
                    value = "{\"noteId\":\"N123456789\",\"displayId\":\"N001\",\"title\":\"我的笔记\",\"description\":\"新的简介\",\"content\":\"笔记内容\",\"tags\":[\"标签1\",\"标签2\"],\"createdTime\":\"2024-01-01T12:00:00\",\"updatedTime\":\"2024-01-01T12:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记ID格式错误",
                        value = "{\"success\":false,\"message\":\"笔记ID格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "简介格式错误",
                        value = "{\"success\":false,\"message\":\"笔记简介格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "会话无效",
                        value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "无权限访问",
                        value = "{\"success\":false,\"message\":\"无权限操作该笔记\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "笔记不存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "笔记不存在",
                        value = "{\"success\":false,\"message\":\"笔记不存在\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记已删除",
                        value = "{\"success\":false,\"message\":\"笔记已被删除\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"修改笔记简介过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<NoteResponse> updateNoteDescription(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(description = "笔记ID", required = true, example = "N123456789")
        @PathVariable String noteId,
        @Parameter(
            description = "更新简介请求参数",
            required = true,
            schema = @Schema(implementation = UpdateDescriptionRequest.class)
        )
        @Valid @RequestBody UpdateDescriptionRequest request
    ) {
        log.info("修改笔记简介请求: noteId={}", noteId);

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("Authorization头格式无效"));
            }

            // 验证noteId格式
            if (noteId == null || noteId.trim().isEmpty() || !noteId.startsWith("N")) {
                log.warn("笔记ID格式错误: {}", noteId);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("笔记ID格式不正确"));
            }

            NoteResponse response = noteAppService.updateNoteDescription(sessionToken, noteId, request);
            return ResponseEntity.ok(responseBuilder.buildNoteSuccessResponse("修改笔记简介成功", response));

        } catch (SessionValidationException e) {
            log.warn("修改笔记简介业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (NoteException e) {
            log.warn("修改笔记简介业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_DESCRIPTION_INVALID) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ACCESS_DENIED) {
                // 权限错误，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_NOT_FOUND ||
                       e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ALREADY_DELETED) {
                // 笔记不存在，返回404
                return ResponseEntity.status(404)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.warn("修改笔记简介参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.warn("修改笔记简介权限错误: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("修改笔记简介系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNoteFailureResponse("修改笔记简介过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{noteId}")
    @Operation(
        summary = "删除笔记",
        description = "软删除指定的笔记"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "删除成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "删除成功",
                    value = "{\"success\":true,\"message\":\"笔记删除成功\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记ID格式错误",
                        value = "{\"success\":false,\"message\":\"笔记ID格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "会话无效",
                        value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "无权限访问",
                        value = "{\"success\":false,\"message\":\"无权限操作该笔记\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "笔记不存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "笔记不存在",
                        value = "{\"success\":false,\"message\":\"笔记不存在\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记已删除",
                        value = "{\"success\":false,\"message\":\"笔记已被删除\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"删除笔记过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<ResponseBuilder.SimpleResponse> deleteNote(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(description = "笔记ID", required = true, example = "N123456789")
        @PathVariable String noteId
    ) {
        log.info("删除笔记请求: noteId={}", noteId);

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, "缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, "Authorization头格式无效"));
            }

            // 验证noteId格式
            if (noteId == null || noteId.trim().isEmpty() || !noteId.startsWith("N")) {
                log.warn("笔记ID格式错误: {}", noteId);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, "笔记ID格式不正确"));
            }

            boolean success = noteAppService.deleteNote(sessionToken, noteId);
            if (success) {
                return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "笔记删除成功"));
            } else {
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSimpleResponse(false, "笔记删除失败"));
            }

        } catch (SessionValidationException e) {
            log.warn("删除笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            }
        } catch (NoteException e) {
            log.warn("删除笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ACCESS_DENIED) {
                // 权限错误，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_NOT_FOUND ||
                       e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ALREADY_DELETED) {
                // 笔记不存在，返回404
                return ResponseEntity.status(404)
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.warn("删除笔记参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.warn("删除笔记权限错误: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(responseBuilder.buildSimpleResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("删除笔记系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildSimpleResponse(false, "删除笔记过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 修改笔记（完整更新）
     */
    @PutMapping("/{noteId}")
    @Operation(
        summary = "修改笔记",
        description = "修改指定笔记的标题、简介、内容或标签，支持部分字段更新"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "修改成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NoteResponse.class),
                examples = @ExampleObject(
                    name = "修改成功",
                    value = "{\"noteId\":\"N123456789\",\"displayId\":\"N001\",\"title\":\"更新后的标题\",\"description\":\"更新后的简介\",\"content\":\"更新后的内容\",\"tags\":[\"新标签1\",\"新标签2\"],\"createdTime\":\"2024-01-01T12:00:00\",\"updatedTime\":\"2024-01-01T12:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记ID格式错误",
                        value = "{\"success\":false,\"message\":\"笔记ID格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "标题格式错误",
                        value = "{\"success\":false,\"message\":\"笔记标题格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "内容格式错误",
                        value = "{\"success\":false,\"message\":\"笔记内容格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "简介格式错误",
                        value = "{\"success\":false,\"message\":\"笔记简介格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "标签格式错误",
                        value = "{\"success\":false,\"message\":\"标签格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "会话无效",
                        value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "无权限访问",
                        value = "{\"success\":false,\"message\":\"无权限操作该笔记\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "笔记不存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "笔记不存在",
                        value = "{\"success\":false,\"message\":\"笔记不存在\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记已删除",
                        value = "{\"success\":false,\"message\":\"笔记已被删除\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"修改笔记过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<NoteResponse> updateNote(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(description = "笔记ID", required = true, example = "N123456789")
        @PathVariable String noteId,
        @Parameter(
            description = "更新笔记请求参数",
            required = true,
            schema = @Schema(implementation = UpdateNoteRequest.class)
        )
        @Valid @RequestBody UpdateNoteRequest request
    ) {
        log.info("修改笔记请求: noteId={}", noteId);

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("Authorization头格式无效"));
            }

            // 验证noteId格式
            if (noteId == null || noteId.trim().isEmpty() || !noteId.startsWith("N")) {
                log.warn("笔记ID格式错误: {}", noteId);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("笔记ID格式不正确"));
            }

            NoteResponse response = noteAppService.updateNote(sessionToken, noteId, request);
            return ResponseEntity.ok(responseBuilder.buildNoteSuccessResponse("修改笔记成功", response));

        } catch (SessionValidationException e) {
            log.warn("修改笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (NoteException e) {
            log.warn("修改笔记业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_TITLE_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_CONTENT_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_DESCRIPTION_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_TAGS_INVALID ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_UPDATE_NO_CHANGES) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ACCESS_DENIED) {
                // 权限错误，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_NOT_FOUND ||
                       e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ALREADY_DELETED) {
                // 笔记不存在，返回404
                return ResponseEntity.status(404)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.warn("修改笔记参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.warn("修改笔记权限错误: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("修改笔记系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNoteFailureResponse("修改笔记过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/{noteId}")
    @Operation(
        summary = "获取笔记详情",
        description = "获取指定笔记的详细信息，包含完整内容"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NoteResponse.class),
                examples = @ExampleObject(
                    name = "获取成功",
                    value = "{\"noteId\":\"N123456789\",\"displayId\":\"N001\",\"title\":\"我的笔记\",\"description\":\"笔记简介\",\"content\":\"这是笔记的完整内容...\",\"tags\":[\"标签1\",\"标签2\"],\"createdTime\":\"2024-01-01T12:00:00\",\"updatedTime\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "缺少授权头",
                        value = "{\"success\":false,\"message\":\"缺少Authorization头\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "授权头格式错误",
                        value = "{\"success\":false,\"message\":\"Authorization头格式无效\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "笔记ID格式错误",
                        value = "{\"success\":false,\"message\":\"笔记ID格式不正确\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = {
                    @ExampleObject(
                        name = "会话无效",
                        value = "{\"success\":false,\"message\":\"会话令牌无效或已过期\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被禁用",
                        value = "{\"success\":false,\"message\":\"用户已被禁用\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "用户被锁定",
                        value = "{\"success\":false,\"message\":\"用户已被锁定\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    ),
                    @ExampleObject(
                        name = "无权限访问",
                        value = "{\"success\":false,\"message\":\"无权限操作该笔记\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "笔记不存在",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "笔记不存在",
                    value = "{\"success\":false,\"message\":\"笔记不存在\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "请求过于频繁",
                    value = "{\"success\":false,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class),
                examples = @ExampleObject(
                    name = "系统异常",
                    value = "{\"success\":false,\"message\":\"获取笔记详情过程中发生异常，请稍后重试\",\"timestamp\":\"2024-01-01T12:00:00\"}"
                )
            )
        )
    })
    public ResponseEntity<NoteResponse> getNoteDetail(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Parameter(description = "笔记ID", required = true, example = "N123456789")
        @PathVariable String noteId
    ) {
        log.info("获取笔记详情请求: noteId={}", noteId);

        try {
            // 检查Authorization头是否存在
            if (authorization == null || authorization.trim().isEmpty()) {
                log.warn("缺少Authorization头");
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("缺少Authorization头"));
            }

            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                log.warn("Authorization头格式无效: {}", authorization);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("Authorization头格式无效"));
            }

            // 验证noteId格式
            if (noteId == null || noteId.trim().isEmpty() || !noteId.startsWith("N")) {
                log.warn("笔记ID格式错误: {}", noteId);
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse("笔记ID格式不正确"));
            }

            NoteResponse response = noteAppService.getNoteDetail(sessionToken, noteId);
            return ResponseEntity.ok(responseBuilder.buildNoteSuccessResponse("获取笔记详情成功", response));

        } catch (SessionValidationException e) {
            log.warn("获取笔记详情业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_REQUIRED ||
                e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.INVALID_TOKEN_FORMAT) {
                // 客户端错误，返回400
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_EXPIRED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.TOKEN_INVALID ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_NOT_FOUND ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_DISABLED ||
                       e.getErrorCode() == SessionValidationException.SessionValidationErrorCode.USER_LOCKED) {
                // 认证失败，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (NoteException e) {
            log.warn("获取笔记详情业务异常: errorCode={}, message={}", e.getErrorCode().getCode(), e.getMessage());

            // 根据错误码返回不同的HTTP状态码
            if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_NOT_FOUND ||
                e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ALREADY_DELETED) {
                // 笔记不存在，返回404
                return ResponseEntity.status(404)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else if (e.getErrorCode() == NoteException.NoteErrorCode.NOTE_ACCESS_DENIED) {
                // 权限错误，返回401
                return ResponseEntity.status(401)
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            } else {
                // 服务器错误，返回500
                return ResponseEntity.internalServerError()
                        .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
            }
        } catch (IllegalArgumentException e) {
            log.warn("获取笔记详情参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (SecurityException e) {
            log.warn("获取笔记详情权限错误: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(responseBuilder.buildNoteFailureResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("获取笔记详情系统异常: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNoteFailureResponse("获取笔记详情过程中发生异常，请稍后重试"));
        }
    }

    /**
     * 根据标签分页获取笔记列表
     */
    @GetMapping("/search/tag")
    @Operation(
        summary = "根据标签搜索笔记",
        description = "根据指定标签分页获取笔记列表"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "搜索成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NotePageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
            )
        )
    })
    public ResponseEntity<NotePageResponse> getNoteListByTag(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader("Authorization") String authorization,
        @Parameter(description = "标签名称", required = true, example = "工作")
        @RequestParam String tag,
        @Parameter(description = "页码，从0开始", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小，最大50", example = "20")
        @RequestParam(defaultValue = "20") @Max(50) int size
    ) {
        log.info("根据标签搜索笔记请求: tag={}, page={}, size={}", tag, page, size);

        try {
            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNotePageFailureResponse("无效的授权头"));
            }

            NotePageResponse response = noteAppService.getNoteListByTag(sessionToken, tag, page, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("根据标签搜索笔记失败: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNotePageFailureResponse("根据标签搜索笔记失败"));
        }
    }

    /**
     * 根据标题搜索笔记
     */
    @GetMapping("/search/title")
    @Operation(
        summary = "根据标题搜索笔记",
        description = "根据标题关键词分页搜索笔记"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "搜索成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NotePageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
            )
        )
    })
    public ResponseEntity<NotePageResponse> searchNotesByTitle(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader("Authorization") String authorization,
        @Parameter(description = "标题关键词", required = true, example = "会议")
        @RequestParam String title,
        @Parameter(description = "页码，从0开始", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小，最大50", example = "20")
        @RequestParam(defaultValue = "20") @Max(50) int size
    ) {
        log.info("根据标题搜索笔记请求: title={}, page={}, size={}", title, page, size);

        try {
            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNotePageFailureResponse("无效的授权头"));
            }

            NotePageResponse response = noteAppService.searchNotesByTitle(sessionToken, title, page, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("根据标题搜索笔记失败: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNotePageFailureResponse("根据标题搜索笔记失败"));
        }
    }

    /**
     * 根据内容搜索笔记
     */
    @GetMapping("/search/content")
    @Operation(
        summary = "根据内容搜索笔记",
        description = "根据内容关键词分页搜索笔记"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "搜索成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NotePageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "认证失败",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseBuilder.SimpleResponse.class)
            )
        )
    })
    public ResponseEntity<NotePageResponse> searchNotesByContent(
        @Parameter(
            description = "Bearer Token，格式：Bearer {token}",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @RequestHeader("Authorization") String authorization,
        @Parameter(description = "内容关键词", required = true, example = "项目")
        @RequestParam String content,
        @Parameter(description = "页码，从0开始", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小，最大50", example = "20")
        @RequestParam(defaultValue = "20") @Max(50) int size
    ) {
        log.info("根据内容搜索笔记请求: content={}, page={}, size={}", content, page, size);

        try {
            String sessionToken = extractTokenFromHeader(authorization);
            if (sessionToken == null) {
                return ResponseEntity.badRequest()
                        .body(responseBuilder.buildNotePageFailureResponse("无效的授权头"));
            }

            NotePageResponse response = noteAppService.searchNotesByContent(sessionToken, content, page, size);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("根据内容搜索笔记失败: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(responseBuilder.buildNotePageFailureResponse("根据内容搜索笔记失败"));
        }
    }

    /**
     * 从Authorization头中提取令牌
     */
    private String extractTokenFromHeader(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }


}
