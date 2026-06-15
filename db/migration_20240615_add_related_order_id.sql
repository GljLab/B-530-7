USE permission_system;

-- 安全添加字段（如果不存在）
SET @column_exists = (SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'permission_system' 
    AND TABLE_NAME = 'room_change_log' 
    AND COLUMN_NAME = 'related_order_id');

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE room_change_log ADD COLUMN related_order_id BIGINT COMMENT ''关联维护单ID'' AFTER related_order_no',
    'SELECT ''Column related_order_id already exists'' AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 安全创建索引（如果不存在）
SET @index_exists = (SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = 'permission_system' 
    AND TABLE_NAME = 'room_change_log' 
    AND INDEX_NAME = 'idx_related_order_id');

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_related_order_id ON room_change_log(related_order_id)',
    'SELECT ''Index idx_related_order_id already exists'' AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
