package com.rtalky.infrastructure.external.aliyun;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.rtalky.infrastructure.external.SmsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 阿里云短信服务客户端实现
 * 使用阿里云官方SDK进行短信发送和验证
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "aliyun", matchIfMissing = true)
public class AliyunSmsClient implements SmsClient {

    private Client smsClient;

    @Value("${sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.aliyun.sign-name:RTalky}")
    private String signName;

    @Value("${sms.aliyun.template-code:SMS_123456789}")
    private String templateCode;

    @Value("${sms.aliyun.region-id:cn-hangzhou}")
    private String regionId;

    @Value("${sms.aliyun.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    /**
     * 构造函数
     */
    public AliyunSmsClient() {
        // 构造函数中不进行初始化，等待 @PostConstruct
    }

    /**
     * 初始化阿里云短信客户端
     */
    @PostConstruct
    public void init() {
        try {
            if (isAkConfigured()) {
                Config config = new Config()
                        .setAccessKeyId(accessKeyId)
                        .setAccessKeySecret(accessKeySecret)
                        .setRegionId(regionId)
                        .setEndpoint(endpoint);
                
                this.smsClient = new Client(config);
                log.info("阿里云短信客户端初始化完成，区域: {}", regionId);
            } else {
                log.warn("阿里云短信客户端未配置AK，将使用模拟模式");
                this.smsClient = null;
            }
        } catch (Exception e) {
            log.error("阿里云短信客户端初始化失败: {}", e.getMessage(), e);
            this.smsClient = null;
        }
    }

    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        try {
            if (smsClient == null) {
                log.warn("阿里云短信客户端未初始化，使用模拟模式");
                return true; // 模拟成功，便于开发环境测试
            }
            
            // 1. 对手机号进行去空格处理
            String cleanedPhoneNumber = cleanPhoneNumber(phoneNumber);
            log.debug("手机号去空格处理: 原始={}, 处理后={}", phoneNumber, cleanedPhoneNumber);
            
            // 2. 构建短信发送请求
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(cleanedPhoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            // 3. 发送短信
            SendSmsResponse response = smsClient.sendSms(sendSmsRequest);

            // 4. 处理响应
            if ("OK".equalsIgnoreCase(response.getBody().getCode())) {
                log.info("短信验证码发送成功: 手机号={}, 验证码={}, 请求ID={}",
                        cleanedPhoneNumber, code, response.getBody().getRequestId());
                return true;
            } else {
                log.error("短信验证码发送失败: 手机号={}, 错误代码={}, 错误消息={}",
                        cleanedPhoneNumber, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("发送短信验证码异常: 手机号={}, 错误信息={}", phoneNumber, e.getMessage(), e);
            return false; // 异常情况下返回 false
        }
    }

    @Override
    public boolean verifyCode(String phoneNumber, String code) {
        // 注意：验证码验证应该由上游的 CacheService 统一管理
        // 这里仅作为备用方案，实际项目中建议移除
        log.warn("验证码验证应由上游 CacheService 处理，当前方法可能被误用");
        return false;
    }

    /**
     * 检查是否配置了阿里云访问密钥
     */
    private boolean isAkConfigured() {
        return accessKeyId != null && !accessKeyId.trim().isEmpty()
                && accessKeySecret != null && !accessKeySecret.trim().isEmpty();
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

    /**
     * 获取短信发送状态报告（可选功能）
     * 需要配置阿里云短信服务的回调URL
     */
    public void querySendDetails(String phoneNumber, String sendDate, long pageSize, long currentPage) {
        try {
            if (smsClient == null) {
                log.warn("阿里云短信客户端未初始化，无法查询发送详情");
                return;
            }
            
            /*
            QuerySendDetailsRequest queryRequest = new QuerySendDetailsRequest()
                    .setPhoneNumber(phoneNumber)
                    .setSendDate(sendDate)
                    .setPageSize(pageSize)
                    .setCurrentPage(currentPage);

            QuerySendDetailsResponse response = smsClient.querySendDetails(queryRequest);
            log.info("短信发送详情查询结果: {}", response.getBody());
            */
            log.info("短信发送详情查询功能待实现");
        } catch (Exception e) {
            log.error("查询短信发送详情异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 批量发送短信（可选功能）
     */
    public boolean sendBatchSms(String[] phoneNumbers, String[] signs, String[] templateCodes, String[] templateParams) {
        try {
            if (smsClient == null) {
                log.warn("阿里云短信客户端未初始化，无法批量发送短信");
                return false;
            }
            
            /*
            SendBatchSmsRequest batchRequest = new SendBatchSmsRequest()
                    .setPhoneNumberJson(com.aliyun.teautil.Common.toJSONString(phoneNumbers))
                    .setSignNameJson(com.aliyun.teautil.Common.toJSONString(signs))
                    .setTemplateCodeJson(com.aliyun.teautil.Common.toJSONString(templateCodes))
                    .setTemplateParamJson(com.aliyun.teautil.Common.toJSONString(templateParams));

            SendBatchSmsResponse response = smsClient.sendBatchSms(batchRequest);
            return "OK".equalsIgnoreCase(response.getBody().getCode());
            */
            log.info("批量发送短信功能待实现");
            return false;
        } catch (Exception e) {
            log.error("批量发送短信异常: {}", e.getMessage(), e);
            return false;
        }
    }
}
