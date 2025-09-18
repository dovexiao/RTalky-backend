-- 手机号注销限制配置表
CREATE TABLE phone_deletion_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value INT NOT NULL COMMENT '配置值（天数）',
    description VARCHAR(200) COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认配置：30天限制期
INSERT INTO phone_deletion_config (config_key, config_value, description) 
VALUES ('default_restriction_days', 30, '手机号注销后默认限制重新注册的天数');

-- 手机号注销限制记录表
CREATE TABLE phone_deletion_restriction (
    phone_number VARCHAR(20) PRIMARY KEY COMMENT '手机号',
    deletion_time TIMESTAMP NOT NULL COMMENT '注销时间',
    restriction_until TIMESTAMP NOT NULL COMMENT '限制到期时间',
    deletion_reason VARCHAR(500) COMMENT '注销原因（仅作审计）',
    deletion_id VARCHAR(50) COMMENT '注销ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_restriction_until (restriction_until)
) COMMENT '手机号注销限制记录表';
