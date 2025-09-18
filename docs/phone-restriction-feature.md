# 手机号注销限制功能

## 功能概述

为了防止用户注销后立即重新注册以绕过系统限制，实现了手机号注销限制功能。当用户注销账号后，该手机号将被限制一段时间内无法重新注册。

## 技术实现

### 1. 数据库设计

#### 配置表 (phone_deletion_config)
- `id`: 主键
- `config_key`: 配置键（如：default_restriction_days）
- `config_value`: 配置值（限制天数）
- `description`: 配置描述
- `created_at`: 创建时间
- `updated_at`: 更新时间

#### 限制记录表 (phone_deletion_restriction)
- `phone_number`: 手机号（主键）
- `deletion_time`: 注销时间
- `restriction_until`: 限制到期时间
- `deletion_reason`: 注销原因（仅作审计）
- `deletion_id`: 注销ID
- `created_at`: 创建时间

### 2. 核心服务

#### PhoneRestrictionService
- `addPhoneRestriction()`: 添加手机号限制
- `isPhoneNumberRestricted()`: 检查手机号是否被限制
- `cleanExpiredRestrictions()`: 清理过期的限制记录

### 3. 缓存策略

- 使用Redis缓存限制状态，提高查询性能
- 缓存键格式：`phone_restriction:{phoneNumber}`
- 缓存TTL与限制期剩余时间一致
- 缓存失效时自动查询数据库

### 4. 集成点

#### 注销流程
在用户成功注销后，自动添加手机号限制：
```java
phoneRestrictionService.addPhoneRestriction(
    user.getPhoneNumber(), 
    request.getDeletionReason(), 
    user.getDeletionId()
);
```

#### 注册/登录流程
在短信登录、密码登录、发送短信验证码前检查限制：
```java
if (phoneRestrictionService.isPhoneNumberRestricted(phoneNumber)) {
    throw new SmsLoginException(SmsLoginException.SmsLoginErrorCode.PHONE_RESTRICTED,
        "该手机号因注销被限制，请等待限制期结束后再试");
}
```

### 5. 配置管理

- 默认限制期：30天
- 通过数据库配置表管理，支持动态调整
- 配置键：`default_restriction_days`

### 6. 定时清理

- 每天凌晨2点执行过期记录清理
- 清理过期的限制记录和对应的缓存
- 避免数据库记录无限增长

## API响应

### 错误码
- `SMS_LOGIN_0015`: 手机号被限制（短信登录）
- `PASSWORD_LOGIN_0015`: 手机号被限制（密码登录）
- `PHONE_RESTRICTED`: 手机号被限制（短信发送）

### HTTP状态码
- `403 Forbidden`: 手机号被限制

### 响应示例
```json
{
  "success": false,
  "message": "该手机号因注销被限制，请等待限制期结束后再试",
  "timestamp": "2024-01-01T12:00:00"
}
```

## 监控与维护

### 关键指标
- 限制记录数量
- 缓存命中率
- 清理任务执行情况

### 日志记录
- 限制添加日志
- 限制检查日志
- 过期记录清理日志

## 安全考虑

1. **异常处理**: 检查限制时发生异常，为安全起见返回"限制"状态
2. **数据一致性**: 使用事务确保限制记录和缓存的一致性
3. **审计追踪**: 记录注销原因和ID，便于问题追踪
4. **配置安全**: 限制期配置存储在数据库中，避免硬编码

## 扩展性

1. **多级限制**: 可根据注销原因设置不同的限制期
2. **白名单机制**: 支持特定手机号免除限制
3. **手动管理**: 支持管理员手动添加/移除限制
4. **通知机制**: 限制期结束前可发送通知

## 测试

- 单元测试覆盖核心业务逻辑
- 集成测试验证完整流程
- 性能测试验证缓存效果
