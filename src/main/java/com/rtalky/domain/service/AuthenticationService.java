package com.rtalky.domain.service;

import com.rtalky.domain.model.User;
import com.rtalky.domain.model.AuthMethod;

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
}
