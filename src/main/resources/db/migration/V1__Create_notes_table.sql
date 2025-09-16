-- 创建笔记表
CREATE TABLE notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '技术主键',
    note_id VARCHAR(50) UNIQUE NOT NULL COMMENT '业务主键，笔记唯一标识符',
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    title VARCHAR(20) NOT NULL COMMENT '笔记标题',
    description VARCHAR(50) COMMENT '笔记简介',
    content LONGTEXT NOT NULL COMMENT '笔记内容',
    tags JSON COMMENT '标签列表（JSON格式存储）',
    created_time DATETIME NOT NULL COMMENT '创建时间',
    updated_time DATETIME NOT NULL COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_user_id (user_id),
    INDEX idx_created_time (created_time),
    INDEX idx_updated_time (updated_time),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';
