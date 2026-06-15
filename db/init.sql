SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS permission_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE permission_system;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_role_name (role_name),
    UNIQUE INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(200) COMMENT '组件路径',
    perms VARCHAR(100) COMMENT '权限标识',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型：0-目录，1-菜单，2-按钮',
    visible TINYINT DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    icon VARCHAR(100) COMMENT '菜单图标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_data_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    scope_type TINYINT DEFAULT 1 COMMENT '权限范围：1-全部，2-自定义，3-本部门，4-本部门及以下，5-仅本人',
    custom_dept_ids VARCHAR(500) COMMENT '自定义部门ID（逗号分隔）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限表';

-- =============================================
-- 酒店信息表
-- =============================================
CREATE TABLE IF NOT EXISTS hotel_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '酒店ID',
    hotel_name VARCHAR(100) NOT NULL COMMENT '酒店名称',
    brand VARCHAR(100) COMMENT '品牌',
    star_rating TINYINT COMMENT '星级：3-三星，4-四星，5-五星',
    address VARCHAR(500) COMMENT '地址',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    license_number VARCHAR(100) COMMENT '营业执照号',
    opening_date DATE COMMENT '开业时间',
    description TEXT COMMENT '酒店简介',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店信息表';

-- =============================================
-- 酒店设施表
-- =============================================
CREATE TABLE IF NOT EXISTS hotel_facility (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设施ID',
    hotel_id BIGINT NOT NULL COMMENT '酒店ID',
    facility_name VARCHAR(50) NOT NULL COMMENT '设施名称',
    facility_icon VARCHAR(100) COMMENT '设施图标',
    open_time VARCHAR(100) COMMENT '开放时间',
    description VARCHAR(500) COMMENT '设施描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店设施表';

-- =============================================
-- 酒店图片表
-- =============================================
CREATE TABLE IF NOT EXISTS hotel_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    ref_type VARCHAR(20) NOT NULL COMMENT '关联类型：hotel-酒店，roomType-房型',
    ref_id BIGINT NOT NULL COMMENT '关联ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    is_main TINYINT DEFAULT 0 COMMENT '是否主图：0-否，1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ref (ref_type, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店图片表';

-- =============================================
-- 楼栋表
-- =============================================
CREATE TABLE IF NOT EXISTS building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼栋ID',
    building_name VARCHAR(50) NOT NULL COMMENT '楼栋名称',
    building_code VARCHAR(50) NOT NULL COMMENT '楼栋编号',
    total_floors INT DEFAULT 0 COMMENT '总楼层数',
    description VARCHAR(500) COMMENT '楼栋简介',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_building_code (building_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼栋表';

-- =============================================
-- 楼层表
-- =============================================
CREATE TABLE IF NOT EXISTS floor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼层ID',
    building_id BIGINT NOT NULL COMMENT '楼栋ID',
    floor_number INT NOT NULL COMMENT '楼层号',
    floor_name VARCHAR(50) COMMENT '楼层名称',
    features VARCHAR(500) COMMENT '楼层特点：无烟、安静区域、高楼层等（逗号分隔）',
    room_count INT DEFAULT 0 COMMENT '房间数量（自动统计）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，2-维修中，3-暂停',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_building_id (building_id),
    UNIQUE INDEX idx_building_floor (building_id, floor_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼层表';

-- =============================================
-- 房型表
-- =============================================
CREATE TABLE IF NOT EXISTS room_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房型ID',
    type_name VARCHAR(50) NOT NULL COMMENT '房型名称',
    type_code VARCHAR(20) NOT NULL COMMENT '房型编码',
    area DECIMAL(8,2) COMMENT '面积（平方米）',
    bed_type VARCHAR(20) COMMENT '床型：single-单人床，king-大床，twin-双床',
    max_occupancy INT DEFAULT 1 COMMENT '最大入住人数',
    extra_bed_policy VARCHAR(200) COMMENT '加床政策',
    facilities TEXT COMMENT '设施清单（JSON）',
    base_price DECIMAL(10,2) COMMENT '平日价格',
    weekend_price DECIMAL(10,2) COMMENT '周末价格',
    cost_price DECIMAL(10,2) COMMENT '成本价',
    sale_status TINYINT DEFAULT 1 COMMENT '销售状态：1-在售，0-停售',
    description TEXT COMMENT '房型描述',
    room_count INT DEFAULT 0 COMMENT '房间数量（自动统计）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房型表';

-- =============================================
-- 房间表
-- =============================================
CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    building_id BIGINT NOT NULL COMMENT '楼栋ID',
    floor_id BIGINT NOT NULL COMMENT '楼层ID',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    orientation VARCHAR(20) COMMENT '朝向：东/南/西/北/东南/东北/西南/西北',
    view_type VARCHAR(50) COMMENT '景观：江景/海景/山景/园景/城景等',
    location_features VARCHAR(500) COMMENT '位置特点（逗号分隔）：靠近电梯/转角房/安静区域等',
    special_tags VARCHAR(500) COMMENT '特殊标识（逗号分隔）：残疾人房/连通房/禁烟房/允许宠物等',
    status TINYINT DEFAULT 1 COMMENT '房间状态：1-空闲，2-已预订，3-已入住，4-待清洁，5-清洁中，6-维修中，7-停用',
    remark VARCHAR(500) COMMENT '房间备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_room_number (room_number),
    INDEX idx_building_id (building_id),
    INDEX idx_floor_id (floor_id),
    INDEX idx_room_type_id (room_type_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- =============================================
-- 房间状态变更记录表
-- =============================================
CREATE TABLE IF NOT EXISTS room_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    operator VARCHAR(50) COMMENT '操作人',
    operator_id BIGINT COMMENT '操作人ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_room_id (room_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间状态变更记录表';

-- =============================================
-- 初始化数据
-- =============================================

INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(1, '超级管理员', 'admin', 1, 1, '拥有系统所有权限'),
(2, '普通用户', 'user', 2, 1, '普通用户角色'),
(3, '酒店管理员', 'hotel_admin', 3, 1, '酒店管理员，完全权限'),
(4, '前厅部经理', 'frontdesk_manager', 4, 1, '前厅部经理，可修改房间属性和状态'),
(5, '客房部经理', 'housekeeping_manager', 5, 1, '客房部经理，可修改房间状态和房型设施'),
(6, '普通前台', 'receptionist', 6, 1, '普通前台员工，只读有限信息'),
(7, '财务人员', 'finance_staff', 7, 1, '财务部门，可查看价格成本信息');

INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '超级管理员', 'admin@example.com', '13800138000', 1),
(2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '测试用户', 'test@example.com', '13800138001', 1),
(3, 'hotel_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '酒店管理员', 'hotel_admin@example.com', '13800138002', 1),
(4, 'frontdesk_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '前厅部经理', 'frontdesk@example.com', '13800138003', 1),
(5, 'housekeeping_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '客房部经理', 'housekeeping@example.com', '13800138004', 1),
(6, 'receptionist', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '普通前台', 'receptionist@example.com', '13800138005', 1),
(7, 'finance_staff', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '财务人员', 'finance@example.com', '13800138006', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7);

-- 系统管理菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1, '系统管理', 0, 2, '/system', NULL, NULL, 0, 1, 1, 'Setting'),
(2, '用户管理', 1, 1, '/system/user', 'system/User', 'system:user:list', 1, 1, 1, 'User'),
(3, '角色管理', 1, 2, '/system/role', 'system/Role', 'system:role:list', 1, 1, 1, 'Avatar'),
(4, '菜单管理', 1, 3, '/system/menu', 'system/Menu', 'system:menu:list', 1, 1, 1, 'Menu'),
(5, '数据权限', 1, 4, '/system/dataPerm', 'system/DataPermission', 'system:dataPerm:list', 1, 1, 1, 'Lock');

-- 用户管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(21, '用户查询', 2, 1, '', NULL, 'system:user:query', 2, 1, 1, NULL),
(22, '用户新增', 2, 2, '', NULL, 'system:user:add', 2, 1, 1, NULL),
(23, '用户修改', 2, 3, '', NULL, 'system:user:edit', 2, 1, 1, NULL),
(24, '用户删除', 2, 4, '', NULL, 'system:user:delete', 2, 1, 1, NULL),
(25, '重置密码', 2, 5, '', NULL, 'system:user:resetPwd', 2, 1, 1, NULL);

-- 角色管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(31, '角色查询', 3, 1, '', NULL, 'system:role:query', 2, 1, 1, NULL),
(32, '角色新增', 3, 2, '', NULL, 'system:role:add', 2, 1, 1, NULL),
(33, '角色修改', 3, 3, '', NULL, 'system:role:edit', 2, 1, 1, NULL),
(34, '角色删除', 3, 4, '', NULL, 'system:role:delete', 2, 1, 1, NULL);

-- 菜单管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(41, '菜单查询', 4, 1, '', NULL, 'system:menu:query', 2, 1, 1, NULL),
(42, '菜单新增', 4, 2, '', NULL, 'system:menu:add', 2, 1, 1, NULL),
(43, '菜单修改', 4, 3, '', NULL, 'system:menu:edit', 2, 1, 1, NULL),
(44, '菜单删除', 4, 4, '', NULL, 'system:menu:delete', 2, 1, 1, NULL);

-- 数据权限按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(51, '数据权限查询', 5, 1, '', NULL, 'system:dataPerm:query', 2, 1, 1, NULL),
(52, '数据权限编辑', 5, 2, '', NULL, 'system:dataPerm:edit', 2, 1, 1, NULL),
(53, '数据权限删除', 5, 3, '', NULL, 'system:dataPerm:delete', 2, 1, 1, NULL);

-- =============================================
-- 酒店管理菜单 (ID range: 100-199)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(100, '酒店管理', 0, 1, '/hotel', NULL, NULL, 0, 1, 1, 'House'),
(101, '酒店概览', 100, 1, '/hotel/overview', 'hotel/HotelOverview', 'hotel:info:list', 1, 1, 1, 'OfficeBuilding'),
(102, '楼栋楼层', 100, 2, '/hotel/building', 'hotel/BuildingFloor', 'hotel:building:list', 1, 1, 1, 'School'),
(103, '房型管理', 100, 3, '/hotel/roomType', 'hotel/RoomType', 'hotel:roomType:list', 1, 1, 1, 'Tickets'),
(104, '房间管理', 100, 4, '/hotel/room', 'hotel/RoomManage', 'hotel:room:list', 1, 1, 1, 'Key'),
(105, '统计看板', 100, 5, '/hotel/dashboard', 'hotel/HotelDashboard', 'hotel:dashboard:list', 1, 1, 1, 'DataAnalysis');

-- 酒店信息按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(111, '酒店信息查询', 101, 1, '', NULL, 'hotel:info:query', 2, 1, 1, NULL),
(112, '酒店信息编辑', 101, 2, '', NULL, 'hotel:info:edit', 2, 1, 1, NULL),
(113, '酒店设施管理', 101, 3, '', NULL, 'hotel:facility:edit', 2, 1, 1, NULL),
(114, '酒店图片管理', 101, 4, '', NULL, 'hotel:image:edit', 2, 1, 1, NULL);

-- 楼栋管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(121, '楼栋查询', 102, 1, '', NULL, 'hotel:building:query', 2, 1, 1, NULL),
(122, '楼栋新增', 102, 2, '', NULL, 'hotel:building:add', 2, 1, 1, NULL),
(123, '楼栋修改', 102, 3, '', NULL, 'hotel:building:edit', 2, 1, 1, NULL),
(124, '楼栋删除', 102, 4, '', NULL, 'hotel:building:delete', 2, 1, 1, NULL),
(125, '楼层查询', 102, 5, '', NULL, 'hotel:floor:query', 2, 1, 1, NULL),
(126, '楼层新增', 102, 6, '', NULL, 'hotel:floor:add', 2, 1, 1, NULL),
(127, '楼层修改', 102, 7, '', NULL, 'hotel:floor:edit', 2, 1, 1, NULL),
(128, '楼层删除', 102, 8, '', NULL, 'hotel:floor:delete', 2, 1, 1, NULL);

-- 房型管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(131, '房型查询', 103, 1, '', NULL, 'hotel:roomType:query', 2, 1, 1, NULL),
(132, '房型新增', 103, 2, '', NULL, 'hotel:roomType:add', 2, 1, 1, NULL),
(133, '房型修改', 103, 3, '', NULL, 'hotel:roomType:edit', 2, 1, 1, NULL),
(134, '房型删除', 103, 4, '', NULL, 'hotel:roomType:delete', 2, 1, 1, NULL),
(135, '房型价格查看', 103, 5, '', NULL, 'hotel:roomType:price:view', 2, 1, 1, NULL),
(136, '房型价格编辑', 103, 6, '', NULL, 'hotel:roomType:price:edit', 2, 1, 1, NULL),
(137, '房型成本价查看', 103, 7, '', NULL, 'hotel:roomType:cost:view', 2, 1, 1, NULL);

-- 房间管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(141, '房间查询', 104, 1, '', NULL, 'hotel:room:query', 2, 1, 1, NULL),
(142, '房间新增', 104, 2, '', NULL, 'hotel:room:add', 2, 1, 1, NULL),
(143, '房间修改', 104, 3, '', NULL, 'hotel:room:edit', 2, 1, 1, NULL),
(144, '房间删除', 104, 4, '', NULL, 'hotel:room:delete', 2, 1, 1, NULL),
(145, '房间状态修改', 104, 5, '', NULL, 'hotel:room:status:edit', 2, 1, 1, NULL),
(146, '房间备注编辑', 104, 6, '', NULL, 'hotel:room:remark:edit', 2, 1, 1, NULL),
(147, '房间批量创建', 104, 7, '', NULL, 'hotel:room:batch:add', 2, 1, 1, NULL),
(148, '房间内部备注查看', 104, 8, '', NULL, 'hotel:room:remark:view', 2, 1, 1, NULL);

-- 统计看板按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(151, '统计查看', 105, 1, '', NULL, 'hotel:dashboard:query', 2, 1, 1, NULL),
(152, '价格导出', 105, 2, '', NULL, 'hotel:dashboard:export', 2, 1, 1, NULL);

-- =============================================
-- 超级管理员：所有权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 21), (1, 22), (1, 23), (1, 24), (1, 25),
(1, 31), (1, 32), (1, 33), (1, 34),
(1, 41), (1, 42), (1, 43), (1, 44),
(1, 51), (1, 52), (1, 53),
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 105),
(1, 111), (1, 112), (1, 113), (1, 114),
(1, 121), (1, 122), (1, 123), (1, 124), (1, 125), (1, 126), (1, 127), (1, 128),
(1, 131), (1, 132), (1, 133), (1, 134), (1, 135), (1, 136), (1, 137),
(1, 141), (1, 142), (1, 143), (1, 144), (1, 145), (1, 146), (1, 147), (1, 148),
(1, 151), (1, 152);

-- =============================================
-- 酒店管理员(hotel_admin)：完全权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 100), (3, 101), (3, 102), (3, 103), (3, 104), (3, 105),
(3, 111), (3, 112), (3, 113), (3, 114),
(3, 121), (3, 122), (3, 123), (3, 124), (3, 125), (3, 126), (3, 127), (3, 128),
(3, 131), (3, 132), (3, 133), (3, 134), (3, 135), (3, 136), (3, 137),
(3, 141), (3, 142), (3, 143), (3, 144), (3, 145), (3, 146), (3, 147), (3, 148),
(3, 151), (3, 152);

-- =============================================
-- 前厅部经理(frontdesk_manager)：
-- 查看所有信息，修改房间属性和状态，添加备注
-- 不能修改价格和成本价，不能删除楼栋楼层房型
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 100), (4, 101), (4, 102), (4, 103), (4, 104), (4, 105),
(4, 111),
(4, 121), (4, 125),
(4, 131), (4, 135),
(4, 141), (4, 143), (4, 145), (4, 146), (4, 148),
(4, 151);

-- =============================================
-- 客房部经理(housekeeping_manager)：
-- 查看所有信息，修改房间状态、备注、房型设施
-- 不能修改价格，不能删除数据
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 100), (5, 101), (5, 102), (5, 103), (5, 104), (5, 105),
(5, 111),
(5, 121), (5, 125),
(5, 131), (5, 133), (5, 135),
(5, 141), (5, 145), (5, 146), (5, 148),
(5, 151);

-- =============================================
-- 普通前台(receptionist)：
-- 只看空闲和已预订房间，基本信息，无成本价和内部备注
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 100), (6, 104),
(6, 141);

