package com.rtalky.domain.service.impl;

import com.rtalky.domain.model.SmsStatistics;
import com.rtalky.domain.repository.SmsStatisticsRepository;
import com.rtalky.domain.service.SmsStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsStatisticsServiceImpl implements SmsStatisticsService {

    private final SmsStatisticsRepository smsStatisticsRepository;

    @Override
    @Transactional
    public void recordSmsSuccess(String phoneNumber, String deviceFingerprint) {
        SmsStatistics statistics = getOrCreateStatistics(phoneNumber, deviceFingerprint);
        statistics.recordSuccess();
        smsStatisticsRepository.save(statistics);

        log.info("记录短信发送成功: phoneNumber={}, deviceFingerprint={}", phoneNumber, deviceFingerprint);
    }

    @Override
    @Transactional
    public void recordSmsFailure(String phoneNumber, String deviceFingerprint, String failureReason) {
        SmsStatistics statistics = getOrCreateStatistics(phoneNumber, deviceFingerprint);
        statistics.recordFailure();
        smsStatisticsRepository.save(statistics);

        log.info("记录短信发送失败: phoneNumber={}, deviceFingerprint={}, reason={}",
                phoneNumber, deviceFingerprint, failureReason);
    }

    @Override
    public Optional<SmsStatistics> getStatistics(String phoneNumber, String deviceFingerprint) {
        return smsStatisticsRepository.findByPhoneNumberAndDeviceFingerprint(phoneNumber, deviceFingerprint);
    }

    @Override
    public List<SmsStatistics> getStatisticsByPhoneNumber(String phoneNumber) {
        return smsStatisticsRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public List<SmsStatistics> getStatisticsByDeviceFingerprint(String deviceFingerprint) {
        return smsStatisticsRepository.findByDeviceFingerprint(deviceFingerprint);
    }

    @Override
    public SmsStatistics getOverallStatistics() {
        return smsStatisticsRepository.findOverallStatistics();
    }

    private SmsStatistics getOrCreateStatistics(String phoneNumber, String deviceFingerprint) {
        return smsStatisticsRepository
                .findByPhoneNumberAndDeviceFingerprint(phoneNumber, deviceFingerprint)
                .orElseGet(() -> {
                    SmsStatistics newStats = new SmsStatistics();
                    newStats.setPhoneNumber(phoneNumber);
                    newStats.setDeviceFingerprint(deviceFingerprint);
                    return newStats;
                });
    }
}

