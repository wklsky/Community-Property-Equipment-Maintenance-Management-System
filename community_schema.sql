-- ============================================================
-- 社区物业设备维护管理系统 - 完整表结构 (Schema)
-- ============================================================

-- DROP DATABASE IF EXISTS property_system;
CREATE DATABASE IF NOT EXISTS property_system DEFAULT CHARSET utf8mb4;
USE property_system;

-- ===================== 1. 租户 =====================
CREATE TABLE sys_tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================== 2. 用户与权限 =====================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID（超级管理员为 0）',
    username VARCHAR(50),
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status TINYINT DEFAULT 1,
    is_super_admin TINYINT DEFAULT 0 NOT NULL COMMENT '是否超级管理员: 0=否, 1=是（跨租户管理权限）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone (tenant_id, phone)
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    role_code VARCHAR(50) COMMENT '角色编码: ADMIN/WORKER/OWNER',
    role_name VARCHAR(50) COMMENT '角色名称: 系统管理员/维修工/业主',
    description VARCHAR(200) COMMENT '角色描述',
    status INT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL
);

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    role_id BIGINT,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

-- ===================== 3. 房产 =====================
CREATE TABLE community (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100)
);

CREATE TABLE building (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    community_id BIGINT,
    name VARCHAR(50)
);

CREATE TABLE room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    building_id BIGINT,
    room_no VARCHAR(50)
);

CREATE TABLE property (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    room_id BIGINT,
    owner_id BIGINT,
    is_default INT DEFAULT 0
);

CREATE TABLE user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    address VARCHAR(200) NOT NULL COMMENT '详细地址',
    is_default INT DEFAULT 0 COMMENT '默认地址: 1是 0否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_address (tenant_id, user_id)
);

-- ===================== 4. 设备 =====================
CREATE TABLE device_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    name VARCHAR(100)
);

CREATE TABLE device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    building_id BIGINT COMMENT '所属楼栋',
    category_id BIGINT,
    name VARCHAR(100),
    model VARCHAR(100) COMMENT '设备型号',
    location VARCHAR(200),
    status TINYINT DEFAULT 1,
    install_date DATE COMMENT '安装日期',
    qr_code_url VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================== 5. 巡检 =====================
CREATE TABLE inspection_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    name VARCHAR(100) COMMENT '计划名称',
    building_id BIGINT COMMENT '楼栋ID',
    category_id BIGINT COMMENT '设备类型ID',
    device_id BIGINT,
    cycle INT,
    next_time DATETIME,
    status TINYINT DEFAULT 0 COMMENT '状态:0草稿,1已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inspection_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    plan_id BIGINT,
    plan_name VARCHAR(100) COMMENT '计划名称',
    building_id BIGINT COMMENT '楼栋ID',
    category_id BIGINT COMMENT '设备类型ID',
    device_id BIGINT,
    assigned_to BIGINT COMMENT '执行人(维修工)',
    task_date DATE,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inspection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    task_id BIGINT,
    device_id BIGINT,
    result TINYINT,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================== 6. 工单 =====================
CREATE TABLE repair_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    device_id BIGINT,
    address VARCHAR(255),
    fault_desc VARCHAR(500),

    status TINYINT DEFAULT 0,
    priority TINYINT DEFAULT 1,

    assign_to BIGINT,
    appoint_time DATETIME,
    finish_time DATETIME,

    process_desc TEXT,
    result_images VARCHAR(1000),
    transfer_reason VARCHAR(255),

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_order_no (tenant_id, order_no),
    KEY idx_status (tenant_id, status),
    KEY idx_user (tenant_id, user_id)
);

CREATE TABLE repair_order_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    from_status TINYINT,
    to_status TINYINT,
    action VARCHAR(50)
);

CREATE TABLE repair_order_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    order_id BIGINT,
    operator_id BIGINT,
    action VARCHAR(50),
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_order_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    order_id BIGINT,
    rating TINYINT,
    comment VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_order_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    order_id BIGINT,
    material_name VARCHAR(100),
    quantity INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ===================== 7. 公告 =====================
CREATE TABLE notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    title VARCHAR(200),
    content TEXT,
    publish_status TINYINT DEFAULT 0,
    scheduled_time DATETIME COMMENT '定时发送时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notice_target (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    notice_id BIGINT,
    building_id BIGINT
);

CREATE TABLE notice_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    notice_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_time DATETIME DEFAULT NULL
);

-- ===================== 8. 消息与字典 =====================
CREATE TABLE message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    user_id BIGINT,
    type VARCHAR(50),
    content VARCHAR(500),
    is_read TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE biz_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    biz_type VARCHAR(50),
    biz_id BIGINT,
    file_type VARCHAR(20),
    url VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    dict_type VARCHAR(50),
    dict_key VARCHAR(50),
    dict_value VARCHAR(100)
);
