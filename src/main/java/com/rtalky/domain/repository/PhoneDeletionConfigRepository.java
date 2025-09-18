package com.rtalky.domain.repository;

import com.rtalky.domain.model.PhoneDeletionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 手机号注销限制配置Repository
 */
@Repository
public interface PhoneDeletionConfigRepository extends JpaRepository<PhoneDeletionConfig, Long> {
    
    /**
     * 根据配置键查找配置
     */
    Optional<PhoneDeletionConfig> findByConfigKey(String configKey);
}
