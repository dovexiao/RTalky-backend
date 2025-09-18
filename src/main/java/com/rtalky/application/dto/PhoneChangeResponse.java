package com.rtalky.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 换绑手机号响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "换绑手机号响应")
public class PhoneChangeResponse {

    @Schema(description = "操作是否成功", example = "true")
    private boolean success;

    @Schema(description = "响应消息", example = "换绑成功")
    private String message;

    @Schema(description = "响应时间", example = "2024-01-01T12:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "新手机号", example = "13900139000")
    private String newPhoneNumber;
}
