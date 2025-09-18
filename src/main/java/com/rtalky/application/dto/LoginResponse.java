package com.rtalky.application.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {

    private String userId;

    private String phoneNumber;

    private String sessionToken;

    private LocalDateTime loginTime;

    private LocalDateTime tokenExpiryTime;

    private UserProfile userProfile;

    @Data
    public static class UserProfile {
        private String nickname;
        private String avatar; // base64编码的头像数据
        private String bio;
        private String theme; // 主题偏好：LIGHT/DARK/SYSTEM
    }
}
