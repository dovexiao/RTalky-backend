package com.rtalky.infrastructure.db.repository;

import com.rtalky.domain.model.LoginRecord;
import com.rtalky.domain.repository.LoginRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Modifying
    @Transactional
    @Query("UPDATE LoginRecord l SET l.phoneNumber = :newPhoneNumber WHERE l.userId = :userId AND l.phoneNumber = :oldPhoneNumber")
    void updatePhoneNumber(@Param("userId") String userId, @Param("oldPhoneNumber") String oldPhoneNumber, @Param("newPhoneNumber") String newPhoneNumber);
}
