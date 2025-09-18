package com.rtalky.domain.repository;

import com.rtalky.domain.model.PhoneDeletionRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 手机号注销限制记录Repository
 */
@Repository
public interface PhoneDeletionRestrictionRepository extends JpaRepository<PhoneDeletionRestriction, String> {
    
    /**
     * 根据手机号查找限制记录
     */
    Optional<PhoneDeletionRestriction> findByPhoneNumber(String phoneNumber);
    
    /**
     * 查找过期的限制记录
     */
    @Query("SELECT p FROM PhoneDeletionRestriction p WHERE p.restrictionUntil < :now")
    List<PhoneDeletionRestriction> findExpiredRestrictions(@Param("now") LocalDateTime now);
    
    /**
     * 根据手机号删除限制记录
     */
    void deleteByPhoneNumber(String phoneNumber);
}
