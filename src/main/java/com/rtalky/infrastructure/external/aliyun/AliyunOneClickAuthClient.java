package com.rtalky.infrastructure.external.aliyun;

import com.rtalky.infrastructure.external.OneClickAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 阿里云一键登录服务客户端实现
 * 使用Mock模式，后期可替换为真实阿里云号码认证服务调用
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "oneclick.auth.provider", havingValue = "aliyun", matchIfMissing = true)
public class AliyunOneClickAuthClient implements OneClickAuthClient {

    @Value("${oneclick.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${oneclick.aliyun.access-key-secret:}")
    private String accessKeySecret;

    private final Random random = new Random();

    @Override
    public AuthResult verifyToken(String token) {
        try {
            // 模拟网络延迟
            Thread.sleep(150);

            // 模拟验证逻辑 - 90%成功率
            boolean success = random.nextDouble() < 0.9;

            if (success) {
                // 模拟从token中解析手机号
                String phoneNumber = "138" + (10000000 + random.nextInt(90000000));
                log.info("一键登录验证成功: token={}, 手机号={}", token, phoneNumber);

                // 实际阿里云SDK调用示例 (注释掉)
                /*
                DefaultProfile profile = DefaultProfile.getProfile(
                    "cn-hangzhou",
                    accessKeyId,
                    accessKeySecret
                );
                IAcsClient client = new DefaultAcsClient(profile);

                VerifyMobileRequest request = new VerifyMobileRequest();
                request.setToken(token);

                VerifyMobileResponse response = client.getAcsResponse(request);
                if (response.isSuccess()) {
                    return new AuthResult(true, response.getMobile(), null);
                } else {
                    return new AuthResult(false, null, response.getMessage());
                }
                */

                return new AuthResult(true, phoneNumber, null);
            } else {
                log.warn("一键登录验证失败: token={}", token);
                return new AuthResult(false, null, "模拟验证失败");
            }
        } catch (Exception e) {
            log.error("一键登录验证异常: {}", e.getMessage(), e);
            return new AuthResult(false, null, "验证异常: " + e.getMessage());
        }
    }
}
