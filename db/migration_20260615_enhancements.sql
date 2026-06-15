-- =============================================
-- 房间维护系统增强功能数据库迁移
-- 功能：批量操作、智能分配、楼层权限、统计增强
-- =============================================

USE permission_system;

-- =============================================
-- 1. 批量操作批次表
-- =============================================
CREATE TABLE IF NOT EXISTS batch_operation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '批次ID',
    batch_no VARCHAR(50) NOT NULL UNIQUE COMMENT '批次号（UUID）',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-批量修改状态，2-批量修改属性，3-批量删除',
    target_status INT COMMENT '目标状态（修改状态时）',
    attr_type VARCHAR(50) COMMENT '属性类型（修改属性时）：orientation/viewType/locationFeatures/specialTags',
    attr_mode TINYINT COMMENT '属性模式：1-覆盖，2-添加，3-移除',
    attr_value TEXT COMMENT '属性值',
    operation_reason VARCHAR(500) NOT NULL COMMENT '操作原因',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    total_count INT DEFAULT 0 COMMENT '总数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    skip_count INT DEFAULT 0 COMMENT '跳过数量',
    status TINYINT DEFAULT 1 COMMENT '批次状态：1-处理中，2-已完成，3-部分失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finish_time DATETIME COMMENT '完成时间',
    INDEX idx_batch_no (batch_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_create_time (create_time),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批量操作批次表';

-- =============================================
-- 2. 批量操作明细表
-- =============================================
CREATE TABLE IF NOT EXISTS batch_operation_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    target_type TINYINT NOT NULL COMMENT '目标类型：1-房间，2-维护单',
    target_id BIGINT NOT NULL COMMENT '目标ID（房间ID或维护单ID）',
    target_no VARCHAR(50) COMMENT '目标编号（房号或维护单号）',
    result_status TINYINT NOT NULL COMMENT '结果状态：1-成功，2-失败，3-跳过',
    fail_reason VARCHAR(500) COMMENT '失败原因',
    old_value TEXT COMMENT '原值',
    new_value TEXT COMMENT '新值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_batch_id (batch_id),
    INDEX idx_batch_no (batch_no),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批量操作明细表';

-- =============================================
-- 3. 用户楼层权限表
-- =============================================
CREATE TABLE IF NOT EXISTS user_floor_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    building_id BIGINT NOT NULL COMMENT '楼栋ID',
    floor_id BIGINT NOT NULL COMMENT '楼层ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_user_floor (user_id, building_id, floor_id),
    INDEX idx_user_id (user_id),
    INDEX idx_building_id (building_id),
    INDEX idx_floor_id (floor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户楼层权限表';

-- =============================================
-- 4. 维护单自动分配规则表
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_assign_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type TINYINT NOT NULL COMMENT '规则类型：1-按维护类型，2-按楼层，3-按优先级',
    condition_value VARCHAR(200) COMMENT '条件值（维护类型ID、楼层范围、优先级值等）',
    target_user_id BIGINT COMMENT '目标用户ID（优先级高于target_role_key）',
    target_role_key VARCHAR(50) COMMENT '目标角色标识',
    priority INT DEFAULT 0 COMMENT '规则优先级（数字大优先）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_type (rule_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护单自动分配规则表';

-- =============================================
-- 5. 维修人员专长表
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_staff_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    skill_type VARCHAR(50) NOT NULL COMMENT '专长类型：对应维护类型或自定义',
    skill_level TINYINT DEFAULT 1 COMMENT '熟练度：1-初级，2-中级，3-高级，4-专家',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_user_skill (user_id, skill_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维修人员专长表';

-- =============================================
-- 6. 补充菜单权限
-- =============================================

-- 批量操作相关按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(154, '房间批量修改状态', 104, 12, '', NULL, 'hotel:room:batch:status', 2, 1, 1, NULL),
(155, '房间批量修改属性', 104, 13, '', NULL, 'hotel:room:batch:attr', 2, 1, 1, NULL),
(156, '房间批量删除', 104, 14, '', NULL, 'hotel:room:batch:delete', 2, 1, 1, NULL),
(157, '批量操作日志查看', 104, 15, '', NULL, 'hotel:room:batch:log', 2, 1, 1, NULL);

-- 楼层权限配置菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(106, '楼层权限配置', 100, 6, '/hotel/floorPermission', 'hotel/FloorPermission', 'hotel:floorPermission:list', 1, 1, 1, 'Key'),
(158, '楼层权限查询', 106, 1, '', NULL, 'hotel:floorPermission:query', 2, 1, 1, NULL),
(159, '楼层权限编辑', 106, 2, '', NULL, 'hotel:floorPermission:edit', 2, 1, 1, NULL);

-- 批量分配维护单权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(221, '维护单批量分配', 201, 11, '', NULL, 'maintenance:order:batch:assign', 2, 1, 1, NULL),
(222, '维护单自动分配配置', 201, 12, '', NULL, 'maintenance:order:autoAssign:config', 2, 1, 1, NULL);

-- 统计导出权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(158, '统计报表导出', 105, 3, '', NULL, 'hotel:dashboard:exportAll', 2, 1, 1, NULL);

-- =============================================
-- 7. 分配角色权限
-- =============================================

-- 超级管理员所有新权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 154), (1, 155), (1, 156), (1, 157),
(1, 106), (1, 158), (1, 159),
(1, 221), (1, 222),
(1, 158);

-- 酒店管理员：批量操作、楼层权限配置、批量分配
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 154), (3, 155), (3, 156), (3, 157),
(3, 106), (3, 158), (3, 159),
(3, 221), (3, 222);

-- 客房部经理：批量修改状态/属性、批量删除
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 154), (5, 155), (5, 156);

-- 前厅部经理：批量修改状态
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 154);

-- 维修部主管：批量分配维护单、自动分配配置
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(9, 221), (9, 222);

-- =============================================
-- 8. 初始化自动分配规则示例数据
-- =============================================

INSERT INTO maintenance_assign_rule (rule_name, rule_type, condition_value, target_role_key, priority, status, remark) VALUES
('设施维修分配给维修组A', 1, '1', 'maintenance_staff', 10, 1, '维护类型1-设施维修'),
('设备更换分配给维修组B', 1, '4', 'maintenance_staff', 10, 1, '维护类型4-设备更换'),
('深度清洁分配给清洁组', 1, '3', 'housekeeper', 10, 1, '维护类型3-深度清洁'),
('紧急优先分配给经验丰富人员', 3, '1', NULL, 20, 1, '优先级1-紧急，结合专长分配');
