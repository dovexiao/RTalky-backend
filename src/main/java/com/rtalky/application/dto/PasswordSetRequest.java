package com.rtalky.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码设置请求DTO（首次补设/重置密码）
 */
@Data
public class PasswordSetRequest {

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;
}
