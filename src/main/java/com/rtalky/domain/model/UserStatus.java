package com.rtalky.domain.model;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    ACTIVE("活跃"),
    INACTIVE("非活跃"),
    SUSPENDED("暂停使用"),
    DISABLED("已禁用"),
    LOCKED("已锁定"),
    DELETED("已删除");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

}
