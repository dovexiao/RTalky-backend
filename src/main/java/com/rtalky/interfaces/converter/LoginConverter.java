package com.rtalky.interfaces.converter;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.LoginResponse;
import com.rtalky.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 登录相关DTO转换器
 * 使用MapStruct实现对象转换
 */
@Mapper(componentModel = "spring")
public interface LoginConverter {

    LoginConverter INSTANCE = Mappers.getMapper(LoginConverter.class);

    /**
     * 将User实体转换为LoginResponse
     */
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(target = "sessionToken", ignore = true)
    @Mapping(target = "loginTime", ignore = true)
    @Mapping(target = "tokenExpiryTime", ignore = true)
    @Mapping(target = "userProfile", expression = "java(toUserProfile(user))")
    LoginResponse toLoginResponse(User user);

    /**
     * 转换用户资料信息
     */
    default LoginResponse.UserProfile toUserProfile(User user) {
        LoginResponse.UserProfile profile = new LoginResponse.UserProfile();
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        profile.setBio(user.getBio());
        return profile;
    }

    /**
     * 从LoginRequest中提取设备信息（用于日志记录等）
     */
    default String extractDeviceInfoForLog(LoginRequest request) {
        if (request.getDeviceInfo() == null) {
            return "Unknown Device";
        }

        // 简单的设备信息提取（实际项目中可以解析JSON）
        if (request.getDeviceInfo().length() > 100) {
            return request.getDeviceInfo().substring(0, 100) + "...";
        }
        return request.getDeviceInfo();
    }

    /**
     * 从LoginRequest中提取认证方式描述
     */
    default String getAuthMethodDescription(LoginRequest request) {
        if (request.getAuthMethod() == null) {
            return "未知认证方式";
        }
        return request.getAuthMethod().getDescription();
    }
}
