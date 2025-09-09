package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.time.LocalDateTime;

/**
 * 用户登录记录实体
 * 记录每次登录的详细信息，用于安全审计和行为分析
 */
@AggregateRoot
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "login_records")
public class LoginRecord {

    /**
     * 技术主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 会话ID
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * 登录时间
     */
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    /**
     * 认证方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", nullable = false)
    private AuthMethod authMethod;

    /**
     * 客户端IP地址
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * 设备信息
     */
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    /**
     * 设备指纹
     */
    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    /**
     * 登录是否成功
     */
    @Column(name = "success")
    private Boolean success;

    /**
     * 失败原因（如果登录失败）
     */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * 地理位置信息
     */
    @Column(name = "location")
    private String location;

    /**
     * 应用版本
     */
    @Column(name = "app_version")
    private String appVersion;

    /**
     * 操作系统版本
     */
    @Column(name = "os_version")
    private String osVersion;

    // 工厂方法 - 创建成功的登录记录
    public static LoginRecord successRecord(
            String userId,
            AuthMethod authMethod,
            String deviceFingerprint,
            String ipAddress,
            String deviceInfo) {
        LoginRecord record = new LoginRecord();
        record.setUserId(userId);
        record.setAuthMethod(authMethod);
        record.setDeviceFingerprint(deviceFingerprint);
        record.setIpAddress(ipAddress);
        record.setDeviceInfo(deviceInfo);
        record.setLoginTime(LocalDateTime.now());
        record.setSuccess(true);
        return record;
    }

    // 工厂方法 - 创建失败的登录记录
    public static LoginRecord failureRecord(
            String userId,
            AuthMethod authMethod,
            String deviceFingerprint,
            String ipAddress,
            String deviceInfo,
            String failureReason) {
        LoginRecord record = new LoginRecord();
        record.setUserId(userId);
        record.setAuthMethod(authMethod);
        record.setDeviceFingerprint(deviceFingerprint);
        record.setIpAddress(ipAddress);
        record.setDeviceInfo(deviceInfo);
        record.setLoginTime(LocalDateTime.now());
        record.setSuccess(false);
        record.setFailureReason(failureReason);
        return record;
    }

    /**
     * 判断是否为异常登录
     * 可根据IP、设备等信息扩展更复杂的异常检测逻辑
     */
    public boolean isSuspicious() {
        // 简单示例：异地登录或设备变更可视为可疑
        // 实际项目中应根据历史记录进行更复杂的判断
        return false;
    }

    public Boolean isSuccess() {
        return success;
    }
}
