package com.rtalky.domain.repository;

import com.rtalky.domain.model.LoginRecord;

import java.util.List;

/**
 * 登录记录仓储接口
 * 定义登录记录的持久化操作
 */
public interface LoginRecordRepository {

    /**
     * 保存登录记录
     *
     * @param loginRecord 登录记录对象
     * @return 保存后的登录记录
     */
    LoginRecord save(LoginRecord loginRecord);

    /**
     * 根据用户ID查找登录记录
     *
     * @param userId 用户ID
     * @return 登录记录列表
     */
    List<LoginRecord> findByUserId(String userId);

    /**
     * 根据设备指纹查找登录记录
     *
     * @param deviceFingerprint 设备指纹
     * @return 登录记录列表
     */
    List<LoginRecord> findByDeviceFingerprint(String deviceFingerprint);

    /**
     * 查找最近的成功登录记录
     *
     * @param userId 用户ID
     * @param limit 记录数量限制
     * @return 登录记录列表
     */
    List<LoginRecord> findRecentSuccessfulLogins(String userId, int limit);

    /**
     * 查找最近的失败登录记录
     *
     * @param userId 用户ID
     * @param limit 记录数量限制
     * @return 登录记录列表
     */
    List<LoginRecord> findRecentFailedLogins(String userId, int limit);

    /**
     * 统计某用户的登录次数
     *
     * @param userId 用户ID
     * @return 登录次数
     */
    long countByUserId(String userId);

    /**
     * 统计某设备的登录次数
     *
     * @param deviceFingerprint 设备指纹
     * @return 登录次数
     */
    long countByDeviceFingerprint(String deviceFingerprint);

    // LoginRecord 不存储 phoneNumber，不需要更新
}
