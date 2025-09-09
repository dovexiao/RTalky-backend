package com.rtalky.application.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 登录请求验证注解
 */
@Documented
@Constraint(validatedBy = LoginRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLoginRequest {

    String message() default "认证参数不完整";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
