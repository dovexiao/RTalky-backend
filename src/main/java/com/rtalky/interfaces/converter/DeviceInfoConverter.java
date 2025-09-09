package com.rtalky.interfaces.converter;

import com.rtalky.infrastructure.device.DeviceInfoParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备信息转换器
 * 处理设备信息的解析和转换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceInfoConverter {

    private final DeviceInfoParser deviceInfoParser;

    /**
     * 转换设备信息字符串为结构化对象
     */
    public DeviceInfoParser.DeviceInfo convertDeviceInfo(String deviceInfoJson) {
        try {
            return deviceInfoParser.parseDeviceInfo(deviceInfoJson);
        } catch (Exception e) {
            log.warn("设备信息转换失败，使用默认设备信息: {}", deviceInfoJson, e);

            // 返回默认设备信息
            DeviceInfoParser.DeviceInfo defaultInfo = new DeviceInfoParser.DeviceInfo();
            defaultInfo.setDeviceId("unknown");
            defaultInfo.setDeviceModel("Unknown Device");
            defaultInfo.setOsType("Unknown");
            defaultInfo.setOsVersion("Unknown");
            return defaultInfo;
        }
    }

    /**
     * 转换设备信息为用于日志的字符串
     */
    public String convertToLogString(String deviceInfoJson) {
        DeviceInfoParser.DeviceInfo deviceInfo = convertDeviceInfo(deviceInfoJson);
        return String.format("Device[model=%s, os=%s %s, id=%s]",
                deviceInfo.getDeviceModel(),
                deviceInfo.getOsType(),
                deviceInfo.getOsVersion(),
                deviceInfo.getDeviceId());
    }

    /**
     * 转换设备信息为用于指纹生成的字符串
     */
    public String convertToFingerprintString(String deviceInfoJson) {
        DeviceInfoParser.DeviceInfo deviceInfo = convertDeviceInfo(deviceInfoJson);
        return String.format("%s|%s|%s|%s|%s",
                deviceInfo.getDeviceId(),
                deviceInfo.getDeviceModel(),
                deviceInfo.getOsType(),
                deviceInfo.getOsVersion(),
                deviceInfo.getAppVersion());
    }
}
