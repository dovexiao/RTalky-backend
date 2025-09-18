package com.rtalky.application.service;

import com.rtalky.application.dto.LoginRequest;
import com.rtalky.application.dto.LoginResponse;
import com.rtalky.application.dto.SmsSendRequest;
import com.rtalky.application.dto.UserInfoResponse;
import com.rtalky.application.dto.UpdateUserInfoRequest;
import com.rtalky.application.dto.PasswordSmsSendRequest;
import com.rtalky.application.dto.PasswordSetRequest;
import com.rtalky.application.dto.PasswordChangeRequest;
import com.rtalky.application.dto.PasswordResponse;
import com.rtalky.application.dto.AccountDeletionSmsRequest;
import com.rtalky.application.dto.AccountDeletionRequest;
import com.rtalky.application.dto.AccountDeletionResponse;
import com.rtalky.application.dto.SmsSendResponse;
import com.rtalky.application.dto.PhoneChangeSmsRequest;
import com.rtalky.application.dto.PhoneChangeRequest;
import com.rtalky.application.dto.PhoneChangeResponse;

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

    /**
     * 发送密码设置短信验证码
     *
     * @param sessionToken 会话令牌
     * @param request 密码设置短信发送请求
     * @return 发送结果
     */
    PasswordResponse sendPasswordSmsCode(String sessionToken, PasswordSmsSendRequest request);

    /**
     * 设置密码（首次补设/重置密码）
     *
     * @param sessionToken 会话令牌
     * @param request 密码设置请求
     * @return 设置结果
     */
    PasswordResponse setPassword(String sessionToken, PasswordSetRequest request);

    /**
     * 修改密码（知道旧密码）
     *
     * @param sessionToken 会话令牌
     * @param request 密码修改请求
     * @return 修改结果
     */
    PasswordResponse changePassword(String sessionToken, PasswordChangeRequest request);

    /**
     * 发送账号注销短信验证码
     *
     * @param sessionToken 会话令牌
     * @param request 账号注销短信发送请求
     * @return 发送结果
     */
    SmsSendResponse sendDeletionSmsCode(String sessionToken, AccountDeletionSmsRequest request);

    /**
     * 提交账号注销申请
     *
     * @param sessionToken 会话令牌
     * @param request 账号注销申请请求
     * @return 注销结果
     */
    AccountDeletionResponse submitDeletion(String sessionToken, AccountDeletionRequest request);

    /**
     * 发送换绑手机号短信验证码
     *
     * @param sessionToken 会话令牌
     * @param request 换绑手机号短信发送请求
     * @return 发送结果
     */
    SmsSendResponse sendPhoneChangeSmsCode(String sessionToken, PhoneChangeSmsRequest request);

    /**
     * 提交换绑手机号申请
     *
     * @param sessionToken 会话令牌
     * @param request 换绑手机号请求
     * @return 换绑结果
     */
    PhoneChangeResponse submitPhoneChange(String sessionToken, PhoneChangeRequest request);
}
