package com.rtalky.interfaces.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中的各种异常，提供一致的错误响应格式
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     * 当 @Valid 注解验证失败时触发
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("timestamp", LocalDateTime.now());

        // 获取第一个字段错误
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult.hasFieldErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().get(0);

            // 优先使用自定义消息，其次使用默认消息
            String errorMessage = fieldError.getDefaultMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = fieldError.getCode() + " " + fieldError.getField();
            }

            response.put("message", errorMessage);
            response.put("field", fieldError.getField());
            response.put("rejectedValue", fieldError.getRejectedValue());
        } else {
            response.put("message", "参数验证失败");
        }

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理其他未捕获的异常
     * 作为最后的兜底处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "系统异常，请稍后重试");
        response.put("timestamp", LocalDateTime.now());

        // 记录异常日志
        System.err.println("未处理的异常: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.internalServerError().body(response);
    }
}
