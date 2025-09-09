package com.rtalky.domain.repository;

import com.rtalky.domain.model.SmsStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsStatisticsRepository extends JpaRepository<SmsStatistics, Long> {

    /**
     * 根据手机号和设备指纹查找统计信息
     */
    Optional<SmsStatistics> findByPhoneNumberAndDeviceFingerprint(String phoneNumber, String deviceFingerprint);

    /**
     * 根据手机号查找所有设备统计
     */
    List<SmsStatistics> findByPhoneNumber(String phoneNumber);

    /**
     * 根据设备指纹查找所有手机号统计
     */
    List<SmsStatistics> findByDeviceFingerprint(String deviceFingerprint);

    /**
     * 获取总体统计信息
     */
    @Query("SELECT s FROM SmsStatistics s WHERE s.id = (" +
           "SELECT MAX(s2.id) FROM SmsStatistics s2)")
    SmsStatistics findOverallStatistics();
}
