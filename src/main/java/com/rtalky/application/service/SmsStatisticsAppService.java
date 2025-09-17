package com.rtalky.application.service;

import com.rtalky.domain.model.SmsStatistics;

import java.util.List;

/**
 * 短信统计应用服务接口
 */
public interface SmsStatisticsAppService {

    /**
     * 获取手机号+设备的统计信息
     */
    SmsStatistics getStatistics(String phoneNumber, String deviceFingerprint);

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












