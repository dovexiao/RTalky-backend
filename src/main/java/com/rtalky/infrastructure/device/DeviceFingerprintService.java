package com.rtalky.infrastructure.device;

/**
 * 设备指纹服务接口
 * 负责生成和验证设备指纹，实现分级限流策略
 */
public interface DeviceFingerprintService {

    /**
     * 生成纯设备指纹（仅基于设备信息）
     *
     * @param deviceInfo 设备信息
     * @return 设备指纹哈希值
     */
    String generateDeviceFingerprint(String deviceInfo);

    /**
     * 生成复合设备指纹（设备信息+手机号）
     *
     * @param deviceInfo 设备信息
     * @param phoneNumber 手机号
     * @return 复合设备指纹哈希值
     */
    String generateCompositeFingerprint(String deviceInfo, String phoneNumber);

    /**
     * 验证设备指纹
     *
     * @param fingerprint 待验证的设备指纹
     * @param deviceInfo 当前设备信息
     * @return 验证是否通过
     */
    boolean verifyFingerprint(String fingerprint, String deviceInfo);

    /**
     * 检查设备是否被限制
     *
     * @param fingerprint 设备指纹
     * @param phoneNumber 手机号
     * @param operation 操作类型
     * @return 是否被限制
     */
    boolean isDeviceLimited(String fingerprint, String phoneNumber, String operation);

    /**
     * 记录设备操作（用于限流计数）
     *
     * @param fingerprint 设备指纹
     * @param phoneNumber 手机号
     * @param operation 操作类型
     */
    void recordDeviceOperation(String fingerprint, String phoneNumber, String operation);

    /**
     * 获取设备操作次数
     *
     * @param fingerprint 设备指纹
     * @param phoneNumber 手机号
     * @param operation 操作类型
     * @param timeWindow 时间窗口（分钟）
     * @return 操作次数
     */
    int getOperationCount(String fingerprint, String phoneNumber, String operation, int timeWindow);
}
