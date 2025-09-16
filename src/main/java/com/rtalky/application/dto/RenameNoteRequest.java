package com.rtalky.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重命名笔记请求DTO
 */
@Data
public class RenameNoteRequest {

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 20, message = "笔记标题不能超过20个字符")
    private String title;
}