-- =============================================
-- 财务人员(finance_staff)：
-- 查看价格成本信息，导出价格表，不能修改物理属性和房间状态
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 100), (7, 101), (7, 103), (7, 104), (7, 105),
(7, 111),
(7, 131), (7, 135), (7, 137),
(7, 141),
(7, 151), (7, 152);

-- 普通用户只有查看权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 3),
(2, 21), (2, 31);

-- 数据权限初始化
INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(1, 1, NULL),
(2, 5, NULL),
(3, 1, NULL),
(4, 1, NULL),
(5, 1, NULL),
(6, 1, NULL),
(7, 1, NULL);

-- =============================================
-- 保存的筛选方案表
-- =============================================
CREATE TABLE IF NOT EXISTS saved_filter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    filter_name VARCHAR(100) NOT NULL COMMENT '方案名称',
    filter_config TEXT NOT NULL COMMENT '筛选条件JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保存的筛选方案表';

-- 新增房间管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(149, '房间复制', 104, 9, '', NULL, 'hotel:room:copy', 2, 1, 1, NULL),
(150, '模板应用', 104, 10, '', NULL, 'hotel:room:template', 2, 1, 1, NULL),
(153, '房间数据导出', 104, 11, '', NULL, 'hotel:room:export', 2, 1, 1, NULL);

