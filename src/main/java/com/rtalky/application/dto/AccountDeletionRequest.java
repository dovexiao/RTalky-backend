package com.rtalky.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "账号注销申请请求")
public class AccountDeletionRequest {

    @NotBlank(message = "短信验证码不能为空")
    @Schema(description = "短信验证码", required = true, example = "123456")
    private String smsCode;

    @NotBlank(message = "注销原因不能为空")
    @Size(max = 500, message = "注销原因不能超过500个字符")
    @Schema(description = "注销原因", required = true, example = "不再使用该应用")
    private String deletionReason;

    @NotBlank(message = "设备信息不能为空")
    @Schema(description = "设备信息", required = true, example = "{\"deviceId\":\"device123\",\"platform\":\"Android\",\"version\":\"1.0.0\"}")
    private String deviceInfo;
}
