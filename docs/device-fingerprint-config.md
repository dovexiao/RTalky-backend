# 设备指纹限流配置说明

## 概述

设备指纹限流系统通过分析设备信息和手机号，实现分级限流策略，有效防止恶意操作和滥用。

## 配置结构

### 配置文件位置
`src/main/resources/application.yml`

### 配置项说明

```yaml
device:
  fingerprint:
    algorithm: SHA-256                    # 设备指纹生成算法
    rate-limit:                          # 限流配置
      send-sms:                          # 短信发送限流
        max-attempts: 5                  # 手机号最大尝试次数：5次
        time-window: 60                  # 时间窗口：60分钟
      send-sms-device:                   # 设备短信发送限流
        max-attempts: 20                 # 设备最大尝试次数：20次
        time-window: 60                  # 时间窗口：60分钟
      login-attempt:                     # 登录尝试限流
        max-attempts: 10                 # 手机号最大尝试次数：10次
        time-window: 60                  # 时间窗口：60分钟
      login-attempt-device:              # 设备登录尝试限流
        max-attempts: 30                 # 设备最大尝试次数：30次
        time-window: 60                  # 时间窗口：60分钟
    restriction:                         # 限制惩罚配置
      duration-hours: 24                 # 超限后限制时长：24小时
```

## 限流策略

### 分级限流
- **手机号级别**：针对单个手机号的限制，防止恶意用户
- **设备级别**：针对单个设备的限制，防止设备滥用

### 操作类型
- **SEND_SMS**：短信验证码发送
- **LOGIN_ATTEMPT**：登录尝试

### 时间窗口
- 使用滑动窗口算法，精确到分钟级别
- 支持1分钟到24小时的配置范围

## 配置验证

### 自动验证
系统启动时会自动验证配置的合理性：

1. **数值范围验证**
   - 最大尝试次数：1-1000
   - 时间窗口：1-1440分钟
   - 限制时长：1-720小时

2. **逻辑合理性验证**
   - 设备级别限制应大于等于手机号级别限制
   - 时间窗口不应过短（建议≥5分钟）

3. **算法支持验证**
   - 支持 SHA-256、SHA-1、MD5
   - 推荐使用 SHA-256

### 验证失败处理
- **开发环境**：抛出异常，阻止应用启动
- **生产环境**：记录错误日志，应用继续运行

## 使用示例

### 环境变量配置
```bash
# 自定义 Redis 配置
export REDIS_HOST=192.168.1.100
export REDIS_PORT=6380
export REDIS_PASSWORD=your_password

# 自定义限流配置
export DEVICE_SMS_MAX_ATTEMPTS=10
export DEVICE_SMS_TIME_WINDOW=30
```

### 配置文件覆盖
```yaml
# 生产环境配置
device:
  fingerprint:
    rate-limit:
      send-sms:
        max-attempts: 3        # 生产环境更严格
        time-window: 30        # 30分钟窗口
      restriction:
        duration-hours: 48     # 48小时限制
```

## 监控和日志

### 日志级别
```yaml
logging:
  level:
    com.rtalky.infrastructure.device: DEBUG  # 设备指纹服务日志
    org.springframework.data.redis: DEBUG    # Redis 操作日志
```

### 关键日志事件
- 设备指纹生成
- 限流触发
- 配置验证结果
- 异常操作警告

## 性能考虑

### Redis 键设计
- 使用前缀区分不同类型的计数
- 自动设置过期时间，避免内存泄漏
- 定期清理过期记录

### 限流精度
- 毫秒级响应时间
- 原子操作保证计数准确性
- 支持高并发场景

## 故障排除

### 常见问题
1. **配置验证失败**
   - 检查配置值是否在合理范围内
   - 验证时间单位是否正确

2. **限流过于严格**
   - 调整 `max-attempts` 值
   - 增加 `time-window` 时间

3. **Redis 连接问题**
   - 检查 Redis 服务状态
   - 验证连接配置

### 调试模式
```yaml
# 启用详细日志
logging:
  level:
    com.rtalky.infrastructure.device: TRACE
    org.springframework.data.redis: TRACE
```

## 最佳实践

1. **配置管理**
   - 使用环境变量覆盖敏感配置
   - 定期审查限流策略
   - 监控限流触发频率

2. **限流策略**
   - 设备级别限制应大于手机号级别
   - 时间窗口不宜过短（≥5分钟）
   - 根据业务特点调整限制次数

3. **监控告警**
   - 设置限流触发阈值告警
   - 监控异常设备行为
   - 定期分析限流数据
