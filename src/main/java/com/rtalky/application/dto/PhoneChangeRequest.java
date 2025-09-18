package com.rtalky.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 换绑手机号请求DTO
 */
@Data
@Schema(description = "换绑手机号请求")
public class PhoneChangeRequest {

    @NotBlank(message = "短信验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    @Schema(description = "短信验证码", required = true, example = "123456")
    private String smsCode;

    @NotBlank(message = "设备信息不能为空")
    @Schema(description = "设备信息", required = true, example = "{\"deviceId\":\"device123\",\"platform\":\"Android\",\"version\":\"1.0.0\"}")
    private String deviceInfo;
}
