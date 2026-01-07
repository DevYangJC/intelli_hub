-- ===============================================
-- IntelliHub 事件中心数据库表结构
-- Event Center Database Schema
-- ===============================================

-- -------------------- 创建数据库 --------------------
CREATE DATABASE IF NOT EXISTS `intelli_hub_event` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `intelli_hub_event`;

-- -------------------- 事件定义表 --------------------
-- 用于定义系统中所有可发布的事件类型
CREATE TABLE IF NOT EXISTS `event_definition` (
    `id` VARCHAR(32) NOT NULL COMMENT '事件ID',
    `tenant_id` VARCHAR(32) NOT NULL COMMENT '租户ID',
    `event_code` VARCHAR(100) NOT NULL COMMENT '事件编码',
    `event_name` VARCHAR(200) NOT NULL COMMENT '事件名称',
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型(SYSTEM/BUSINESS/CUSTOM)',
    `description` TEXT COMMENT '事件描述',
    `schema_definition` TEXT COMMENT '事件数据结构定义(JSON Schema)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/INACTIVE)',
    `created_by` VARCHAR(32) COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `event_code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_event_code` (`event_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件定义表';

-- -------------------- 事件订阅表 --------------------
-- 记录哪些订阅者关注哪些事件，以及如何通知他们
CREATE TABLE IF NOT EXISTS `event_subscription` (
    `id` VARCHAR(32) NOT NULL COMMENT '订阅ID',
    `tenant_id` VARCHAR(32) NOT NULL COMMENT '租户ID',
    `event_code` VARCHAR(100) NOT NULL COMMENT '事件编码',
    `subscriber_type` VARCHAR(50) NOT NULL COMMENT '订阅者类型(SERVICE/WEBHOOK/MQ)',
    `subscriber_name` VARCHAR(200) NOT NULL COMMENT '订阅者名称',
    `callback_url` VARCHAR(500) COMMENT '回调地址(Webhook)',
    `callback_method` VARCHAR(10) COMMENT '回调方法(POST/PUT)',
    `callback_headers` TEXT COMMENT '回调请求头(JSON)',
    `mq_topic` VARCHAR(200) COMMENT 'MQ主题',
    `mq_tag` VARCHAR(100) COMMENT 'MQ标签',
    `filter_expression` TEXT COMMENT '过滤表达式(SpEL)',
    `retry_strategy` VARCHAR(50) DEFAULT 'EXPONENTIAL' COMMENT '重试策略(NONE/FIXED/EXPONENTIAL)',
    `max_retry_times` INT DEFAULT 3 COMMENT '最大重试次数',
    `timeout_seconds` INT DEFAULT 30 COMMENT '超时时间(秒)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/PAUSED/INACTIVE)',
    `priority` INT DEFAULT 0 COMMENT '优先级(数字越大优先级越高)',
    `created_by` VARCHAR(32) COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_event` (`tenant_id`, `event_code`),
    KEY `idx_status` (`status`),
    KEY `idx_subscriber_type` (`subscriber_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件订阅表';

-- -------------------- 事件发布记录表 --------------------
-- 记录每次事件发布的详情，用于追溯和审计
CREATE TABLE IF NOT EXISTS `event_publish_record` (
    `id` VARCHAR(32) NOT NULL COMMENT '记录ID',
    `tenant_id` VARCHAR(32) NOT NULL COMMENT '租户ID',
    `event_code` VARCHAR(100) NOT NULL COMMENT '事件编码',
    `event_id` VARCHAR(64) NOT NULL COMMENT '事件唯一ID',
    `event_data` TEXT NOT NULL COMMENT '事件数据(JSON)',
    `source` VARCHAR(200) COMMENT '事件源',
    `publish_time` DATETIME NOT NULL COMMENT '发布时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态(PUBLISHED/FAILED)',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`),
    KEY `idx_tenant_event` (`tenant_id`, `event_code`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件发布记录表';

-- -------------------- 事件消费记录表 --------------------
-- 记录每个订阅者的消费情况，支持重试机制
CREATE TABLE IF NOT EXISTS `event_consume_record` (
    `id` VARCHAR(32) NOT NULL COMMENT '记录ID',
    `tenant_id` VARCHAR(32) NOT NULL COMMENT '租户ID',
    `subscription_id` VARCHAR(32) NOT NULL COMMENT '订阅ID',
    `event_id` VARCHAR(64) NOT NULL COMMENT '事件唯一ID',
    `event_code` VARCHAR(100) NOT NULL COMMENT '事件编码',
    `event_data` TEXT NOT NULL COMMENT '事件数据(JSON)',
    `consume_time` DATETIME NOT NULL COMMENT '消费时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/SUCCESS/FAILED/RETRYING)',
    `retry_times` INT DEFAULT 0 COMMENT '重试次数',
    `next_retry_time` DATETIME COMMENT '下次重试时间',
    `response_code` INT COMMENT '响应状态码',
    `response_body` TEXT COMMENT '响应内容',
    `error_message` TEXT COMMENT '错误信息',
    `cost_time` INT COMMENT '耗时(毫秒)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_subscription` (`subscription_id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_status` (`status`),
    KEY `idx_next_retry` (`next_retry_time`),
    KEY `idx_tenant_event` (`tenant_id`, `event_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件消费记录表';

-- -------------------- 事件统计表 --------------------
-- 按日统计事件的发布和消费情况，用于监控和报表
CREATE TABLE IF NOT EXISTS `event_statistics` (
    `id` VARCHAR(32) NOT NULL COMMENT '统计ID',
    `tenant_id` VARCHAR(32) NOT NULL COMMENT '租户ID',
    `event_code` VARCHAR(100) NOT NULL COMMENT '事件编码',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `publish_count` BIGINT DEFAULT 0 COMMENT '发布次数',
    `consume_count` BIGINT DEFAULT 0 COMMENT '消费次数',
    `success_count` BIGINT DEFAULT 0 COMMENT '成功次数',
    `failed_count` BIGINT DEFAULT 0 COMMENT '失败次数',
    `avg_cost_time` INT DEFAULT 0 COMMENT '平均耗时(毫秒)',
    `max_cost_time` INT DEFAULT 0 COMMENT '最大耗时(毫秒)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_event_date` (`tenant_id`, `event_code`, `stat_date`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件统计表';
