package com.rtalky.application.service;

/**
 * 手机号限制服务接口
 */
public interface PhoneRestrictionService {
    
    /**
     * 添加手机号限制
     * @param phoneNumber 手机号
     * @param deletionReason 注销原因（仅作审计）
     * @param deletionId 注销ID
     */
    void addPhoneRestriction(String phoneNumber, String deletionReason, String deletionId);
    
    /**
     * 检查手机号是否被限制
     * @param phoneNumber 手机号
     * @return true-被限制，false-未被限制
     */
    boolean isPhoneNumberRestricted(String phoneNumber);
    
    /**
     * 清理过期的限制记录
     */
    void cleanExpiredRestrictions();
}
