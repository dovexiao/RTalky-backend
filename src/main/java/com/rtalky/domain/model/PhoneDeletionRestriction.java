package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 手机号注销限制记录
 */
@Entity
@Table(name = "phone_deletion_restriction")
@Data
public class PhoneDeletionRestriction {

    @Id
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "deletion_time", nullable = false)
    private LocalDateTime deletionTime;

    @Column(name = "restriction_until", nullable = false)
    private LocalDateTime restrictionUntil;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "deletion_id", length = 50)
    private String deletionId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
