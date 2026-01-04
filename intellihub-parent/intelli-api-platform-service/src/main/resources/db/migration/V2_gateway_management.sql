-- =====================================================
-- 网关管理功能数据库迁移脚本
-- 版本: V2
-- 说明: 新增限流策略表和路由限流关联表
-- =====================================================

USE `intelli_hub_api_platform`;

-- =====================================================
-- 1. 限流策略表
-- =====================================================
CREATE TABLE IF NOT EXISTS `gateway_ratelimit_policy` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
  `name` varchar(100) NOT NULL COMMENT '策略名称',
  `description` varchar(500) DEFAULT NULL COMMENT '策略描述',
  `type` varchar(20) NOT NULL COMMENT '限流类型: qps/concurrency',
  `dimension` varchar(20) NOT NULL COMMENT '限流维度: global/ip/path/ip_path/user',
  `limit_value` int(11) NOT NULL COMMENT '限流阈值',
  `time_window` int(11) NOT NULL DEFAULT 1 COMMENT '时间窗口(秒)',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/inactive',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网关限流策略表';

-- =====================================================
-- 2. 路由限流策略关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS `gateway_route_ratelimit` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `route_id` varchar(32) NOT NULL COMMENT '路由ID(api_id)',
  `policy_id` varchar(32) NOT NULL COMMENT '策略ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_policy` (`route_id`, `policy_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_policy_id` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路由限流策略关联表';

-- =====================================================
-- 3. 初始化默认限流策略
-- =====================================================
INSERT INTO `gateway_ratelimit_policy` 
  (`id`, `tenant_id`, `name`, `description`, `type`, `dimension`, `limit_value`, `time_window`, `status`, `created_by`) 
VALUES
  ('RL_DEFAULT_001', '1', '默认全局限流', '全局默认限流策略，适用于所有API', 'qps', 'global', 1000, 1, 'active', '1'),
  ('RL_DEFAULT_002', '1', 'IP限流策略', '基于IP的限流策略，防止单个IP过度调用', 'qps', 'ip', 100, 1, 'active', '1'),
  ('RL_DEFAULT_003', '1', '高频API限流', '针对高频调用API的限流策略', 'qps', 'ip_path', 50, 1, 'active', '1');
