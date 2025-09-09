package com.rtalky.infrastructure.external.mock;

import com.rtalky.infrastructure.external.SmsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Mock短信服务客户端，用于开发和测试环境
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock")
public class MockSmsClient implements SmsClient {

    private final Map<String, String> codeStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> codeExpiry = new ConcurrentHashMap<>();

    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        // 对手机号进行去空格处理
        String cleanedPhoneNumber = cleanPhoneNumber(phoneNumber);
        log.info("Mock发送短信验证码: 原始手机号={}, 清理后手机号={}, 验证码={}", 
            phoneNumber, cleanedPhoneNumber, code);

        // 存储验证码和过期时间（5分钟）
        codeStorage.put(cleanedPhoneNumber, code);
        codeExpiry.put(cleanedPhoneNumber, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));

        return true;
    }

    @Override
    public boolean verifyCode(String phoneNumber, String code) {
        // 对手机号进行去空格处理
        String cleanedPhoneNumber = cleanPhoneNumber(phoneNumber);
        String storedCode = codeStorage.get(cleanedPhoneNumber);
        Long expiryTime = codeExpiry.get(cleanedPhoneNumber);

        if (storedCode == null || expiryTime == null) {
            log.warn("Mock验证码不存在或已过期: 原始手机号={}, 清理后手机号={}", phoneNumber, cleanedPhoneNumber);
            return false;
        }

        if (System.currentTimeMillis() > expiryTime) {
            log.warn("Mock验证码已过期: 原始手机号={}, 清理后手机号={}", phoneNumber, cleanedPhoneNumber);
            codeStorage.remove(cleanedPhoneNumber);
            codeExpiry.remove(cleanedPhoneNumber);
            return false;
        }

        boolean isValid = storedCode.equals(code);
        if (isValid) {
            codeStorage.remove(cleanedPhoneNumber);
            codeExpiry.remove(cleanedPhoneNumber);
            log.info("Mock短信验证码验证成功: 原始手机号={}, 清理后手机号={}", phoneNumber, cleanedPhoneNumber);
        } else {
            log.warn("Mock短信验证码验证失败: 原始手机号={}, 清理后手机号={}, 输入验证码={}, 正确验证码={}",
                    phoneNumber, cleanedPhoneNumber, code, storedCode);
        }

        return isValid;
    }

    /**
     * 清理手机号，去除所有空格和特殊字符
     * 支持多种格式的手机号输入
     * 
     * @param phoneNumber 原始手机号
     * @return 清理后的手机号
     */
    private String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // 去除所有空格、连字符、括号等特殊字符，但保留+号
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\*\\#]", "");
        
        // 如果清理后为空，返回原值
        if (cleaned.isEmpty()) {
            return phoneNumber;
        }
        
        return cleaned;
    }
}