-- 酒店管理员：新增权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 149), (3, 150), (3, 153);

-- 前厅部经理：复制和模板权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 149), (4, 150);

-- 客房部经理：复制权限，导出基础数据权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 149), (5, 153);

-- =============================================
-- 客房服务员角色 (housekeeper)
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(8, '客房服务员', 'housekeeper', 8, 1, '客房服务员，只能看待清洁和清洁中的房间');

INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(8, 'housekeeper', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '客房服务员', 'housekeeper@example.com', '13800138007', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(8, 8);

-- 客房服务员：只看房间列表和查询（待清洁和清洁中由前端限制）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(8, 100), (8, 104),
(8, 141);

INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(8, 1, NULL);

-- =============================================
-- 补充：前厅部经理增加房间数据导出权限（不含成本价）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 153);

-- =============================================
-- 房间变更日志表（完整记录所有房间变更）
-- =============================================
CREATE TABLE IF NOT EXISTS room_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    operator VARCHAR(50) COMMENT '操作人账号',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    operator_role VARCHAR(50) COMMENT '操作人角色',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-创建，2-修改，3-状态变更，4-删除，5-维护单关联',
    change_field VARCHAR(100) COMMENT '变更字段',
    old_value TEXT COMMENT '原值',
    new_value TEXT COMMENT '新值',
    change_reason VARCHAR(500) COMMENT '变更原因',
    related_order_no VARCHAR(50) COMMENT '关联维护单号',
    related_order_id BIGINT COMMENT '关联维护单ID',
    terminal_type VARCHAR(20) DEFAULT 'PC' COMMENT '操作终端：PC端/移动端',
    terminal_ip VARCHAR(50) COMMENT '操作终端IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    INDEX idx_room_id (room_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_create_time (create_time),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间变更日志表';

