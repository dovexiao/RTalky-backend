package com.rtalky.infrastructure.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备信息解析器
 * 负责解析客户端上传的设备信息
 */
@Slf4j
@Component
public class DeviceInfoParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析设备信息JSON字符串
     */
    public DeviceInfo parseDeviceInfo(String deviceInfoJson) {
        try {
            if (deviceInfoJson == null || deviceInfoJson.trim().isEmpty()) {
                return new DeviceInfo();
            }

            // 尝试解析JSON
            Map<String, Object> deviceMap = objectMapper.readValue(deviceInfoJson, HashMap.class);
            DeviceInfo deviceInfo = new DeviceInfo();

            // 提取常用设备信息字段
            deviceInfo.setDeviceId((String) deviceMap.get("deviceId"));
            deviceInfo.setDeviceModel((String) deviceMap.get("deviceModel"));
            deviceInfo.setOsType((String) deviceMap.get("osType"));
            deviceInfo.setOsVersion((String) deviceMap.get("osVersion"));
            deviceInfo.setAppVersion((String) deviceMap.get("appVersion"));
            deviceInfo.setScreenResolution((String) deviceMap.get("screenResolution"));
            deviceInfo.setUserAgent((String) deviceMap.get("userAgent"));
            deviceInfo.setIpAddress((String) deviceMap.get("ipAddress"));

            return deviceInfo;
        } catch (JsonProcessingException e) {
            log.warn("设备信息JSON解析失败，使用原始字符串: {}", deviceInfoJson, e);

            // 解析失败时，使用原始字符串作为设备ID
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceId(deviceInfoJson);
            return deviceInfo;
        }
    }

    /**
     * 设备信息值对象
     */
    @Data
    public static class DeviceInfo {
        private String deviceId;
        private String deviceModel;
        private String osType;
        private String osVersion;
        private String appVersion;
        private String screenResolution;
        private String userAgent;
        private String ipAddress;

        @Override
        public String toString() {
            return "DeviceInfo{" +
                    "deviceId='" + deviceId + '\'' +
                    ", deviceModel='" + deviceModel + '\'' +
                    ", osType='" + osType + '\'' +
                    ", osVersion='" + osVersion + '\'' +
                    ", appVersion='" + appVersion + '\'' +
                    ", screenResolution='" + screenResolution + '\'' +
                    ", userAgent='" + userAgent + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    '}';
        }
    }
}
