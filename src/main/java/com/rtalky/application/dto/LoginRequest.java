package com.rtalky.application.dto;

import com.rtalky.application.dto.validation.ValidLoginRequest;
import com.rtalky.domain.model.AuthMethod;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 登录请求DTO
 */
@Data
@ValidLoginRequest(message = "认证参数不完整，请根据认证方式提供相应参数")
public class LoginRequest {

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotNull(message = "认证方式不能为空")
    private AuthMethod authMethod;

    // 短信验证码（短信登录时需要）
    private String smsCode;

    // 密码（密码登录时需要）
    private String password;

    // 一键登录令牌（一键登录时需要）
    private String oneClickToken;

    @NotBlank(message = "设备信息不能为空")
    private String deviceInfo;

    @NotNull(message = "必须同意用户协议和隐私政策")
    private Boolean agreedToTerms;
}
