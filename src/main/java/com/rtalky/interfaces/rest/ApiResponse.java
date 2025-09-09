package com.rtalky.interfaces.rest;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 统一API响应格式
 */
@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    public ApiResponse(boolean success, String message, T data, String path) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public static <T> ApiResponse<T> success(T data, String path) {
        return new ApiResponse<>(true, "操作成功", data, path);
    }

    public static <T> ApiResponse<T> success(String message, T data, String path) {
        return new ApiResponse<>(true, message, data, path);
    }

    public static <T> ApiResponse<T> error(String message, String path) {
        return new ApiResponse<>(false, message, null, path);
    }

    public static <T> ApiResponse<T> error(String message, T data, String path) {
        return new ApiResponse<>(false, message, data, path);
    }
}
