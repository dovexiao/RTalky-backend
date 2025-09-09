package com.rtalky.interfaces.converter;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.SmsSendRequest;
import com.rtalky.domain.model.AuthMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证参数转换器
 * 将DTO转换为领域层需要的参数格式
 */
@Component
public class AuthParameterConverter {

    /**
     * 转换登录请求为领域参数映射
     */
    public Map<String, Object> convertLoginToDomainParams(LoginRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("phoneNumber", request.getPhoneNumber());
        params.put("authMethod", request.getAuthMethod());
        params.put("deviceInfo", request.getDeviceInfo());
        params.put("agreedToTerms", request.getAgreedToTerms());

        // 根据认证方式添加相应参数
        if (request.getAuthMethod() == AuthMethod.SMS) {
            params.put("smsCode", request.getSmsCode());
        } else if (request.getAuthMethod() == AuthMethod.PASSWORD) {
            params.put("password", request.getPassword());
        } else if (request.getAuthMethod() == AuthMethod.ONE_CLICK) {
            params.put("oneClickToken", request.getOneClickToken());
        }

        return params;
    }

    /**
     * 转换短信发送请求为领域参数映射
     */
    public Map<String, Object> convertSmsSendToDomainParams(SmsSendRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("phoneNumber", request.getPhoneNumber());
        params.put("deviceInfo", request.getDeviceInfo());
        params.put("agreedToTerms", request.getAgreedToTerms());
        return params;
    }

    /**
     * 验证登录请求参数完整性
     */
    public boolean validateLoginParameters(LoginRequest request) {
        if (request.getAuthMethod() == null) {
            return false;
        }

        return switch (request.getAuthMethod()) {
            case SMS -> request.getSmsCode() != null && !request.getSmsCode().trim().isEmpty();
            case PASSWORD -> request.getPassword() != null && !request.getPassword().trim().isEmpty();
            case ONE_CLICK -> request.getOneClickToken() != null && !request.getOneClickToken().trim().isEmpty();
        };
    }

    /**
     * 获取认证方式的参数名称
     */
    public String getAuthMethodParameterName(AuthMethod authMethod) {
        return switch (authMethod) {
            case SMS -> "smsCode";
            case PASSWORD -> "password";
            case ONE_CLICK -> "oneClickToken";
        };
    }
}
