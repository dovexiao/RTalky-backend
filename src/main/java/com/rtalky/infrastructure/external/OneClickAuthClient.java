package com.rtalky.infrastructure.external;

/**
 * 一键登录服务客户端接口
 */
public interface OneClickAuthClient {
    /**
     * 验证一键登录令牌
     *
     * @param token 一键登录令牌
     * @return 验证结果，包含手机号等信息
     */
    AuthResult verifyToken(String token);

    /**
     * 一键登录验证结果
     */
    record AuthResult(boolean success, String phoneNumber, String errorMessage) {}
}
