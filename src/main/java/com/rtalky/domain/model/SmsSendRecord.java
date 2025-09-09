package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sms_send_records")
public class SmsSendRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 手机号
     */
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * 设备指纹
     */
    @Column(name = "device_fingerprint", nullable = false)
    private String deviceFingerprint;

    /**
     * 验证码
     */
    @Column(name = "sms_code", nullable = false)
    private String smsCode;

    /**
     * 发送状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SmsSendStatus status;

    /**
     * 发送时间
     */
    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;

    /**
     * 成本（元）
     */
    @Column(name = "cost", precision = 5, scale = 2)
    private BigDecimal cost = BigDecimal.valueOf(0.05);

    /**
     * 失败原因
     */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * 外部服务响应
     */
    @Column(name = "external_response")
    private String externalResponse;

    public enum SmsSendStatus {
        SUCCESS, FAILURE, PENDING
    }
}


