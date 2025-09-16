package com.rtalky.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密码操作响应DTO
 */
@Data
public class PasswordResponse {
    
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    
    public PasswordResponse(boolean success, String message, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
    }
}
