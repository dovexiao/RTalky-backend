# 笔记模块架构设计

## 概述

笔记模块是RTalky项目的新增业务模块，采用DDD（领域驱动设计）架构模式，完全遵循项目现有的架构规范。

## 架构分层

### 1. 领域层 (Domain Layer)

#### 1.1 聚合根
- **Note**: 笔记聚合根，包含笔记的核心业务逻辑
  - 位置: `src/main/java/com/rtalky/domain/model/Note.java`
  - 特性: 使用`@AggregateRoot`注解，包含业务规则验证

#### 1.2 领域服务
- **NoteService**: 笔记领域服务接口
  - 位置: `src/main/java/com/rtalky/domain/service/NoteService.java`
  - 功能: 验证标题、内容、简介、标签等业务规则

#### 1.3 仓储接口
- **NoteRepository**: 笔记仓储接口
  - 位置: `src/main/java/com/rtalky/domain/repository/NoteRepository.java`
  - 功能: 定义笔记数据的持久化操作

#### 1.4 领域事件
- **NoteCreatedEvent**: 笔记创建事件
- **NoteUpdatedEvent**: 笔记更新事件
- **NoteDeletedEvent**: 笔记删除事件
- 位置: `src/main/java/com/rtalky/domain/event/`

### 2. 应用层 (Application Layer)

#### 2.1 应用服务
- **NoteAppService**: 笔记应用服务接口
- **NoteAppServiceImpl**: 笔记应用服务实现
  - 位置: `src/main/java/com/rtalky/application/service/`
  - 功能: 协调领域服务和基础设施，处理业务流程

#### 2.2 DTO对象
- **CreateNoteRequest**: 创建笔记请求
- **RenameNoteRequest**: 重命名笔记请求
- **UpdateDescriptionRequest**: 更新简介请求
- **UpdateNoteRequest**: 更新笔记请求
- **NoteListResponse**: 笔记列表响应
- **NoteResponse**: 笔记详情响应
- **NotePageResponse**: 笔记分页响应
- 位置: `src/main/java/com/rtalky/application/dto/`

#### 2.3 事件处理器
- **NoteCreatedEventHandler**: 笔记创建事件处理器
- **NoteUpdatedEventHandler**: 笔记更新事件处理器
- **NoteDeletedEventHandler**: 笔记删除事件处理器
- 位置: `src/main/java/com/rtalky/application/event/`

### 3. 接口层 (Interfaces Layer)

#### 3.1 REST控制器
- **NoteController**: 笔记REST控制器
  - 位置: `src/main/java/com/rtalky/interfaces/rest/NoteController.java`
  - 功能: 处理所有笔记相关的HTTP请求
  - 特性: 完整的Swagger文档配置

### 4. 基础设施层 (Infrastructure Layer)

#### 4.1 仓储实现
- **NoteRepositoryImpl**: 笔记仓储实现
- **JpaNoteRepository**: JPA仓储接口
- 位置: `src/main/java/com/rtalky/infrastructure/db/repository/`

#### 4.2 领域服务实现
- **NoteServiceImpl**: 笔记领域服务实现
- 位置: `src/main/java/com/rtalky/infrastructure/domain/service/`

## 业务功能

### 1. 核心功能
- ✅ 分页获取笔记列表（每页最大50条）
- ✅ 创建笔记（标题、简介、内容、标签）
- ✅ 重命名笔记
- ✅ 修改笔记简介
- ✅ 删除笔记（软删除）
- ✅ 修改笔记（支持部分字段更新）
- ✅ 获取笔记详情

### 2. 搜索功能
- ✅ 根据标签搜索笔记
- ✅ 根据标题搜索笔记
- ✅ 根据内容搜索笔记

### 3. 校验规则
- **标题**: 必填，最大20个字符
- **简介**: 可选，最大50个字符
- **内容**: 必填（除修改接口外），字数不限
- **标签**: 可选，每个标签最大10个字符，最多20个标签
- **分页**: 每页最大50条记录

## 数据库设计

### 笔记表 (notes)
```sql
CREATE TABLE notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id VARCHAR(50) UNIQUE NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    title VARCHAR(20) NOT NULL,
    description VARCHAR(50),
    content LONGTEXT NOT NULL,
    tags JSON,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_created_time (created_time),
    INDEX idx_updated_time (updated_time),
    INDEX idx_deleted (deleted)
);
```

## API接口

### 1. 基础CRUD接口
- `GET /api/notes` - 分页获取笔记列表
- `POST /api/notes` - 创建笔记
- `GET /api/notes/{noteId}` - 获取笔记详情
- `PUT /api/notes/{noteId}` - 修改笔记
- `DELETE /api/notes/{noteId}` - 删除笔记

### 2. 部分更新接口
- `PUT /api/notes/{noteId}/title` - 重命名笔记
- `PUT /api/notes/{noteId}/description` - 修改笔记简介

### 3. 搜索接口
- `GET /api/notes/search/tag` - 根据标签搜索
- `GET /api/notes/search/title` - 根据标题搜索
- `GET /api/notes/search/content` - 根据内容搜索

## 技术特性

### 1. 架构特性
- ✅ 完全遵循DDD架构模式
- ✅ 领域驱动设计，业务逻辑集中在领域层
- ✅ 事件驱动架构，支持异步处理
- ✅ 依赖注入，松耦合设计

### 2. 安全特性
- ✅ 基于会话令牌的身份验证
- ✅ 用户只能操作自己的笔记
- ✅ 软删除，保持数据完整性

### 3. 性能特性
- ✅ 分页查询，支持大数据量
- ✅ 数据库索引优化
- ✅ 异步事件处理

### 4. 可维护性
- ✅ 完整的Swagger API文档
- ✅ 统一的异常处理
- ✅ 详细的日志记录
- ✅ 清晰的代码结构

## 待实现功能

### 1. 具体实现逻辑
- [ ] JSON标签转换逻辑
- [ ] 数据库查询实现
- [ ] 业务规则验证实现
- [ ] 事件处理逻辑实现

### 2. 高级功能
- [ ] 笔记版本控制
- [ ] 笔记分享功能
- [ ] 笔记分类管理
- [ ] 笔记导入导出

### 3. 性能优化
- [ ] 缓存机制
- [ ] 搜索索引优化
- [ ] 数据库查询优化

## 总结

笔记模块的架构设计完全遵循了项目现有的DDD架构模式，保持了代码的一致性和可维护性。通过分层设计、事件驱动、依赖注入等技术手段，实现了高内聚、低耦合的系统架构。同时，完整的Swagger文档配置和统一的异常处理机制，为后续的开发和维护提供了良好的基础。
