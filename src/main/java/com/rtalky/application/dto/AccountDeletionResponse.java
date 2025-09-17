package com.rtalky.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号注销响应")
public class AccountDeletionResponse {

    @Schema(description = "操作是否成功", example = "true")
    private boolean success;

    @Schema(description = "响应消息", example = "账号注销成功")
    private String message;

    @Schema(description = "操作时间", example = "2024-01-01T12:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "注销ID", example = "deletion-123456")
    private String deletionId;
}
