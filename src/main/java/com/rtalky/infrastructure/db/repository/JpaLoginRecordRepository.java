package com.rtalky.infrastructure.db.repository;

import com.rtalky.domain.model.LoginRecord;
import com.rtalky.domain.repository.LoginRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LoginRecordRepository的JPA实现
 */
@Repository
public interface JpaLoginRecordRepository extends JpaRepository<LoginRecord, Long>, LoginRecordRepository {

    @Override
    List<LoginRecord> findByUserId(String userId);

    @Override
    List<LoginRecord> findByDeviceFingerprint(String deviceFingerprint);

    @Override
    default List<LoginRecord> findRecentSuccessfulLogins(String userId, int limit) {
        return findTopByUserIdAndSuccessOrderByLoginTimeDesc(userId, true, Pageable.ofSize(limit));
    }

    List<LoginRecord> findTopByUserIdAndSuccessOrderByLoginTimeDesc(String userId, Boolean success, Pageable pageable);

    @Override
    default List<LoginRecord> findRecentFailedLogins(String userId, int limit) {
        return findTopByUserIdAndSuccessOrderByLoginTimeDesc(userId, false, Pageable.ofSize(limit));
    }

    @Override
    long countByUserId(String userId);

    @Override
    long countByDeviceFingerprint(String deviceFingerprint);

    // LoginRecord 实体没有 phoneNumber 字段，取消该方法
}
