package com.rtalky.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 手机号注销限制配置
 */
@Entity
@Table(name = "phone_deletion_config")
@Data
public class PhoneDeletionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 50)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private Integer configValue;

    @Column(name = "description", length = 200)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
