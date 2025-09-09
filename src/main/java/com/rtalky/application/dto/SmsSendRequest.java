package com.rtalky.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 短信发送请求DTO
 */
@Data
public class SmsSendRequest {

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    @NotBlank(message = "设备信息不能为空")
    private String deviceInfo;

    @NotNull(message = "必须同意用户协议和隐私政策")
    private Boolean agreedToTerms;
}
