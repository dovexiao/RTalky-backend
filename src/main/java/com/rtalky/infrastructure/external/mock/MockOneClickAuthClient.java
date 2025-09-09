package com.rtalky.infrastructure.external.mock;

import com.rtalky.infrastructure.external.OneClickAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Mock一键登录服务客户端，用于开发和测试环境
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "oneclick.auth.provider", havingValue = "mock")
public class MockOneClickAuthClient implements OneClickAuthClient {

    private final Random random = new Random();

    @Override
    public AuthResult verifyToken(String token) {
        log.info("Mock一键登录验证: token={}", token);

        // 模拟100%成功率
        String phoneNumber = "138" + (10000000 + random.nextInt(90000000));
        log.info("Mock一键登录验证成功: token={}, 手机号={}", token, phoneNumber);

        return new AuthResult(true, phoneNumber, null);
    }
}
