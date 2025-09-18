package com.rtalky.application.service.impl;

import com.rtalky.application.service.PhoneRestrictionService;
import com.rtalky.domain.model.PhoneDeletionConfig;
import com.rtalky.domain.model.PhoneDeletionRestriction;
import com.rtalky.domain.repository.PhoneDeletionConfigRepository;
import com.rtalky.domain.repository.PhoneDeletionRestrictionRepository;
import com.rtalky.infrastructure.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 手机号限制服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneRestrictionServiceImpl implements PhoneRestrictionService {

    private final PhoneDeletionConfigRepository configRepository;
    private final PhoneDeletionRestrictionRepository restrictionRepository;
    private final CacheService cacheService;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "phone_restriction:";
    // 配置键
    private static final String DEFAULT_RESTRICTION_DAYS_KEY = "default_restriction_days";

    @Override
    @Transactional
    public void addPhoneRestriction(String phoneNumber, String deletionReason, String deletionId) {
        try {
            // 从配置表获取限制天数
            int restrictionDays = getRestrictionDays();
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime restrictionUntil = now.plusDays(restrictionDays);
            
            // 创建限制记录
            PhoneDeletionRestriction restriction = new PhoneDeletionRestriction();
            restriction.setPhoneNumber(phoneNumber);
            restriction.setDeletionTime(now);
            restriction.setRestrictionUntil(restrictionUntil);
            restriction.setDeletionReason(deletionReason);
            restriction.setDeletionId(deletionId);
            
            restrictionRepository.save(restriction);
            
            // 缓存限制信息
            String cacheKey = CACHE_PREFIX + phoneNumber;
            long ttlSeconds = Duration.between(now, restrictionUntil).getSeconds();
            cacheService.set(cacheKey, "1", ttlSeconds, TimeUnit.SECONDS);
            
            log.info("添加手机号限制: phoneNumber={}, restrictionUntil={}, reason={}", 
                phoneNumber, restrictionUntil, deletionReason);
                
        } catch (Exception e) {
            log.error("添加手机号限制失败: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            // 不抛出异常，避免影响注销主流程
        }
    }

    @Override
    public boolean isPhoneNumberRestricted(String phoneNumber) {
        try {
            // 1. 先查缓存
            String cacheKey = CACHE_PREFIX + phoneNumber;
            if (cacheService.get(cacheKey) != null) {
                log.debug("手机号在限制期内(缓存): phoneNumber={}", phoneNumber);
                return true;
            }
            
            // 2. 查数据库
            Optional<PhoneDeletionRestriction> restrictionOpt = restrictionRepository.findByPhoneNumber(phoneNumber);
            if (restrictionOpt.isPresent()) {
                PhoneDeletionRestriction restriction = restrictionOpt.get();
                if (restriction.getRestrictionUntil().isAfter(LocalDateTime.now())) {
                    // 仍在限制期内，缓存结果
                    long ttlSeconds = Duration.between(LocalDateTime.now(), restriction.getRestrictionUntil()).getSeconds();
                    cacheService.set(cacheKey, "1", ttlSeconds, TimeUnit.SECONDS);
                    log.debug("手机号在限制期内(数据库): phoneNumber={}, restrictionUntil={}", 
                        phoneNumber, restriction.getRestrictionUntil());
                    return true;
                } else {
                    // 限制期已过，删除记录
                    restrictionRepository.deleteByPhoneNumber(phoneNumber);
                    log.debug("手机号限制期已过，删除记录: phoneNumber={}", phoneNumber);
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("检查手机号限制状态失败: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            // 发生异常时，为了安全起见，返回true（限制）
            return true;
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanExpiredRestrictions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<PhoneDeletionRestriction> expired = restrictionRepository.findExpiredRestrictions(now);
            
            if (!expired.isEmpty()) {
                // 清理缓存
                for (PhoneDeletionRestriction restriction : expired) {
                    String cacheKey = CACHE_PREFIX + restriction.getPhoneNumber();
                    cacheService.delete(cacheKey);
                }
                
                // 删除数据库记录
                restrictionRepository.deleteAll(expired);
                
                log.info("清理过期手机号限制记录: count={}", expired.size());
            }
        } catch (Exception e) {
            log.error("清理过期限制记录失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 从配置表获取限制天数
     */
    private int getRestrictionDays() {
        try {
            Optional<PhoneDeletionConfig> configOpt = configRepository.findByConfigKey(DEFAULT_RESTRICTION_DAYS_KEY);
            if (configOpt.isPresent()) {
                return configOpt.get().getConfigValue();
            }
        } catch (Exception e) {
            log.warn("获取限制天数配置失败，使用默认值: error={}", e.getMessage());
        }
        
        // 默认30天
        return 30;
    }
}
