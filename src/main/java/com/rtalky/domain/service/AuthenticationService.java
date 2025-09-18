package com.rtalky.domain.service;

import com.rtalky.domain.model.User;


/**
 * 认证领域服务接口
 * 处理核心认证逻辑，不依赖外部实现细节
 */
public interface AuthenticationService {

    /**
     * 验证短信验证码
     *
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @param deviceFingerprint 设备指纹
     * @return 验证是否成功
     */
    boolean verifySmsCode(String phoneNumber, String smsCode, String deviceFingerprint);

    /**
     * 验证一键登录令牌
     *
     * @param phoneNumber 手机号
     * @param token 一键登录令牌
     * @param deviceFingerprint 设备指纹
     * @return 验证是否成功
     */
    boolean verifyOneClickToken(String phoneNumber, String token, String deviceFingerprint);

    /**
     * 验证密码
     *
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 验证是否成功
     */
    boolean verifyPassword(String phoneNumber, String password);
    /**
     * 检查用户是否存在
     *
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    boolean userExists(String phoneNumber);

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号
     * @param deviceFingerprint 设备指纹
     * @return 发送是否成功
     */
    boolean sendSmsCode(String phoneNumber, String deviceFingerprint);

    /**
     * 发送密码设置短信验证码
     *
     * @param phoneNumber 手机号
     * @param deviceFingerprint 设备指纹
     * @return 发送是否成功
     */
    boolean sendPasswordSmsCode(String phoneNumber, String deviceFingerprint);

    /**
     * 验证密码设置短信验证码
     *
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @param deviceFingerprint 设备指纹
     * @return 验证是否成功
     */
    boolean verifyPasswordSmsCode(String phoneNumber, String smsCode, String deviceFingerprint);

    /**
     * 设置用户密码（首次补设/重置）
     *
     * @param phoneNumber 手机号
     * @param newPassword 新密码（已加密）
     * @return 设置是否成功
     */
    boolean setPassword(String phoneNumber, String newPassword);

    /**
     * 修改用户密码（知道旧密码）
     *
     * @param user 用户对象
     * @param oldPassword 旧密码
     * @param newPassword 新密码（已加密）
     * @return 修改是否成功
     */
    boolean changePassword(User user, String oldPassword, String newPassword);

    /**
     * 缓存密码设置的新密码
     *
     * @param phoneNumber 手机号
     * @param deviceFingerprint 设备指纹
     * @param newPassword 新密码（已加密）
     * @return 缓存是否成功
     */
    boolean cachePasswordForSetup(String phoneNumber, String deviceFingerprint, String newPassword);

    /**
     * 获取缓存的密码设置新密码
     *
     * @param phoneNumber 手机号
     * @param deviceFingerprint 设备指纹
     * @return 缓存的新密码，如果不存在返回null
     */
    String getCachedPasswordForSetup(String phoneNumber, String deviceFingerprint);

    /**
     * 清除缓存的密码设置新密码
     *
     * @param phoneNumber 手机号
     * @param deviceFingerprint 设备指纹
     * @return 清除是否成功
     */
    boolean clearCachedPasswordForSetup(String phoneNumber, String deviceFingerprint);

    /**
     * 加密密码
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    String hashPassword(String password);

    /**
     * 发送账号注销短信验证码
     *
     * @param phoneNumber 手机号
     * @param deviceFingerprint 设备指纹
     * @return 发送是否成功
     */
    boolean sendDeletionSmsCode(String phoneNumber, String deviceFingerprint);

    /**
     * 验证账号注销短信验证码
     *
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @param deviceFingerprint 设备指纹
     * @return 验证是否成功
     */
    boolean verifyDeletionSmsCode(String phoneNumber, String smsCode, String deviceFingerprint);

    /**
     * 发送换绑手机号短信验证码
     *
     * @param userId 用户ID
     * @param newPhoneNumber 新手机号
     * @param deviceFingerprint 设备指纹
     * @return 发送是否成功
     */
    boolean sendPhoneChangeSmsCode(String userId, String newPhoneNumber, String deviceFingerprint);

    /**
     * 验证换绑手机号短信验证码
     *
     * @param newPhoneNumber 新手机号
     * @param smsCode 短信验证码
     * @param deviceFingerprint 设备指纹
     * @return 验证是否成功
     */
    boolean verifyPhoneChangeSmsCode(String newPhoneNumber, String smsCode, String deviceFingerprint);

    /**
     * 缓存换绑手机号的新手机号
     *
     * @param userId 用户ID
     * @param newPhoneNumber 新手机号
     * @param deviceFingerprint 设备指纹
     * @return 缓存是否成功
     */
    boolean cacheNewPhoneForChange(String userId, String newPhoneNumber, String deviceFingerprint);

    /**
     * 获取缓存的换绑手机号
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return 缓存的新手机号，如果不存在返回null
     */
    String getCachedNewPhoneForChange(String userId, String deviceFingerprint);

    /**
     * 清除缓存的换绑手机号
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return 清除是否成功
     */
    boolean clearCachedNewPhoneForChange(String userId, String deviceFingerprint);
}
