-- =====================================================
-- IntelliHub AIGC服务数据库初始化脚本
-- =====================================================

CREATE DATABASE IF NOT EXISTS `intelli_hub_aigc` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `intelli_hub_aigc`;

-- =====================================================
-- 1. AIGC模型配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `aigc_model_config` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
  `model_name` varchar(100) NOT NULL COMMENT '模型名称',
  `model_type` varchar(20) NOT NULL COMMENT '模型类型(text/chat/image/embedding)',
  `provider` varchar(50) NOT NULL COMMENT '提供商(aliyun_qwen/baidu_ernie/tencent_hunyuan)',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'API密钥（加密存储）',
  `api_endpoint` varchar(255) DEFAULT NULL COMMENT 'API端点',
  `max_tokens` int DEFAULT 2000 COMMENT '最大Token数',
  `temperature` float DEFAULT 0.7 COMMENT '默认温度参数',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态(active/disabled)',
  `priority` int DEFAULT 0 COMMENT '优先级',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除(0-未删除，1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_model_name` (`model_name`),
  KEY `idx_provider` (`provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AIGC模型配置表';

-- =====================================================
-- 2. AIGC请求日志表
-- =====================================================
CREATE TABLE IF NOT EXISTS `aigc_request_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
  `app_id` varchar(32) DEFAULT NULL COMMENT '应用ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `model_name` varchar(100) NOT NULL COMMENT '使用的模型',
  `provider` varchar(50) NOT NULL COMMENT '提供商',
  `prompt` text COMMENT '输入提示词',
  `response` text COMMENT '响应内容',
  `tokens_used` int DEFAULT 0 COMMENT '消耗Token数',
  `cost` decimal(10,4) DEFAULT 0.0000 COMMENT '成本（元）',
  `duration` bigint DEFAULT 0 COMMENT '耗时（毫秒）',
  `status` varchar(20) NOT NULL COMMENT '状态(success/failed)',
  `error_message` text COMMENT '错误信息',
  `request_id` varchar(100) DEFAULT NULL COMMENT '请求ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_model_name` (`model_name`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AIGC请求日志表';

-- =====================================================
-- 3. AIGC配额配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `aigc_quota_config` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) NOT NULL DEFAULT 'default' COMMENT '租户ID',
  `daily_quota` bigint DEFAULT 100000 COMMENT '每日配额（Token数）',
  `used_quota` bigint DEFAULT 0 COMMENT '已用配额',
  `total_cost` decimal(10,4) DEFAULT 0.0000 COMMENT '总成本（元）',
  `reset_time` datetime DEFAULT NULL COMMENT '配额重置时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AIGC配额配置表';

-- =====================================================
-- 4. AIGC对话历史表
-- =====================================================
CREATE TABLE IF NOT EXISTS `aigc_conversation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
  `conversation_id` varchar(100) NOT NULL COMMENT '会话ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `role` varchar(20) NOT NULL COMMENT '角色(user/assistant/system)',
  `content` text NOT NULL COMMENT '消息内容',
  `tokens` int DEFAULT 0 COMMENT 'Token数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AIGC对话历史表';

-- =====================================================
-- 5. AIGC Prompt模板表
-- =====================================================
CREATE TABLE IF NOT EXISTS `aigc_prompt_template` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `code` varchar(50) NOT NULL COMMENT '模板代码（唯一标识）',
  `content` text NOT NULL COMMENT '模板内容（支持{变量}占位符）',
  `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
  `type` varchar(20) DEFAULT 'text' COMMENT '模板类型（text/chat/custom）',
  `variables` text COMMENT '变量定义（JSON格式）',
  `is_public` tinyint DEFAULT 0 COMMENT '是否公共模板（0否1是）',
  `use_count` bigint DEFAULT 0 COMMENT '使用次数',
  `status` tinyint DEFAULT 1 COMMENT '状态（0禁用1启用）',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除（0未删除1已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AIGC Prompt模板表';

-- =====================================================
-- 插入初始数据
-- =====================================================

-- 插入默认模型配置（示例）
INSERT INTO `aigc_model_config` (`id`, `tenant_id`, `model_name`, `model_type`, `provider`, `status`, `priority`) VALUES
('1', 'default', 'qwen-turbo', 'chat', 'aliyun_qwen', 'active', 1),
('2', 'default', 'qwen-plus', 'chat', 'aliyun_qwen', 'active', 2),
('3', 'default', 'ernie-bot-turbo', 'chat', 'baidu_ernie', 'active', 1),
('4', 'default', 'hunyuan-lite', 'chat', 'tencent_hunyuan', 'active', 1);

-- 插入默认租户配额
INSERT INTO `aigc_quota_config` (`id`, `tenant_id`, `daily_quota`, `used_quota`, `reset_time`) VALUES
('1', 'default', 100000, 0, DATE_ADD(CURDATE(), INTERVAL 1 DAY));
