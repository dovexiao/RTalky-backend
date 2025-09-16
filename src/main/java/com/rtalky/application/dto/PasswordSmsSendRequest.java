package com.rtalky.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码设置短信验证码发送请求DTO
 */
@Data
public class PasswordSmsSendRequest {

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "设备信息不能为空")
    private String deviceInfo;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
