-- =====================================================
-- IntelliHub IAM 数据库初始化脚本
-- 数据库: intellihub_iam
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `intellihub_iam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `intellihub_iam`;

-- =====================================================
-- 1. 租户表
-- =====================================================
DROP TABLE IF EXISTS `iam_tenant`;
CREATE TABLE `iam_tenant` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `name` varchar(128) NOT NULL COMMENT '租户名称',
    `code` varchar(64) NOT NULL COMMENT '租户编码',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `logo` varchar(256) DEFAULT NULL COMMENT 'Logo URL',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
    `max_users` int(11) DEFAULT 100 COMMENT '最大用户数',
    `max_apps` int(11) DEFAULT 10 COMMENT '最大应用数',
    `max_apis` int(11) DEFAULT 100 COMMENT '最大API数',
    `max_calls_per_day` bigint(20) DEFAULT 100000 COMMENT '每日最大调用次数',
    `max_calls_per_month` bigint(20) DEFAULT 3000000 COMMENT '每月最大调用次数',
    `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人',
    `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
    `contact_email` varchar(128) DEFAULT NULL COMMENT '联系邮箱',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- =====================================================
-- 2. 用户表
-- =====================================================
DROP TABLE IF EXISTS `iam_user`;
CREATE TABLE `iam_user` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
    `username` varchar(64) NOT NULL COMMENT '用户名',
    `password` varchar(128) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
    `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
    `avatar` varchar(256) DEFAULT NULL COMMENT '头像URL',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive/locked',
    `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
    `login_fail_count` int(11) DEFAULT 0 COMMENT '登录失败次数',
    `locked_until` datetime DEFAULT NULL COMMENT '锁定截止时间',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 3. 角色表
-- =====================================================
DROP TABLE IF EXISTS `iam_role`;
CREATE TABLE `iam_role` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(32) DEFAULT NULL COMMENT '租户ID（NULL表示系统角色）',
    `code` varchar(64) NOT NULL COMMENT '角色编码',
    `name` varchar(64) NOT NULL COMMENT '角色名称',
    `description` varchar(256) DEFAULT NULL COMMENT '描述',
    `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统内置',
    `sort` int(11) DEFAULT 0 COMMENT '排序',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =====================================================
-- 4. 用户角色关联表
-- =====================================================
DROP TABLE IF EXISTS `iam_user_role`;
CREATE TABLE `iam_user_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `role_id` varchar(32) NOT NULL COMMENT '角色ID',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =====================================================
-- 5. 权限表
-- =====================================================
DROP TABLE IF EXISTS `iam_permission`;
CREATE TABLE `iam_permission` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `code` varchar(128) NOT NULL COMMENT '权限编码',
    `name` varchar(64) NOT NULL COMMENT '权限名称',
    `group_name` varchar(64) DEFAULT NULL COMMENT '权限分组',
    `description` varchar(256) DEFAULT NULL COMMENT '描述',
    `sort` int(11) DEFAULT 0 COMMENT '排序',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- =====================================================
-- 6. 角色权限关联表
-- =====================================================
DROP TABLE IF EXISTS `iam_role_permission`;
CREATE TABLE `iam_role_permission` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` varchar(32) NOT NULL COMMENT '角色ID',
    `permission_id` varchar(32) NOT NULL COMMENT '权限ID',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =====================================================
-- 7. 菜单表
-- =====================================================
DROP TABLE IF EXISTS `iam_menu`;
CREATE TABLE `iam_menu` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `parent_id` varchar(32) DEFAULT NULL COMMENT '父级ID',
    `name` varchar(64) NOT NULL COMMENT '菜单名称',
    `path` varchar(256) DEFAULT NULL COMMENT '路由路径',
    `component` varchar(256) DEFAULT NULL COMMENT '组件路径',
    `icon` varchar(64) DEFAULT NULL COMMENT '图标',
    `permission` varchar(128) DEFAULT NULL COMMENT '权限编码',
    `type` int(11) DEFAULT 1 COMMENT '类型：1-菜单，2-按钮',
    `visible` tinyint(1) DEFAULT 1 COMMENT '是否可见',
    `sort` int(11) DEFAULT 0 COMMENT '排序',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- =====================================================
