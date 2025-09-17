package com.rtalky.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "账号注销短信验证码请求")
public class AccountDeletionSmsRequest {

    @NotNull(message = "必须同意注销说明")
    @Schema(description = "是否已同意注销说明", required = true, example = "true")
    private Boolean agreedToDeletion;

    @NotBlank(message = "设备信息不能为空")
    private String deviceInfo;
}
