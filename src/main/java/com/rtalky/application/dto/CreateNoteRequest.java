package com.rtalky.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建笔记请求DTO
 */
@Data
public class CreateNoteRequest {

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 20, message = "笔记标题不能超过20个字符")
    private String title;

    @Size(max = 50, message = "笔记简介不能超过50个字符")
    private String description;

    @NotBlank(message = "笔记内容不能为空")
    private String content;

    @Size(max = 20, message = "标签数量不能超过20个")
    private List<@Size(max = 10, message = "标签长度不能超过10个字符") String> tags;
}