-- 8. 登录日志表
-- =====================================================
DROP TABLE IF EXISTS `iam_login_log`;
CREATE TABLE `iam_login_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
    `username` varchar(64) NOT NULL COMMENT '用户名',
    `tenant_id` varchar(32) DEFAULT NULL COMMENT '租户ID',
    `login_type` varchar(32) NOT NULL COMMENT '登录类型：password/sms/oauth',
    `login_result` varchar(20) NOT NULL COMMENT '登录结果：success/fail',
    `fail_reason` varchar(256) DEFAULT NULL COMMENT '失败原因',
    `ip` varchar(64) DEFAULT NULL COMMENT 'IP地址',
    `location` varchar(128) DEFAULT NULL COMMENT '登录地点',
    `user_agent` varchar(512) DEFAULT NULL COMMENT 'User-Agent',
    `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 1. 创建默认租户
INSERT INTO `iam_tenant` (`id`, `name`, `code`, `description`, `status`, `max_users`, `max_apps`, `max_apis`, `contact_name`, `contact_email`) VALUES
('1', '平台管理', 'platform', '平台管理租户', 'active', 1000, 100, 1000, '系统管理员', 'admin@intellihub.com'),
('2', '示例租户', 'demo', '示例租户', 'active', 100, 10, 100, '张三', 'zhangsan@example.com');

-- 2. 创建系统核心角色（3个核心角色）
INSERT INTO `iam_role` (`id`, `tenant_id`, `code`, `name`, `description`, `is_system`, `sort`, `status`) VALUES
('1', NULL, 'platform_admin', '超级管理员', '跨租户全权限，可管理所有租户、用户、系统配置', 1, 1, 'active'),
('2', NULL, 'tenant_admin', '租户管理员', '本租户管理权限，可管理本租户内的API、应用、用户', 1, 2, 'active'),
('3', NULL, 'user', '普通用户', '基础使用权限，可浏览API市场、使用已授权的API', 1, 3, 'active');

-- 3. 创建示例用户 (密码: admin123，使用BCrypt加密)
-- BCrypt加密后的密码: $2a$10$SxdXvMth5xuhQoe6NLIZVOBQhtq.3/US4eVfjD/0t4bwO3yVRV15u
INSERT INTO `iam_user` (`id`, `tenant_id`, `username`, `password`, `nickname`, `email`, `status`) VALUES
('1', '1', 'admin', '$2a$10$SxdXvMth5xuhQoe6NLIZVOBQhtq.3/US4eVfjD/0t4bwO3yVRV15u', '超级管理员', 'admin@intellihub.com', 'active'),
('2', '2', 'tenant_admin', '$2a$10$SxdXvMth5xuhQoe6NLIZVOBQhtq.3/US4eVfjD/0t4bwO3yVRV15u', '租户管理员', 'tenant@example.com', 'active'),
('3', '2', 'user', '$2a$10$SxdXvMth5xuhQoe6NLIZVOBQhtq.3/US4eVfjD/0t4bwO3yVRV15u', '普通用户', 'user@example.com', 'active');

-- 4. 分配角色
INSERT INTO `iam_user_role` (`user_id`, `role_id`) VALUES
('1', '1'),  -- admin -> platform_admin (超级管理员)
('2', '2'),  -- tenant_admin -> tenant_admin (租户管理员)
('3', '3');  -- user -> user (普通用户)

