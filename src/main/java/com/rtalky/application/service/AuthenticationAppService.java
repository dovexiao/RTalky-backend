package com.rtalky.application.service;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.LoginResponse;
import com.rtalky.application.dto.SmsSendRequest;
import com.rtalky.application.dto.UserInfoResponse;
import com.rtalky.application.dto.UpdateUserInfoRequest;

/**
 * 认证应用服务接口
 * 协调领域服务和基础设施，处理认证业务流程
 */
public interface AuthenticationAppService {

    /**
     * 发送短信验证码
     *
     * @param request 短信发送请求
     * @return 发送是否成功
     */
    boolean sendSmsCode(SmsSendRequest request);

    /**
     * 执行登录操作
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 一键登录
     *
     * @param request 登录请求（包含一键登录令牌）
     * @return 登录响应
     */
    LoginResponse oneClickLogin(LoginRequest request);

    /**
     * 短信验证码登录
     *
     * @param request 登录请求（包含短信验证码）
     * @return 登录响应
     */
    LoginResponse smsLogin(LoginRequest request);

    /**
     * 密码登录
     *
     * @param request 登录请求（包含密码）
     * @return 登录响应
     */
    LoginResponse passwordLogin(LoginRequest request);

    /**
     * 退出登录
     *
     * @param sessionToken 会话令牌
     * @return 退出是否成功
     */
    boolean logout(String sessionToken);

    /**
     * 验证会话令牌
     *
     * @param sessionToken 会话令牌
     * @return 用户ID（如果验证成功）
     */
    String validateSession(String sessionToken);

    /**
     * 获取用户信息
     *
     * @param sessionToken 会话令牌
     * @return 用户信息
     */
    UserInfoResponse.UserInfo getUserInfo(String sessionToken);

    /**
     * 更新用户信息
     *
     * @param sessionToken 会话令牌
     * @param request 更新用户信息请求
     * @return 更新后的用户信息
     */
    UserInfoResponse.UserInfo updateUserInfo(String sessionToken, UpdateUserInfoRequest request);
}
