package com.rtalky.application.service.impl;

import com.rtalky.application.service.SmsStatisticsAppService;
import com.rtalky.domain.model.SmsStatistics;
import com.rtalky.domain.service.SmsStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsStatisticsAppServiceImpl implements SmsStatisticsAppService {

    private final SmsStatisticsService smsStatisticsService;

    @Override
    public SmsStatistics getStatistics(String phoneNumber, String deviceFingerprint) {
        log.info("获取短信统计信息: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
        return smsStatisticsService.getStatistics(phoneNumber, deviceFingerprint)
                .orElse(null);
    }

    @Override
    public List<SmsStatistics> getStatisticsByPhoneNumber(String phoneNumber) {
        log.info("获取手机号的所有设备统计: phoneNumber={}", phoneNumber);
        return smsStatisticsService.getStatisticsByPhoneNumber(phoneNumber);
    }

    @Override
    public List<SmsStatistics> getStatisticsByDeviceFingerprint(String deviceFingerprint) {
        log.info("获取设备的所有手机号统计: deviceFingerprint={}", deviceFingerprint);
        return smsStatisticsService.getStatisticsByDeviceFingerprint(deviceFingerprint);
    }

    @Override
    public SmsStatistics getOverallStatistics() {
        log.info("获取总体统计信息");
        return smsStatisticsService.getOverallStatistics();
    }
}












