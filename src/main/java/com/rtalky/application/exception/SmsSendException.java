package com.rtalky.application.exception;

/**
 * 短信发送异常
 * 用于传递具体的短信发送错误信息
 */
public class SmsSendException extends RuntimeException {
    
    private final SmsSendErrorCode errorCode;
    
    public SmsSendException(SmsSendErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SmsSendException(SmsSendErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public SmsSendErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * 短信发送错误码枚举
     */
    public enum SmsSendErrorCode {
        INVALID_PHONE_NUMBER("INVALID_PHONE_NUMBER", "手机号格式无效"),
        PHONE_NUMBER_REQUIRED("PHONE_NUMBER_REQUIRED", "手机号不能为空"),
        AGREEMENT_NOT_ACCEPTED("AGREEMENT_NOT_ACCEPTED", "必须同意用户协议和隐私政策"),
        DEVICE_LIMITED("DEVICE_LIMITED", "设备或手机号操作受限，请稍后再试"),
        SMS_ALREADY_SENT("SMS_ALREADY_SENT", "验证码已发送，请稍后再试"),
        SMS_SEND_FAILED("SMS_SEND_FAILED", "短信发送失败，请稍后重试"),
        SMS_SERVICE_ERROR("SMS_SERVICE_ERROR", "短信服务异常，请稍后重试"),
        CACHE_ERROR("CACHE_ERROR", "缓存服务异常，请稍后重试"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误，请稍后重试");
        
        private final String code;
        private final String message;
        
        SmsSendErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
