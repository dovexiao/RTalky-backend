package com.rtalky.application.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * 更新笔记简介请求DTO
 */
@Data
public class UpdateDescriptionRequest {

    @Size(max = 50, message = "笔记简介不能超过50个字符")
    private String description;
}
