package com.rtalky.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信发送响应DTO
 */
@Data
public class SmsSendResponse {
    
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private Integer codeDigits; // 验证码位数
    
    public SmsSendResponse(boolean success, String message, LocalDateTime timestamp, Integer codeDigits) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
        this.codeDigits = codeDigits;
    }
}
