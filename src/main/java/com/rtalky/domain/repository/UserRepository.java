package com.rtalky.domain.repository;

import com.rtalky.domain.model.User;

import java.util.Optional;

/**
 * 用户仓储接口
 * 定义用户数据的持久化操作，不包含具体实现
 */
public interface UserRepository {

    /**
     * 根据用户ID查找用户
     *
     * @param userId 用户ID
     * @return 用户对象（如果存在）
     */
    Optional<User> findByUserId(String userId);

    /**
     * 根据手机号查找用户
     *
     * @param phoneNumber 手机号
     * @return 用户对象（如果存在）
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 根据设备指纹查找用户
     *
     * @param deviceFingerprint 设备指纹
     * @return 用户对象（如果存在）
     */
    Optional<User> findByDeviceFingerprint(String deviceFingerprint);

    /**
     * 保存用户信息
     *
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    User save(User user);

    /**
     * 检查手机号是否已存在
     *
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     */
    void updateLastLoginTime(String userId);
}
