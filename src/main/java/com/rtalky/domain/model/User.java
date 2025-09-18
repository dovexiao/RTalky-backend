package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.LocalDateTime;
import java.util.*;

@AggregateRoot
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
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    /**
     * 手机号
     */
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    /**
     * 密码(加密后存储)
     */
    @Column(name = "password")
    private String password;

    /**
     * 设备指纹
     */
    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    /**
     * 是否同意条款
     */
    @Column(name = "agreed_to_terms", nullable = false)
    private boolean agreedToTerms = false;

    /**
     * 同意条款时间
     */
    @Column(name = "agreement_time")
    private LocalDateTime agreementTime;

    /**
     * 认证方式（支持多种凭证）
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_auth_methods", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "auth_method")
    private Set<AuthMethod> authMethods = new HashSet<>();

    /**
     * 兼容性字段：主认证方式（用于向后兼容）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_auth_method", nullable = false)
    private AuthMethod primaryAuthMethod;

    /**
     * 账号状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 注册时间
     */
    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime = LocalDateTime.now();

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 头像(base64编码)
     */
    @Column(name = "avatar", columnDefinition = "LONGTEXT")
    private String avatar;

    /**
     * 昵称
     */
    @Column(name = "nickname")
    private String nickname;

    /**
     * 个人介绍
     */
    @Column(name = "bio", length = 500)
    private String bio;

    /**
     * 主题偏好
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    private ThemePreference theme = ThemePreference.SYSTEM;

    /**
     * 是否注销
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 注销时间
     */
    @Column(name = "deletion_time")
    private LocalDateTime deletionTime;

    /**
     * 注销原因
     */
    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    /**
     * 注销ID
     */
    @Column(name = "deletion_id")
    private String deletionId;

    // 工厂方法 - 创建新用户（首次登录时自动注册）
    public static User newUser(
            String phoneNumber,
            String deviceFingerprint,
            boolean agreedToTerms,
            AuthMethod authMethod
    ) {
        User user = new User();
        user.setUserId("U" + UUID.randomUUID().toString().replace("-", ""));
        user.setPhoneNumber(phoneNumber);
        user.setDeviceFingerprint(deviceFingerprint);
        user.setAgreedToTerms(agreedToTerms);
        user.setPrimaryAuthMethod(authMethod);
        user.addAuthMethod(authMethod);
        user.setAgreementTime(agreedToTerms ? LocalDateTime.now() : null);
        user.setRegistrationTime(LocalDateTime.now());
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    // 领域行为：修改密码
    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        this.password = newPassword;
        this.addAuthMethod(AuthMethod.PASSWORD); // 添加密码认证方式
        this.primaryAuthMethod = AuthMethod.PASSWORD; // 更新主认证方式
    }

    // 领域行为：验证协议同意状态
    public void verifyAgreement(boolean agreed) {
        if (!agreed) {
            throw new IllegalStateException("用户未同意服务条款");
        }
        this.agreedToTerms = true;
        this.agreementTime = LocalDateTime.now();
    }

    // 领域行为：更新登录时间
    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }

    // 领域行为：检查用户是否已设置密码
    public boolean hasPassword() {
        return this.password != null && !this.password.isEmpty();
    }

    // 领域行为：注销账号
    public void markAsDeleted() {
        this.deleted = true;
        this.status = UserStatus.DELETED;
    }

    // 领域行为：检查账号是否可用
    public boolean isAccountActive() {
        return !deleted && status == UserStatus.ACTIVE;
    }

    // 领域行为：添加认证方式
    public void addAuthMethod(AuthMethod authMethod) {
        if (authMethod != null) {
            this.authMethods.add(authMethod);
        }
    }

    // 领域行为：移除认证方式
    public void removeAuthMethod(AuthMethod authMethod) {
        if (authMethod != null && this.authMethods.size() > 1) {
            this.authMethods.remove(authMethod);
            // 如果移除的是主认证方式，选择第一个可用的作为主认证方式
            if (this.primaryAuthMethod == authMethod) {
                this.primaryAuthMethod = this.authMethods.iterator().next();
            }
        }
    }

    // 领域行为：检查是否拥有指定认证方式
    public boolean hasAuthMethod(AuthMethod authMethod) {
        return this.authMethods.contains(authMethod);
    }

    // 领域行为：获取所有认证方式
    public Set<AuthMethod> getAuthMethods() {
        return new HashSet<>(this.authMethods);
    }

    // 兼容性方法：获取主认证方式（向后兼容）
    public AuthMethod getAuthMethod() {
        return this.primaryAuthMethod;
    }

    // 兼容性方法：设置主认证方式（向后兼容）
    public void setAuthMethod(AuthMethod authMethod) {
        this.primaryAuthMethod = authMethod;
        if (authMethod != null) {
            this.addAuthMethod(authMethod);
        }
    }
}
