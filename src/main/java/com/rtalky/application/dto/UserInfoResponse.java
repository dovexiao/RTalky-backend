package com.rtalky.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private UserInfo data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String userId;
        private String nickname;
        private String bio;
        private String avatar; // base64编码的头像数据
    }
}
