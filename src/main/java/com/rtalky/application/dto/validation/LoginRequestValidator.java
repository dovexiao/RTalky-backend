package com.rtalky.application.dto.validation;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.domain.model.AuthMethod;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 登录请求验证器
 * 确保根据认证方式提供相应的参数
 */
public class LoginRequestValidator implements ConstraintValidator<ValidLoginRequest, LoginRequest> {

    @Override
    public boolean isValid(LoginRequest request, ConstraintValidatorContext context) {
        AuthMethod authMethod = request.getAuthMethod();

        if (authMethod == null) {
            return false;
        }

        switch (authMethod) {
            case SMS:
                return request.getSmsCode() != null && !request.getSmsCode().trim().isEmpty();
            case PASSWORD:
                return request.getPassword() != null && !request.getPassword().trim().isEmpty();
            case ONE_CLICK:
                return request.getOneClickToken() != null && !request.getOneClickToken().trim().isEmpty();
            default:
                return false;
        }
    }
}