-- =============================================
-- 维护单表
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '维护单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '维护单号：WH+年月日+流水号',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    maintenance_type TINYINT NOT NULL COMMENT '维护类型：1-设施维修，2-定期保养，3-深度清洁，4-设备更换，5-装修改造',
    priority TINYINT NOT NULL COMMENT '优先级：1-紧急，2-高，3-中，4-低',
    problem_description TEXT NOT NULL COMMENT '问题描述',
    expected_finish_time DATETIME COMMENT '预计完成时间',
    special_remark VARCHAR(500) COMMENT '特殊说明',
    status TINYINT DEFAULT 1 NOT NULL COMMENT '状态：1-待分配，2-处理中，3-已完成，4-已验收，5-已关闭',
    assigned_user_id BIGINT COMMENT '分配的维修人员ID',
    assigned_user_name VARCHAR(50) COMMENT '分配的维修人员姓名',
    assign_time DATETIME COMMENT '分配时间',
    accept_time DATETIME COMMENT '接单时间',
    actual_hours DECIMAL(8,2) COMMENT '实际用时（小时）',
    used_parts TEXT COMMENT '使用的配件及数量',
    maintenance_cost DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用（元）',
    maintenance_description TEXT COMMENT '维护说明',
    finish_time DATETIME COMMENT '完成时间',
    inspector_id BIGINT COMMENT '验收人ID',
    inspector_name VARCHAR(50) COMMENT '验收人姓名',
    inspect_time DATETIME COMMENT '验收时间',
    inspect_result TINYINT COMMENT '验收结果：1-通过，2-不通过',
    inspect_opinion TEXT COMMENT '验收意见',
    rectification_requirement TEXT COMMENT '整改要求（不通过时填写）',
    create_user_id BIGINT NOT NULL COMMENT '创建人ID',
    create_user_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_room_id (room_id),
    INDEX idx_status (status),
    INDEX idx_maintenance_type (maintenance_type),
    INDEX idx_priority (priority),
    INDEX idx_assigned_user_id (assigned_user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护单表';

-- =============================================
-- 维护单照片表
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '照片ID',
    order_id BIGINT NOT NULL COMMENT '维护单ID',
    photo_type TINYINT NOT NULL COMMENT '照片类型：1-问题照片，2-维护后照片',
    photo_url VARCHAR(500) NOT NULL COMMENT '照片URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_photo_type (photo_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护单照片表';

-- =============================================
-- 维护单状态流转日志表
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    order_id BIGINT NOT NULL COMMENT '维护单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '维护单号',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark VARCHAR(500) COMMENT '备注说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_order_id (order_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护单状态流转日志表';

-- =============================================
-- 新增角色：维修部主管、维修人员
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(9, '维修部主管', 'maintenance_manager', 9, 1, '维修部主管，可分配维护单、查看统计'),
(10, '维修人员', 'maintenance_staff', 10, 1, '维修人员，处理分配给自己的维护单');

-- =============================================
-- 新增测试用户
-- =============================================
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(9, 'maintenance_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '维修部主管', 'maintenance_mgr@example.com', '13800138008', 1),
(10, 'maintenance_staff_a', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '维修人员A', 'maintenance_a@example.com', '13800138009', 1),
(11, 'maintenance_staff_b', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '维修人员B', 'maintenance_b@example.com', '13800138010', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(9, 9),
(10, 10),
(11, 10);

INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(9, 1, NULL),
(10, 1, NULL);

-- =============================================
-- 维护管理菜单 (ID range: 200-299)
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(200, '维护管理', 0, 3, '/maintenance', NULL, NULL, 0, 1, 1, 'Tools'),
(201, '维护单管理', 200, 1, '/maintenance/order', 'maintenance/MaintenanceOrderList', 'maintenance:order:list', 1, 1, 1, 'Document'),
(202, '创建维护单', 200, 2, '/maintenance/order/create', 'maintenance/MaintenanceOrderCreate', 'maintenance:order:add', 1, 1, 1, 'Edit'),
(203, '房间变更日志', 200, 3, '/maintenance/changeLog', 'maintenance/RoomChangeLog', 'maintenance:changeLog:list', 1, 1, 1, 'Clock'),
(204, '维护统计报表', 200, 4, '/maintenance/statistics', 'maintenance/MaintenanceStatistics', 'maintenance:statistics:list', 1, 1, 1, 'DataLine');

-- 维护单按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(211, '维护单查询', 201, 1, '', NULL, 'maintenance:order:query', 2, 1, 1, NULL),
(212, '维护单新增', 201, 2, '', NULL, 'maintenance:order:add', 2, 1, 1, NULL),
(213, '维护单编辑', 201, 3, '', NULL, 'maintenance:order:edit', 2, 1, 1, NULL),
(214, '维护单删除', 201, 4, '', NULL, 'maintenance:order:delete', 2, 1, 1, NULL),
(215, '维护单分配', 201, 5, '', NULL, 'maintenance:order:assign', 2, 1, 1, NULL),
(216, '维护单接单', 201, 6, '', NULL, 'maintenance:order:accept', 2, 1, 1, NULL),
(217, '维护单提交完成', 201, 7, '', NULL, 'maintenance:order:finish', 2, 1, 1, NULL),
(218, '维护单验收', 201, 8, '', NULL, 'maintenance:order:inspect', 2, 1, 1, NULL),
(219, '维护费用查看', 201, 9, '', NULL, 'maintenance:order:cost:view', 2, 1, 1, NULL),
(220, '维护单导出', 201, 10, '', NULL, 'maintenance:order:export', 2, 1, 1, NULL);

-- 变更日志按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(231, '变更日志查询', 203, 1, '', NULL, 'maintenance:changeLog:query', 2, 1, 1, NULL);

-- 统计报表按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(241, '统计查询', 204, 1, '', NULL, 'maintenance:statistics:query', 2, 1, 1, NULL),
(242, '统计导出', 204, 2, '', NULL, 'maintenance:statistics:export', 2, 1, 1, NULL);

-- =============================================
-- 超级管理员：维护管理所有权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 200), (1, 201), (1, 202), (1, 203), (1, 204),
(1, 211), (1, 212), (1, 213), (1, 214), (1, 215), (1, 216), (1, 217), (1, 218), (1, 219), (1, 220),
(1, 231),
(1, 241), (1, 242);

-- =============================================
-- 酒店管理员(hotel_admin)：维护管理所有权限（不含系统管理）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 200), (3, 201), (3, 202), (3, 203), (3, 204),
(3, 211), (3, 212), (3, 213), (3, 214), (3, 215), (3, 216), (3, 217), (3, 218), (3, 219), (3, 220),
(3, 231),
(3, 241), (3, 242);

-- =============================================
-- 客房部经理(housekeeping_manager)：
-- 可创建维护单、验收、查看日志和统计
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 200), (5, 201), (5, 202), (5, 203), (5, 204),
(5, 211), (5, 212), (5, 218), (5, 219),
(5, 231),
(5, 241);

-- =============================================
-- 前厅部经理(frontdesk_manager)：
-- 可查看维护单、查看日志
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 200), (4, 201), (4, 203),
(4, 211), (4, 212),
(4, 231);

-- =============================================
-- 维修部主管(maintenance_manager)：
-- 可查看所有维护单、分配、查看统计
-- 不能验收
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(9, 200), (9, 201), (9, 203), (9, 204),
(9, 211), (9, 215), (9, 219),
(9, 231),
(9, 241);

-- =============================================
-- 维修人员(maintenance_staff)：
-- 只能查看分配给自己的维护单、接单、提交完成
-- 查看自己的统计
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(10, 200), (10, 201),
(10, 211), (10, 216), (10, 217);

-- =============================================
-- 普通前台(receptionist)：
-- 可查看维护单（只读）、查看房间维护历史
-- 看不到维修费用
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 200), (6, 201),
(6, 211);

-- =============================================
-- 财务人员(finance_staff)：
-- 可查看维护单、费用、统计导出
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(7, 200), (7, 201), (7, 204),
(7, 211), (7, 219), (7, 220),
(7, 241), (7, 242);
