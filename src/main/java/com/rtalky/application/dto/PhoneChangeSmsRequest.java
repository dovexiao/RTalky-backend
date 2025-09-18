package com.rtalky.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 换绑手机号短信验证码请求DTO
 */
@Data
@Schema(description = "换绑手机号短信验证码请求")
public class PhoneChangeSmsRequest {

    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "新手机号", required = true, example = "13900139000")
    private String newPhoneNumber;

    @NotBlank(message = "设备信息不能为空")
    @Schema(description = "设备信息", required = true, example = "{\"deviceId\":\"device123\",\"platform\":\"Android\",\"version\":\"1.0.0\"}")
    private String deviceInfo;
}
