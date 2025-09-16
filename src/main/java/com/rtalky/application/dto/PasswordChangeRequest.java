package com.rtalky.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码修改请求DTO（知道旧密码）
 */
@Data
public class PasswordChangeRequest {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
