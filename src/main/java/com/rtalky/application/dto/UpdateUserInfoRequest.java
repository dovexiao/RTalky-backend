package com.rtalky.application.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 * 所有字段都是可选的，支持部分更新
 */
@Data
public class UpdateUserInfoRequest {
    
    @Size(max = 10, message = "昵称长度不能超过10个字符")
    private String nickname;
    
    @Size(max = 50, message = "个人简介长度不能超过50个字符")
    private String bio;
    
    @Size(max = 500000, message = "头像base64数据过大，请选择较小的图片（建议小于375KB）")
    private String avatar;

    /**
     * 主题偏好：LIGHT/DARK/SYSTEM
     */
    private String theme;
}
