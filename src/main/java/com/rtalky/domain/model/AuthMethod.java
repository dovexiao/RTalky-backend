package com.rtalky.domain.model;

import lombok.Getter;

/**
 * 认证方式枚举
 */
@Getter
public enum AuthMethod {
    ONE_CLICK("一键登录"),
    SMS("短信验证码登录"),
    PASSWORD("密码登录");

    private final String description;

    AuthMethod(String description) {
        this.description = description;
    }

    /**
     * 检查是否为第三方认证方式
     */
    public boolean isThirdPartyAuth() {
        return this == ONE_CLICK || this == SMS;
    }
}
