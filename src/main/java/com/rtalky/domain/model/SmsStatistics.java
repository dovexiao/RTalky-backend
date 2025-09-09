package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AggregateRoot
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sms_statistics")
public class SmsStatistics {

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
     * 发送总数
     */
    @Column(name = "total_sent", nullable = false)
    private Long totalSent = 0L;

    /**
     * 发送成功数
     */
    @Column(name = "success_count", nullable = false)
    private Long successCount = 0L;

    /**
     * 发送失败数
     */
    @Column(name = "failure_count", nullable = false)
    private Long failureCount = 0L;

    /**
     * 发送成功率
     */
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate = BigDecimal.ZERO;

    /**
     * 总成本（元）
     */
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    /**
     * 最后发送时间
     */
    @Column(name = "last_sent_time")
    private LocalDateTime lastSentTime;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    /**
     * 记录短信发送成功
     */
    public void recordSuccess() {
        this.totalSent++;
        this.successCount++;
        this.lastSentTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.calculateSuccessRate();
        this.calculateTotalCost();
    }

    /**
     * 记录短信发送失败
     */
    public void recordFailure() {
        this.totalSent++;
        this.failureCount++;
        this.lastSentTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.calculateSuccessRate();
        this.calculateTotalCost();
    }

    /**
     * 计算成功率
     */
    private void calculateSuccessRate() {
        if (this.totalSent > 0) {
            this.successRate = BigDecimal.valueOf(this.successCount)
                    .divide(BigDecimal.valueOf(this.totalSent), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 计算总成本（每条0.05元）
     */
    private void calculateTotalCost() {
        this.totalCost = BigDecimal.valueOf(this.totalSent)
                .multiply(BigDecimal.valueOf(0.05));
    }
}


