package com.rtalky.application.service;

import com.rtalky.application.service.impl.PhoneRestrictionServiceImpl;
import com.rtalky.domain.model.PhoneDeletionConfig;
import com.rtalky.domain.model.PhoneDeletionRestriction;
import com.rtalky.domain.repository.PhoneDeletionConfigRepository;
import com.rtalky.domain.repository.PhoneDeletionRestrictionRepository;
import com.rtalky.infrastructure.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 手机号限制服务测试
 */
@ExtendWith(MockitoExtension.class)
class PhoneRestrictionServiceTest {

    @Mock
    private PhoneDeletionConfigRepository configRepository;

    @Mock
    private PhoneDeletionRestrictionRepository restrictionRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PhoneRestrictionServiceImpl phoneRestrictionService;

    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_DELETION_REASON = "测试注销";
    private static final String TEST_DELETION_ID = "test-deletion-123";

    @BeforeEach
    void setUp() {
        // 模拟配置返回30天限制期
        PhoneDeletionConfig config = new PhoneDeletionConfig();
        config.setConfigKey("default_restriction_days");
        config.setConfigValue(30);
        when(configRepository.findByConfigKey("default_restriction_days"))
            .thenReturn(Optional.of(config));
    }

    @Test
    void testAddPhoneRestriction() {
        // 执行
        phoneRestrictionService.addPhoneRestriction(TEST_PHONE, TEST_DELETION_REASON, TEST_DELETION_ID);

        // 验证
        verify(restrictionRepository).save(any(PhoneDeletionRestriction.class));
        verify(cacheService).set(anyString(), eq("1"), anyLong(), any());
    }

    @Test
    void testIsPhoneNumberRestricted_FromCache() {
        // 模拟缓存中存在
        when(cacheService.get("phone_restriction:" + TEST_PHONE)).thenReturn("1");

        // 执行
        boolean result = phoneRestrictionService.isPhoneNumberRestricted(TEST_PHONE);

        // 验证
        assertTrue(result);
        verify(cacheService).get("phone_restriction:" + TEST_PHONE);
        verify(restrictionRepository, never()).findByPhoneNumber(anyString());
    }

    @Test
    void testIsPhoneNumberRestricted_FromDatabase_Active() {
        // 模拟缓存中不存在，数据库中存在且未过期
        when(cacheService.get("phone_restriction:" + TEST_PHONE)).thenReturn(null);
        
        PhoneDeletionRestriction restriction = new PhoneDeletionRestriction();
        restriction.setPhoneNumber(TEST_PHONE);
        restriction.setRestrictionUntil(LocalDateTime.now().plusDays(1));
        when(restrictionRepository.findByPhoneNumber(TEST_PHONE))
            .thenReturn(Optional.of(restriction));

        // 执行
        boolean result = phoneRestrictionService.isPhoneNumberRestricted(TEST_PHONE);

        // 验证
        assertTrue(result);
        verify(cacheService).set(anyString(), eq("1"), anyLong(), any());
    }

    @Test
    void testIsPhoneNumberRestricted_FromDatabase_Expired() {
        // 模拟缓存中不存在，数据库中存在但已过期
        when(cacheService.get("phone_restriction:" + TEST_PHONE)).thenReturn(null);
        
        PhoneDeletionRestriction restriction = new PhoneDeletionRestriction();
        restriction.setPhoneNumber(TEST_PHONE);
        restriction.setRestrictionUntil(LocalDateTime.now().minusDays(1));
        when(restrictionRepository.findByPhoneNumber(TEST_PHONE))
            .thenReturn(Optional.of(restriction));

        // 执行
        boolean result = phoneRestrictionService.isPhoneNumberRestricted(TEST_PHONE);

        // 验证
        assertFalse(result);
        verify(restrictionRepository).deleteByPhoneNumber(TEST_PHONE);
    }

    @Test
    void testIsPhoneNumberRestricted_NotRestricted() {
        // 模拟缓存和数据库中都不存在
        when(cacheService.get("phone_restriction:" + TEST_PHONE)).thenReturn(null);
        when(restrictionRepository.findByPhoneNumber(TEST_PHONE))
            .thenReturn(Optional.empty());

        // 执行
        boolean result = phoneRestrictionService.isPhoneNumberRestricted(TEST_PHONE);

        // 验证
        assertFalse(result);
    }
}
