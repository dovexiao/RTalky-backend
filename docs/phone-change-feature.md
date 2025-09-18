# 换绑手机号功能实现文档

## 功能概述

换绑手机号功能允许已登录用户更换绑定的手机号码。该功能采用两步验证流程：
1. 发送换绑短信验证码到新手机号
2. 提交换绑申请并验证短信验证码

## 技术架构

### 1. DTO类

#### PhoneChangeSmsRequest
- `newPhoneNumber`: 新手机号（必填，格式验证）
- `deviceInfo`: 设备信息（必填）

#### PhoneChangeRequest
- `smsCode`: 短信验证码（必填，6位数字）
- `deviceInfo`: 设备信息（必填）

#### PhoneChangeResponse
- `success`: 操作是否成功
- `message`: 响应消息
- `timestamp`: 响应时间
- `newPhoneNumber`: 新手机号

### 2. 异常处理

#### PhoneChangeException
包含以下错误码：
- `PHONE_SAME`: 新手机号与当前手机号相同
- `PHONE_ALREADY_EXISTS`: 新手机号已被其他用户使用
- `PHONE_RESTRICTED`: 新手机号被限制
- `SMS_CODE_INVALID`: 短信验证码错误或已过期
- `SMS_CODE_EXPIRED`: 短信验证码已过期
- `SMS_CODE_NOT_FOUND`: 短信验证码不存在
- `USER_NOT_FOUND`: 用户不存在
- `SESSION_EXPIRED`: 会话已过期
- `DEVICE_LIMITED`: 设备操作受限
- `PHONE_LIMITED`: 手机号操作受限
- `CACHE_ERROR`: 缓存服务异常
- `SYSTEM_ERROR`: 系统异常

### 3. 事件处理

#### PhoneChangedEvent
- `userId`: 用户ID
- `oldPhoneNumber`: 旧手机号
- `newPhoneNumber`: 新手机号
- `changeTime`: 换绑时间
- `deviceFingerprint`: 设备指纹

#### PhoneChangedEventHandler
异步处理换绑成功后的后续操作：
- 更新其他相关表的手机号字段
- 发送换绑成功通知
- 记录审计日志
- 清理其他相关缓存
- 通知第三方系统手机号变更

### 4. 设备指纹服务扩展

在`DeviceFingerprintProperties`中添加了换绑操作的限流配置：
- `phoneChange`: 手机号换绑操作限流（3次/24小时）
- `phoneChangeDevice`: 设备换绑操作限流（5次/24小时）

### 5. 领域服务扩展

#### AuthenticationService新增方法
- `sendPhoneChangeSmsCode`: 发送换绑手机号短信验证码
- `verifyPhoneChangeSmsCode`: 验证换绑手机号短信验证码
- `cacheNewPhoneForChange`: 缓存换绑手机号
- `getCachedNewPhoneForChange`: 获取缓存的换绑手机号
- `clearCachedNewPhoneForChange`: 清除缓存的换绑手机号

### 6. 应用服务扩展

#### AuthenticationAppService新增方法
- `sendPhoneChangeSmsCode`: 发送换绑手机号短信验证码
- `submitPhoneChange`: 提交换绑手机号申请

### 7. 数据访问层扩展

#### LoginRecordRepository新增方法
- `updatePhoneNumber`: 更新用户登录记录中的手机号

## API接口

### 1. 发送换绑手机号短信验证码

**接口**: `POST /api/auth/phone/change/sms-code`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
  "newPhoneNumber": "13900139000",
  "deviceInfo": "{\"deviceId\":\"device123\",\"platform\":\"Android\",\"version\":\"1.0.0\"}"
}
```

**响应**:
```json
{
  "success": true,
  "message": "短信验证码发送成功",
  "codeDigits": 6,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 2. 提交换绑手机号申请

**接口**: `POST /api/auth/phone/change/submit`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
  "smsCode": "123456",
  "deviceInfo": "{\"deviceId\":\"device123\",\"platform\":\"Android\",\"version\":\"1.0.0\"}"
}
```

**响应**:
```json
{
  "success": true,
  "message": "换绑成功",
  "timestamp": "2024-01-01T12:00:00",
  "newPhoneNumber": "13900139000"
}
```

## 业务流程

### 1. 发送换绑短信验证码流程

1. 验证会话令牌并获取用户ID
2. 获取用户信息
3. 业务规则验证：
   - 检查新手机号是否与当前手机号相同
   - 检查新手机号是否已被其他用户使用
   - 检查新手机号是否被限制
4. 生成设备指纹
5. 检查设备限制
6. 缓存新手机号
7. 发送短信验证码
8. 记录设备操作

### 2. 提交换绑申请流程

1. 验证会话令牌并获取用户ID
2. 获取用户信息
3. 生成设备指纹
4. 检查设备限制
5. 获取缓存的新手机号
6. 验证短信验证码
7. 再次检查新手机号是否已被其他用户使用（双重保险）
8. 再次检查新手机号是否被限制
9. 执行换绑操作：
   - 更新用户表中的手机号字段
   - 更新登录记录表中的手机号字段
   - 更新会话信息中的手机号
10. 清理缓存
11. 清理旧手机号相关缓存
12. 发布换绑成功事件
13. 记录操作日志
14. 记录设备操作（无论成功与否）

## 安全特性

1. **会话验证**: 所有操作都需要有效的会话令牌
2. **设备指纹**: 基于设备信息生成唯一指纹，防止恶意操作
3. **限流保护**: 对设备和手机号进行限流，防止暴力攻击
4. **双重验证**: 发送验证码和提交换绑都需要验证
5. **缓存安全**: 新手机号缓存有时间限制，防止长期占用
6. **业务规则**: 严格的业务规则验证，防止非法操作

## 错误处理

所有接口都包含详细的错误处理和HTTP状态码映射：
- 400: 请求参数错误
- 401: 认证失败
- 403: 手机号被限制
- 422: 业务逻辑错误
- 429: 请求过于频繁
- 500: 系统异常

## 配置参数

在`application.yml`中可以配置以下参数：
```yaml
device:
  fingerprint:
    rate-limit:
      phone-change: # 手机号换绑操作限流
        max-attempts: 3
        time-window: 1440
      phone-change-device: # 设备换绑操作限流
        max-attempts: 5
        time-window: 1440
```

## 注意事项

1. 换绑操作是原子性的，要么全部成功，要么全部失败
2. 换绑成功后，旧手机号的相关缓存会被清理
3. 换绑操作会触发异步事件，用于后续处理
4. 所有操作都有详细的日志记录，便于问题排查
5. 设备操作计数在finally块中执行，确保无论成功与否都计数