-- 5. 创建基础菜单
INSERT INTO `iam_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `permission`, `type`, `visible`, `sort`) VALUES
-- 首页（所有用户可见）
('1', NULL, '首页', '/', 'HomePage', 'HomeFilled', NULL, 1, 1, 1),
-- API市场（所有用户可见）
('2', NULL, 'API市场', '/api-market', 'ApiMarketPage', 'Shop', 'market:view', 1, 1, 2),
-- 控制台（管理员可见）
('3', NULL, '控制台', '/console', 'ConsolePage', 'Monitor', 'api:list', 1, 1, 3),
('31', '3', 'API管理', '/console/api/list', 'ApiListPage', 'Document', 'api:list', 1, 1, 1),
('32', '3', '应用管理', '/console/app/list', 'AppListPage', 'Grid', 'app:list', 1, 1, 2),
('33', '3', '租户管理', '/console/tenant/list', 'TenantListPage', 'OfficeBuilding', 'tenant:list', 1, 1, 3),
('34', '3', '用户管理', '/console/users/list', 'UserListPage', 'User', 'user:list', 1, 1, 4),
-- 监控中心（管理员可见）
('4', NULL, '监控中心', '/monitor', 'MonitorPage', 'DataLine', 'monitor:view', 1, 1, 4),
-- 系统设置（仅超级管理员可见）
('6', NULL, '系统设置', '/system', 'SystemPage', 'Setting', 'system:config', 1, 1, 6),
('61', '6', '系统配置', '/system/config', 'SystemConfigPage', 'Tools', 'system:config', 1, 1, 1),
('62', '6', '公告管理', '/system/announcement', 'AnnouncementPage', 'Bell', 'system:announcement', 1, 1, 2),
('63', '6', '日志管理', '/system/log', 'LogPage', 'Document', 'system:log', 1, 1, 3),
-- 开发文档（所有用户可见）
('5', NULL, '开发文档', '/docs', 'DocsPage', 'Reading', NULL, 1, 1, 5);

-- 6. 创建基础权限
INSERT INTO `iam_permission` (`id`, `code`, `name`, `group_name`, `description`, `sort`) VALUES
-- API管理权限
('1', 'api:list', 'API列表', 'API管理', '查看API列表', 1),
('2', 'api:create', '创建API', 'API管理', '创建新的API', 2),
('3', 'api:update', '更新API', 'API管理', '更新API信息', 3),
('4', 'api:delete', '删除API', 'API管理', '删除API', 4),
('5', 'api:publish', '发布API', 'API管理', '发布API', 5),
-- 应用管理权限
('10', 'app:list', '应用列表', '应用管理', '查看应用列表', 10),
('11', 'app:create', '创建应用', '应用管理', '创建新的应用', 11),
('12', 'app:update', '更新应用', '应用管理', '更新应用信息', 12),
('13', 'app:delete', '删除应用', '应用管理', '删除应用', 13),
-- 用户管理权限
('20', 'user:list', '用户列表', '用户管理', '查看用户列表', 20),
('21', 'user:create', '创建用户', '用户管理', '创建新的用户', 21),
('22', 'user:update', '更新用户', '用户管理', '更新用户信息', 22),
('23', 'user:delete', '删除用户', '用户管理', '删除用户', 23),
-- 租户管理权限（仅超级管理员）
('30', 'tenant:list', '租户列表', '租户管理', '查看租户列表', 30),
('31', 'tenant:create', '创建租户', '租户管理', '创建新的租户', 31),
('32', 'tenant:update', '更新租户', '租户管理', '更新租户信息', 32),
('33', 'tenant:delete', '删除租户', '租户管理', '删除租户', 33),
-- 系统管理权限（仅超级管理员）
('40', 'system:config', '系统配置', '系统管理', '系统配置管理', 40),
('41', 'system:announcement', '公告管理', '系统管理', '系统公告管理', 41),
('42', 'system:log', '日志管理', '系统管理', '系统日志查看', 42),
-- 监控权限
('50', 'monitor:view', '监控查看', '监控中心', '查看监控数据', 50),
('51', 'monitor:alert', '告警管理', '监控中心', '管理告警规则', 51),
-- API市场权限（所有用户）
('60', 'market:view', 'API市场浏览', 'API市场', '浏览API市场', 60),
('61', 'market:subscribe', 'API订阅', 'API市场', '订阅API', 61);

-- 7. 角色权限关联

-- 超级管理员（platform_admin）- 拥有所有权限
INSERT INTO `iam_role_permission` (`role_id`, `permission_id`) VALUES
-- API管理
('1', '1'), ('1', '2'), ('1', '3'), ('1', '4'), ('1', '5'),
-- 应用管理
('1', '10'), ('1', '11'), ('1', '12'), ('1', '13'),
-- 用户管理
('1', '20'), ('1', '21'), ('1', '22'), ('1', '23'),
-- 租户管理
('1', '30'), ('1', '31'), ('1', '32'), ('1', '33'),
-- 系统管理
('1', '40'), ('1', '41'), ('1', '42'),
-- 监控
('1', '50'), ('1', '51'),
-- API市场
('1', '60'), ('1', '61');

-- 租户管理员（tenant_admin）- 本租户管理权限
INSERT INTO `iam_role_permission` (`role_id`, `permission_id`) VALUES
-- API管理
('2', '1'), ('2', '2'), ('2', '3'), ('2', '4'), ('2', '5'),
-- 应用管理
('2', '10'), ('2', '11'), ('2', '12'), ('2', '13'),
-- 用户管理（本租户）
('2', '20'), ('2', '21'), ('2', '22'), ('2', '23'),
-- 监控
('2', '50'), ('2', '51'),
-- API市场
('2', '60'), ('2', '61');

-- 普通用户（user）- 基础使用权限
INSERT INTO `iam_role_permission` (`role_id`, `permission_id`) VALUES
-- API列表（只读）
('3', '1'),
-- 应用列表（只读）
('3', '10'),
-- API市场
('3', '60'), ('3', '61');
