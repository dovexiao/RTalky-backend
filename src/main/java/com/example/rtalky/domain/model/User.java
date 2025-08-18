package com.example.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.LocalDateTime;

@AggregateRoot  // DDD 注解
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    /**
     * 技术主键，用于数据库自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务主键，用户唯一标识符
     */
    private String userId;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码(加密后存储)
     */
    private String password;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 个人介绍
     */
    private String bio;

    /**
     * 注册时间
     */
    private LocalDateTime registrationTime;

    /**
     * 是否注销
     */
    private Boolean deleted = false;

    // 安全设置密码
    public void changePassword(String rawPassword) {
        // 实际应用中应该在这里进行密码加密
        this.password = rawPassword; // 仅作示例，实际应加密处理
    }
}
