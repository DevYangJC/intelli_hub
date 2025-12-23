-- =============================================
-- IntelliHub Governance 数据库表结构
-- 数据库: intelli_hub_governance
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS intelli_hub_governance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE intelli_hub_governance;

-- =============================================
-- 1. API调用日志表
-- =============================================
DROP TABLE IF EXISTS api_call_log;
CREATE TABLE api_call_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    api_id VARCHAR(32) COMMENT 'API ID',
    api_path VARCHAR(255) NOT NULL COMMENT 'API路径',
    api_method VARCHAR(10) NOT NULL COMMENT '请求方法',
    app_id VARCHAR(32) COMMENT '应用ID',
    app_key VARCHAR(64) COMMENT 'AppKey',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    status_code INT COMMENT '响应状态码',
    success TINYINT(1) DEFAULT 1 COMMENT '是否成功(1成功0失败)',
    latency INT COMMENT '响应时间(ms)',
    request_time DATETIME NOT NULL COMMENT '请求时间',
    error_message VARCHAR(500) COMMENT '错误信息',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    request_body TEXT COMMENT '请求体(可选)',
    response_body TEXT COMMENT '响应体(可选)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_api (tenant_id, api_id),
    INDEX idx_request_time (request_time),
    INDEX idx_app_id (app_id),
    INDEX idx_api_path (api_path(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';

-- =============================================
-- 2. API调用统计表(小时维度)
-- =============================================
DROP TABLE IF EXISTS api_call_stats_hourly;
CREATE TABLE api_call_stats_hourly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    api_id VARCHAR(32) COMMENT 'API ID',
    api_path VARCHAR(255) NOT NULL COMMENT 'API路径',
    app_id VARCHAR(32) COMMENT '应用ID',
    stat_time DATETIME NOT NULL COMMENT '统计时间(小时整点)',
    total_count BIGINT DEFAULT 0 COMMENT '总调用次数',
    success_count BIGINT DEFAULT 0 COMMENT '成功次数',
    fail_count BIGINT DEFAULT 0 COMMENT '失败次数',
    avg_latency INT DEFAULT 0 COMMENT '平均响应时间(ms)',
    max_latency INT DEFAULT 0 COMMENT '最大响应时间(ms)',
    min_latency INT DEFAULT 0 COMMENT '最小响应时间(ms)',
    p95_latency INT DEFAULT 0 COMMENT 'P95响应时间(ms)',
    p99_latency INT DEFAULT 0 COMMENT 'P99响应时间(ms)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat (tenant_id, api_path, app_id, stat_time),
    INDEX idx_stat_time (stat_time),
    INDEX idx_api_id (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用统计表(小时维度)';

-- =============================================
-- 3. API调用统计表(天维度)
-- =============================================
DROP TABLE IF EXISTS api_call_stats_daily;
CREATE TABLE api_call_stats_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    api_id VARCHAR(32) COMMENT 'API ID',
    api_path VARCHAR(255) NOT NULL COMMENT 'API路径',
    app_id VARCHAR(32) COMMENT '应用ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_count BIGINT DEFAULT 0 COMMENT '总调用次数',
    success_count BIGINT DEFAULT 0 COMMENT '成功次数',
    fail_count BIGINT DEFAULT 0 COMMENT '失败次数',
    avg_latency INT DEFAULT 0 COMMENT '平均响应时间(ms)',
    max_latency INT DEFAULT 0 COMMENT '最大响应时间(ms)',
    min_latency INT DEFAULT 0 COMMENT '最小响应时间(ms)',
    p95_latency INT DEFAULT 0 COMMENT 'P95响应时间(ms)',
    p99_latency INT DEFAULT 0 COMMENT 'P99响应时间(ms)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat (tenant_id, api_path, app_id, stat_date),
    INDEX idx_stat_date (stat_date),
    INDEX idx_api_id (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用统计表(天维度)';

-- =============================================
-- 4. 告警规则表(预留)
-- =============================================
DROP TABLE IF EXISTS alert_rule;
CREATE TABLE alert_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型(error_rate/latency/qps)',
    api_id VARCHAR(32) COMMENT 'API ID(为空表示全局)',
    threshold DECIMAL(10,2) NOT NULL COMMENT '阈值',
    operator VARCHAR(10) NOT NULL COMMENT '比较运算符(gt/lt/eq/gte/lte)',
    duration INT DEFAULT 60 COMMENT '持续时间(秒)',
    notify_channels VARCHAR(255) COMMENT '通知渠道(email/sms/webhook)',
    notify_targets VARCHAR(500) COMMENT '通知目标',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态(active/disabled)',
    created_by VARCHAR(32) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant (tenant_id),
    INDEX idx_api (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- =============================================
-- 5. 告警记录表(预留)
-- =============================================
DROP TABLE IF EXISTS alert_record;
CREATE TABLE alert_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id VARCHAR(32) NOT NULL COMMENT '租户ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_name VARCHAR(100) COMMENT '规则名称',
    api_id VARCHAR(32) COMMENT 'API ID',
    api_path VARCHAR(255) COMMENT 'API路径',
    alert_level VARCHAR(20) DEFAULT 'warning' COMMENT '告警级别(info/warning/critical)',
    alert_message VARCHAR(500) COMMENT '告警内容',
    current_value DECIMAL(10,2) COMMENT '当前值',
    threshold_value DECIMAL(10,2) COMMENT '阈值',
    status VARCHAR(20) DEFAULT 'firing' COMMENT '状态(firing/resolved)',
    fired_at DATETIME NOT NULL COMMENT '触发时间',
    resolved_at DATETIME COMMENT '恢复时间',
    notified TINYINT(1) DEFAULT 0 COMMENT '是否已通知',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant (tenant_id),
    INDEX idx_rule (rule_id),
    INDEX idx_fired_at (fired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';
