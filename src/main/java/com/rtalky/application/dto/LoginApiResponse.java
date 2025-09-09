package com.rtalky.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录API响应DTO
 * 统一包装登录响应，保持与其他接口的一致性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginApiResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private LoginResponse data; // 登录数据
}
