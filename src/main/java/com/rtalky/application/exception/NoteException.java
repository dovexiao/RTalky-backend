package com.rtalky.application.exception;

import lombok.Getter;

/**
 * 笔记业务异常
 */
@Getter
public class NoteException extends RuntimeException {

    private final NoteErrorCode errorCode;

    public NoteException(NoteErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public NoteException(NoteErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 笔记错误码枚举
     */
    public enum NoteErrorCode {
        NOTE_NOT_FOUND("NOTE_001", "笔记不存在"),
        NOTE_ACCESS_DENIED("NOTE_002", "无权限操作该笔记"),
        NOTE_TITLE_INVALID("NOTE_003", "笔记标题格式不正确"),
        NOTE_CONTENT_INVALID("NOTE_004", "笔记内容格式不正确"),
        NOTE_DESCRIPTION_INVALID("NOTE_005", "笔记简介格式不正确"),
        NOTE_TAGS_INVALID("NOTE_006", "标签格式不正确"),
        NOTE_COUNT_EXCEEDED("NOTE_007", "笔记数量已达上限"),
        NOTE_ALREADY_DELETED("NOTE_008", "笔记已被删除"),
        NOTE_UPDATE_NO_CHANGES("NOTE_009", "没有实质修改内容"),
        NOTE_TITLE_REQUIRED("NOTE_010", "笔记标题不能为空"),
        NOTE_CONTENT_REQUIRED("NOTE_011", "笔记内容不能为空");

        private final String code;
        private final String message;

        NoteErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
