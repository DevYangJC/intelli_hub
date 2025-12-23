-- ========================================
-- 应用中心数据库初始化脚本
-- ========================================


CREATE DATABASE IF NOT EXISTS `intelli_hub_app_center` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 应用信息表
CREATE TABLE IF NOT EXISTS `app_info` (
    `id` VARCHAR(32) NOT NULL COMMENT '应用ID',
    `tenant_id` VARCHAR(32) NOT NULL COMMENT '租户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '应用名称',
    `code` VARCHAR(50) NOT NULL COMMENT '应用编码',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '应用描述',
    `app_type` VARCHAR(20) DEFAULT 'external' COMMENT '应用类型：internal-内部应用，external-外部应用',
    `app_key` VARCHAR(64) NOT NULL COMMENT 'AppKey',
    `app_secret` VARCHAR(128) NOT NULL COMMENT 'AppSecret',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-正常，disabled-禁用，expired-过期',
    `quota_limit` BIGINT DEFAULT 10000 COMMENT '每日调用配额限制',
    `quota_used` BIGINT DEFAULT 0 COMMENT '已使用配额',
    `quota_reset_time` DATETIME DEFAULT NULL COMMENT '配额重置时间',
    `callback_url` VARCHAR(500) DEFAULT NULL COMMENT '回调地址',
    `ip_whitelist` VARCHAR(1000) DEFAULT NULL COMMENT 'IP白名单，多个用逗号分隔',
    `expire_time` DATETIME DEFAULT NULL COMMENT 'AppKey过期时间',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
    `created_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人ID',
    `created_by_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_key` (`app_key`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`, `deleted`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用信息表';

-- 应用API订阅关系表
CREATE TABLE IF NOT EXISTS `app_api_subscription` (
    `id` VARCHAR(32) NOT NULL COMMENT '订阅ID',
    `app_id` VARCHAR(32) NOT NULL COMMENT '应用ID',
    `api_id` VARCHAR(32) NOT NULL COMMENT 'API ID',
    `api_name` VARCHAR(100) DEFAULT NULL COMMENT 'API名称',
    `api_path` VARCHAR(200) DEFAULT NULL COMMENT 'API路径',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-生效，disabled-禁用，expired-过期',
    `quota_limit` BIGINT DEFAULT NULL COMMENT '单独调用限额',
    `effective_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_api` (`app_id`, `api_id`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用API订阅关系表';

-- ========================================
-- 初始化测试数据
-- ========================================

-- 插入测试应用数据
INSERT INTO `app_info` (`id`, `tenant_id`, `name`, `code`, `description`, `app_type`, `app_key`, `app_secret`, `status`, `quota_limit`, `quota_used`, `contact_name`, `contact_email`, `created_by`, `created_by_name`, `created_at`, `updated_at`, `deleted`) VALUES
('app001', 'tenant001', '电商平台应用', 'ecommerce-app', '电商平台对接应用，用于商品查询和订单管理', 'external', 'AK1234567890ABCDEFGHIJKLMNOPQR', 'AS1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz12', 'active', 50000, 1234, '张三', 'zhangsan@example.com', 'user001', '管理员', NOW(), NOW(), 0),
('app002', 'tenant001', '移动端APP', 'mobile-app', '移动端APP接入', 'internal', 'AK0987654321ZYXWVUTSRQPONMLKJI', 'AS0987654321ZYXWVUTSRQPONMLKJIHGFEDCBAzyxwvutsrqponmlkjihgfedcba98', 'active', 100000, 5678, '李四', 'lisi@example.com', 'user001', '管理员', NOW(), NOW(), 0),
('app003', 'tenant001', '数据分析平台', 'analytics-app', '数据分析和报表平台', 'external', 'AKANALYTICS123456789ABCDEFGHIJ', 'ASANALYTICS123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrs', 'active', 20000, 890, '王五', 'wangwu@example.com', 'user001', '管理员', NOW(), NOW(), 0),
('app004', 'tenant001', '第三方支付', 'payment-app', '第三方支付系统接入', 'external', 'AKPAYMENT1234567890ABCDEFGHIJK', 'ASPAYMENT1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstu', 'disabled', 30000, 0, '赵六', 'zhaoliu@example.com', 'user001', '管理员', NOW(), NOW(), 0);

-- 插入测试订阅数据
INSERT INTO `app_api_subscription` (`id`, `app_id`, `api_id`, `api_name`, `api_path`, `status`, `effective_time`, `created_at`, `updated_at`) VALUES
('sub001', 'app001', 'api001', '用户信息查询', '/api/v1/users/{id}', 'active', NOW(), NOW(), NOW()),
('sub002', 'app001', 'api002', '商品列表查询', '/api/v1/products', 'active', NOW(), NOW(), NOW()),
('sub003', 'app002', 'api001', '用户信息查询', '/api/v1/users/{id}', 'active', NOW(), NOW(), NOW()),
('sub004', 'app002', 'api003', '订单创建', '/api/v1/orders', 'active', NOW(), NOW(), NOW()),
('sub005', 'app003', 'api004', '数据统计接口', '/api/v1/stats', 'active', NOW(), NOW(), NOW());
