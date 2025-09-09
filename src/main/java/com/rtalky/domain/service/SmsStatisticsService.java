package com.rtalky.domain.service;

import com.rtalky.domain.model.SmsStatistics;

import java.util.List;
import java.util.Optional;

/**
 * 短信统计领域服务接口
 */
public interface SmsStatisticsService {

    /**
     * 记录短信发送成功
     */
    void recordSmsSuccess(String phoneNumber, String deviceFingerprint);

    /**
     * 记录短信发送失败
     */
    void recordSmsFailure(String phoneNumber, String deviceFingerprint, String failureReason);

    /**
     * 获取短信统计信息
     */
    Optional<SmsStatistics> getStatistics(String phoneNumber, String deviceFingerprint);

    /**
     * 获取手机号的所有设备统计
     */
    List<SmsStatistics> getStatisticsByPhoneNumber(String phoneNumber);

    /**
     * 获取设备的所有手机号统计
     */
    List<SmsStatistics> getStatisticsByDeviceFingerprint(String deviceFingerprint);

    /**
     * 获取总体统计信息
     */
    SmsStatistics getOverallStatistics();
}


