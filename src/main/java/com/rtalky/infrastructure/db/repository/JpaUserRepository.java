package com.rtalky.infrastructure.db.repository;

import com.rtalky.domain.model.User;
import com.rtalky.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UserRepository的JPA实现
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    Optional<User> findByUserId(String userId);

    @Override
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Override
    Optional<User> findByDeviceFingerprint(String deviceFingerprint);

    @Override
    boolean existsByPhoneNumber(String phoneNumber);

    @Override
    void deleteByUserId(String userId);

    @Override
    default void updateLastLoginTime(@Param("userId") String userId) {
        updateLastLoginTimeInternal(userId, LocalDateTime.now());
    }

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginTime = :loginTime WHERE u.userId = :userId")
    void updateLastLoginTimeInternal(@Param("userId") String userId, @Param("loginTime") LocalDateTime loginTime);
}
