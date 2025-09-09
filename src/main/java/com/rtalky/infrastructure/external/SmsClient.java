package com.rtalky.infrastructure.external;

/**
 * 短信服务客户端接口
 */
public interface SmsClient {
    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @return 发送是否成功
     */
    boolean sendVerificationCode(String phoneNumber, String code);

    /**
     * 验证短信验证码
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @return 验证是否成功
     */
    boolean verifyCode(String phoneNumber, String code);
}
