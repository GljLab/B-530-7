USE permission_system;

ALTER TABLE room_change_log 
ADD COLUMN related_order_id BIGINT COMMENT '关联维护单ID' 
AFTER related_order_no;

CREATE INDEX idx_related_order_id ON room_change_log(related_order_id);
