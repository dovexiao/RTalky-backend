-- 添加displayId字段到notes表
ALTER TABLE notes ADD COLUMN display_id VARCHAR(10) UNIQUE NOT NULL COMMENT '显示ID，格式：N+自增ID（不足三位前补零）';

-- 添加displayId索引
CREATE INDEX idx_display_id ON notes(display_id);

-- 为现有数据生成displayId（基于自增ID）
UPDATE notes SET display_id = CONCAT('N', LPAD(id, 3, '0')) WHERE display_id IS NULL OR display_id = '';

-- 确保display_id字段不能为空
ALTER TABLE notes MODIFY COLUMN display_id VARCHAR(10) UNIQUE NOT NULL COMMENT '显示ID，格式：N+自增ID（不足三位前补零）';
