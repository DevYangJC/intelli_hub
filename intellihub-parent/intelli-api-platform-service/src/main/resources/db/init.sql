-- =====================================================
-- IntelliHub API Platform 数据库初始化脚本
-- 数据库: intelli_hub_api_platform
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `intelli_hub_api_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `intelli_hub_api_platform`;

-- =====================================================
-- 1. API分组表
-- =====================================================
DROP TABLE IF EXISTS `api_group`;
CREATE TABLE `api_group` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
    `name` varchar(64) NOT NULL COMMENT '分组名称',
    `code` varchar(64) NOT NULL COMMENT '分组编码',
    `description` varchar(256) DEFAULT NULL COMMENT '描述',
    `sort` int(11) DEFAULT 0 COMMENT '排序',
    `color` varchar(20) DEFAULT '#409EFF' COMMENT '分组颜色',
    `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
    `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API分组表';

-- =====================================================
-- 2. API信息表
-- =====================================================
DROP TABLE IF EXISTS `api_info`;
CREATE TABLE `api_info` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `tenant_id` varchar(32) NOT NULL COMMENT '租户ID',
    `group_id` varchar(32) DEFAULT NULL COMMENT '分组ID',
    `name` varchar(128) NOT NULL COMMENT 'API名称',
    `code` varchar(64) NOT NULL COMMENT 'API编码',
    `version` varchar(20) DEFAULT 'v1' COMMENT '版本号',
    `description` varchar(500) DEFAULT NULL COMMENT 'API描述',
    `method` varchar(10) NOT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
    `path` varchar(256) NOT NULL COMMENT '请求路径',
    `protocol` varchar(20) DEFAULT 'HTTP' COMMENT '协议：HTTP/HTTPS',
    `content_type` varchar(64) DEFAULT 'application/json' COMMENT '内容类型',
    `status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft/published/offline/deprecated',
    `auth_type` varchar(20) DEFAULT 'token' COMMENT '认证方式：none/token/signature',
    `timeout` int(11) DEFAULT 30000 COMMENT '超时时间(ms)',
    `retry_count` int(11) DEFAULT 0 COMMENT '重试次数',
    `cache_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用缓存',
    `cache_ttl` int(11) DEFAULT 0 COMMENT '缓存时间(秒)',
    `rate_limit_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用限流',
    `rate_limit_qps` int(11) DEFAULT 100 COMMENT '限流QPS',
    `mock_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用Mock',
    `mock_response` text DEFAULT NULL COMMENT 'Mock响应数据',
    `success_response` text DEFAULT NULL COMMENT '成功响应示例',
    `error_response` text DEFAULT NULL COMMENT '错误响应示例',
    `ip_whitelist_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用IP白名单',
    `ip_whitelist` text DEFAULT NULL COMMENT 'IP白名单列表',
    `today_calls` bigint(20) DEFAULT 0 COMMENT '今日调用次数',
    `total_calls` bigint(20) DEFAULT 0 COMMENT '总调用次数',
    `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
    `creator_name` varchar(64) DEFAULT NULL COMMENT '创建人名称',
    `published_at` datetime DEFAULT NULL COMMENT '发布时间',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_id`, `code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_status` (`status`),
    KEY `idx_method` (`method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API信息表';

-- =====================================================
-- 3. API请求参数表
-- =====================================================
DROP TABLE IF EXISTS `api_request_param`;
CREATE TABLE `api_request_param` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `api_id` varchar(32) NOT NULL COMMENT 'API ID',
    `name` varchar(64) NOT NULL COMMENT '参数名称',
    `type` varchar(20) NOT NULL COMMENT '参数类型：string/integer/boolean/array/object',
    `location` varchar(20) NOT NULL COMMENT '参数位置：query/header/path/body',
    `required` tinyint(1) DEFAULT 0 COMMENT '是否必填',
    `default_value` varchar(256) DEFAULT NULL COMMENT '默认值',
    `example` varchar(256) DEFAULT NULL COMMENT '示例值',
    `description` varchar(256) DEFAULT NULL COMMENT '参数描述',
    `sort` int(11) DEFAULT 0 COMMENT '排序',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API请求参数表';

-- =====================================================
-- 4. API响应参数表
-- =====================================================
DROP TABLE IF EXISTS `api_response_param`;
CREATE TABLE `api_response_param` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `api_id` varchar(32) NOT NULL COMMENT 'API ID',
    `name` varchar(64) NOT NULL COMMENT '参数名称',
    `type` varchar(20) NOT NULL COMMENT '参数类型：string/integer/boolean/array/object',
    `description` varchar(256) DEFAULT NULL COMMENT '参数描述',
    `example` varchar(512) DEFAULT NULL COMMENT '示例值',
    `sort` int(11) DEFAULT 0 COMMENT '排序',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API响应参数表';

-- =====================================================
-- 5. API后端配置表
-- =====================================================
DROP TABLE IF EXISTS `api_backend`;
CREATE TABLE `api_backend` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `api_id` varchar(32) NOT NULL COMMENT 'API ID',
    `type` varchar(20) NOT NULL DEFAULT 'http' COMMENT '后端类型：http/mock/dubbo',
    `protocol` varchar(20) DEFAULT 'HTTP' COMMENT '协议：HTTP/HTTPS',
    `method` varchar(10) DEFAULT NULL COMMENT '请求方法',
    `host` varchar(256) DEFAULT NULL COMMENT '后端地址',
    `path` varchar(256) DEFAULT NULL COMMENT '后端路径',
    `timeout` int(11) DEFAULT 30000 COMMENT '超时时间(ms)',
    `connect_timeout` int(11) DEFAULT 5000 COMMENT '连接超时(ms)',
    `registry` varchar(255) DEFAULT NULL COMMENT 'Dubbo注册中心地址',
    `interface_name` varchar(255) DEFAULT NULL COMMENT 'Dubbo接口名称',
    `method_name` varchar(100) DEFAULT NULL COMMENT 'Dubbo方法名称',
    `dubbo_version` varchar(50) DEFAULT NULL COMMENT 'Dubbo服务版本',
    `dubbo_group` varchar(100) DEFAULT NULL COMMENT 'Dubbo服务分组',
    `ref_api_id` varchar(32) DEFAULT NULL COMMENT '引用的内部API ID',
    `mock_delay` int(11) DEFAULT 0 COMMENT 'Mock延迟时间(ms)',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API后端配置表';

-- =====================================================
-- 6. API版本历史表
-- =====================================================
DROP TABLE IF EXISTS `api_version`;
CREATE TABLE `api_version` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `api_id` varchar(32) NOT NULL COMMENT 'API ID',
    `version` varchar(20) NOT NULL COMMENT '版本号',
    `snapshot` text NOT NULL COMMENT 'API快照(JSON)',
    `change_log` varchar(500) DEFAULT NULL COMMENT '变更说明',
    `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_api_id` (`api_id`),
    KEY `idx_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API版本历史表';

-- =====================================================
-- 7. API标签表
-- =====================================================
DROP TABLE IF EXISTS `api_tag`;
CREATE TABLE `api_tag` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `api_id` varchar(32) NOT NULL COMMENT 'API ID',
    `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_api_id` (`api_id`),
    KEY `idx_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API标签表';


-- 公告表
CREATE TABLE IF NOT EXISTS sys_announcement (
                                                id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
                                                tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',
                                                title VARCHAR(200) NOT NULL COMMENT '标题',
                                                description TEXT COMMENT '内容描述',
                                                type VARCHAR(20) NOT NULL DEFAULT 'notice' COMMENT '类型: notice-系统通知, feature-功能更新, maintenance-维护公告, warning-重要提醒',
                                                meta VARCHAR(50) COMMENT '标签',
                                                status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: published-已发布, draft-草稿',
                                                publish_time DATETIME COMMENT '发布时间',
                                                created_by VARCHAR(36) COMMENT '创建人',
                                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                updated_by VARCHAR(36) COMMENT '更新人',
                                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                deleted TINYINT DEFAULT 0 COMMENT '是否删除',
                                                INDEX idx_tenant_status (tenant_id, status),
                                                INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
                                          id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
                                          tenant_id VARCHAR(36) NOT NULL COMMENT '租户ID',
                                          config_key VARCHAR(100) NOT NULL COMMENT '配置键',
                                          config_value TEXT COMMENT '配置值',
                                          config_type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型: string, number, boolean, json',
                                          description VARCHAR(200) COMMENT '配置描述',
                                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          UNIQUE KEY uk_tenant_key (tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =====================================================
-- 系统公告初始化数据（近三个月开发进度）
-- =====================================================
INSERT INTO sys_announcement (id, tenant_id, title, description, type, meta, status, publish_time, created_by, deleted) VALUES
-- 2024年10月 - 项目启动阶段
('ANN001', '1', 'IntelliHub 平台正式启动', '企业级智能API管理平台 IntelliHub 项目正式启动开发，目标打造统一的API全生命周期管理解决方案。', 'notice', '项目启动', 'published', '2024-10-08 09:00:00', '1', 0),
('ANN002', '1', '基础架构搭建完成', '完成 Spring Cloud 微服务架构搭建，包括网关服务、认证服务、API平台服务等核心模块。', 'feature', '技术架构', 'published', '2024-10-15 14:30:00', '1', 0),
('ANN003', '1', '用户认证模块上线', '用户认证与权限管理模块已完成开发，支持 JWT Token 认证、角色权限控制、多租户隔离。', 'feature', '功能更新', 'published', '2024-10-22 10:00:00', '1', 0),

-- 2024年11月 - 核心功能开发
('ANN004', '1', 'API 网关核心功能上线', '智能 API 网关已上线，支持动态路由、请求限流、熔断降级、访问日志记录等能力。', 'feature', '核心功能', 'published', '2024-11-01 09:30:00', '1', 0),
('ANN005', '1', '系统维护通知', '2024年11月5日 22:00-24:00 进行系统升级维护，届时平台将暂停服务，请提前做好准备。', 'maintenance', '维护通知', 'published', '2024-11-03 15:00:00', '1', 0),
('ANN006', '1', 'API 生命周期管理上线', 'API 全生命周期管理功能已发布，支持 API 创建、版本管理、发布上线、下线废弃等完整流程。', 'feature', '功能更新', 'published', '2024-11-10 11:00:00', '1', 0),
('ANN007', '1', '应用中心模块发布', '应用中心已上线，支持应用创建、AppKey/AppSecret 管理、API 订阅授权等功能。', 'feature', '功能更新', 'published', '2024-11-18 14:00:00', '1', 0),
('ANN008', '1', 'OpenAPI 签名认证上线', '开放 API 支持 HMAC-SHA256 签名认证，保障接口调用安全，防止请求篡改和重放攻击。', 'feature', '安全增强', 'published', '2024-11-25 10:30:00', '1', 0),

-- 2024年12月 - 监控与增强
('ANN009', '1', '调用统计功能上线', 'API 调用统计功能已发布，支持实时调用量、成功率、响应时间等多维度数据分析。', 'feature', '功能更新', 'published', '2024-12-02 09:00:00', '1', 0),
('ANN010', '1', '监控告警系统发布', '智能监控告警系统上线，支持自定义告警规则、多渠道通知（邮件/Webhook）、告警历史记录。', 'feature', '运维能力', 'published', '2024-12-10 11:00:00', '1', 0),
('ANN011', '1', '前端控制台全面升级', '管理控制台 UI 全面升级，优化用户体验，新增 API 市场、开发文档、数据看板等页面。', 'feature', '体验优化', 'published', '2024-12-16 14:30:00', '1', 0),
('ANN012', '1', '年末系统维护公告', '2024年12月24日 23:00-次日02:00 进行年度系统维护和数据备份，期间服务可能短暂中断。', 'maintenance', '维护通知', 'published', '2024-12-20 16:00:00', '1', 0),
('ANN013', '1', 'Java SDK 正式发布', 'IntelliHub Java SDK v1.0.0 正式发布，支持自动签名认证、HTTP 请求封装，方便开发者快速接入平台 API。', 'feature', '开发工具', 'published', '2024-12-26 10:00:00', '1', 0),
('ANN014', '1', '系统设置与公告管理上线', '新增系统设置功能（基础配置、安全设置、API配置、通知设置）和公告管理功能，提升平台可配置性。', 'feature', '功能更新', 'published', '2024-12-26 15:00:00', '1', 0),
('ANN015', '1', '元旦假期服务保障', '2025年元旦假期（1月1日）平台正常运行，如有问题请联系技术支持：support@intellihub.com', 'notice', '节日通知', 'published', '2024-12-28 09:00:00', '1', 0);

-- =====================================================
-- 初始化数据
-- =====================================================

-- 租户和用户ID
SET @tenant_id = '1';
SET @user_id = '1';
SET @user_name = '系统管理员';

-- 1. 创建API分组
INSERT INTO `api_group` (`id`, `tenant_id`, `name`, `code`, `description`, `sort`, `color`, `status`, `created_by`) VALUES
('G001', @tenant_id, '用户服务', 'user-service', '用户认证、注册、信息管理相关接口', 1, '#409EFF', 'active', @user_id),
('G002', @tenant_id, '订单服务', 'order-service', '订单创建、查询、状态管理相关接口', 2, '#67C23A', 'active', @user_id),
('G003', @tenant_id, '商品服务', 'product-service', '商品信息、库存、分类管理相关接口', 3, '#E6A23C', 'active', @user_id),
('G004', @tenant_id, '支付服务', 'payment-service', '支付、退款、账单相关接口', 4, '#F56C6C', 'active', @user_id),
('G005', @tenant_id, '消息服务', 'message-service', '消息推送、通知、短信相关接口', 5, '#909399', 'active', @user_id);

-- 2. 创建API信息
INSERT INTO `api_info` (`id`, `tenant_id`, `group_id`, `name`, `code`, `version`, `description`, `method`, `path`, `protocol`, `content_type`, `status`, `auth_type`, `timeout`, `retry_count`, `cache_enabled`, `cache_ttl`, `rate_limit_enabled`, `rate_limit_qps`, `mock_enabled`, `success_response`, `error_response`, `ip_whitelist_enabled`, `today_calls`, `total_calls`, `created_by`, `creator_name`, `published_at`) VALUES
-- 用户服务API
('API001', @tenant_id, 'G001', '用户登录', 'user-login', 'v1', '用户账号密码登录接口，返回JWT Token', 'POST', '/api/v1/user/login', 'HTTPS', 'application/json', 'published', 'none', 5000, 0, 0, 0, 1, 100, 0, 
'{"code": 200, "message": "登录成功", "data": {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", "expires_in": 7200}}',
'{"code": 401, "message": "用户名或密码错误", "data": null}',
0, 1250, 58620, @user_id, @user_name, NOW()),

('API002', @tenant_id, 'G001', '用户注册', 'user-register', 'v1', '新用户注册接口', 'POST', '/api/v1/user/register', 'HTTPS', 'application/json', 'published', 'none', 5000, 0, 0, 0, 1, 50, 0,
'{"code": 200, "message": "注册成功", "data": {"user_id": "U123456"}}',
'{"code": 400, "message": "手机号已被注册", "data": null}',
0, 320, 15840, @user_id, @user_name, NOW()),

('API003', @tenant_id, 'G001', '获取用户信息', 'user-info', 'v1', '根据用户ID获取用户详细信息', 'GET', '/api/v1/user/{userId}', 'HTTPS', 'application/json', 'published', 'token', 3000, 1, 1, 300, 1, 200, 0,
'{"code": 200, "message": "success", "data": {"user_id": "U123456", "username": "张三", "phone": "138****8888"}}',
'{"code": 404, "message": "用户不存在", "data": null}',
0, 2850, 125600, @user_id, @user_name, NOW()),

-- 订单服务API
('API004', @tenant_id, 'G002', '创建订单', 'order-create', 'v1', '创建新订单接口', 'POST', '/api/v1/order/create', 'HTTPS', 'application/json', 'published', 'token', 10000, 2, 0, 0, 1, 100, 0,
'{"code": 200, "message": "订单创建成功", "data": {"order_id": "ORD202312220001", "total_amount": 299.00}}',
'{"code": 400, "message": "商品库存不足", "data": null}',
0, 890, 42150, @user_id, @user_name, NOW()),

('API005', @tenant_id, 'G002', '查询订单详情', 'order-detail', 'v1', '根据订单ID查询订单详情', 'GET', '/api/v1/order/{orderId}', 'HTTPS', 'application/json', 'published', 'token', 3000, 1, 1, 60, 1, 300, 0,
'{"code": 200, "message": "success", "data": {"order_id": "ORD202312220001", "status": "paid", "items": []}}',
'{"code": 404, "message": "订单不存在", "data": null}',
0, 1560, 78920, @user_id, @user_name, NOW()),

('API006', @tenant_id, 'G002', '订单列表', 'order-list', 'v1', '分页查询用户订单列表', 'GET', '/api/v1/order/list', 'HTTPS', 'application/json', 'published', 'token', 5000, 1, 1, 30, 1, 200, 0,
'{"code": 200, "message": "success", "data": {"total": 100, "records": []}}',
'{"code": 401, "message": "未授权访问", "data": null}',
0, 980, 45600, @user_id, @user_name, NOW()),

-- 商品服务API
('API007', @tenant_id, 'G003', '商品详情', 'product-detail', 'v1', '获取商品详细信息', 'GET', '/api/v1/product/{productId}', 'HTTPS', 'application/json', 'published', 'none', 3000, 1, 1, 600, 1, 500, 0,
'{"code": 200, "message": "success", "data": {"product_id": "P001", "name": "iPhone 15", "price": 5999.00}}',
'{"code": 404, "message": "商品不存在", "data": null}',
0, 3200, 168500, @user_id, @user_name, NOW()),

('API008', @tenant_id, 'G003', '商品搜索', 'product-search', 'v1', '根据关键词搜索商品', 'GET', '/api/v1/product/search', 'HTTPS', 'application/json', 'published', 'none', 5000, 1, 1, 60, 1, 300, 0,
'{"code": 200, "message": "success", "data": {"total": 50, "records": []}}',
'{"code": 400, "message": "搜索关键词不能为空", "data": null}',
0, 2100, 98700, @user_id, @user_name, NOW()),

-- 支付服务API
('API009', @tenant_id, 'G004', '发起支付', 'payment-create', 'v1', '创建支付订单', 'POST', '/api/v1/payment/create', 'HTTPS', 'application/json', 'published', 'signature', 15000, 3, 0, 0, 1, 50, 0,
'{"code": 200, "message": "success", "data": {"payment_id": "PAY001", "pay_url": "https://pay.example.com/..."}}',
'{"code": 500, "message": "支付渠道异常", "data": null}',
1, 450, 21500, @user_id, @user_name, NOW()),

('API010', @tenant_id, 'G004', '支付回调', 'payment-callback', 'v1', '支付结果异步通知回调', 'POST', '/api/v1/payment/callback', 'HTTPS', 'application/json', 'published', 'signature', 10000, 0, 0, 0, 0, 0, 0,
'{"code": 200, "message": "success"}',
'{"code": 400, "message": "签名验证失败", "data": null}',
1, 680, 32100, @user_id, @user_name, NOW()),

-- 消息服务API
('API011', @tenant_id, 'G005', '发送短信', 'sms-send', 'v1', '发送短信验证码', 'POST', '/api/v1/message/sms/send', 'HTTPS', 'application/json', 'published', 'token', 5000, 2, 0, 0, 1, 10, 0,
'{"code": 200, "message": "发送成功", "data": {"message_id": "MSG001"}}',
'{"code": 429, "message": "发送频率过快", "data": null}',
0, 520, 28900, @user_id, @user_name, NOW()),

('API012', @tenant_id, 'G005', '消息推送', 'push-notification', 'v1', 'APP消息推送接口', 'POST', '/api/v1/message/push', 'HTTPS', 'application/json', 'published', 'token', 5000, 2, 0, 0, 1, 100, 0,
'{"code": 200, "message": "推送成功", "data": {"push_id": "PUSH001"}}',
'{"code": 400, "message": "推送目标不能为空", "data": null}',
0, 780, 45200, @user_id, @user_name, NOW()),

-- 草稿API
('API013', @tenant_id, 'G001', '修改密码', 'user-change-password', 'v1', '用户修改密码接口', 'POST', '/api/v1/user/password', 'HTTPS', 'application/json', 'draft', 'token', 5000, 0, 0, 0, 1, 20, 0,
'{"code": 200, "message": "密码修改成功"}',
'{"code": 400, "message": "原密码错误", "data": null}',
0, 0, 0, @user_id, @user_name, NULL);

-- 3. 创建API后端配置
INSERT INTO `api_backend` (`id`, `api_id`, `type`, `protocol`, `method`, `host`, `path`, `timeout`, `connect_timeout`, `mock_delay`) VALUES
('BE001', 'API001', 'http', 'HTTP', 'POST', 'http://user-service:8080', '/internal/user/login', 5000, 1000, 0),
('BE002', 'API002', 'http', 'HTTP', 'POST', 'http://user-service:8080', '/internal/user/register', 5000, 1000, 0),
('BE003', 'API003', 'http', 'HTTP', 'GET', 'http://user-service:8080', '/internal/user/{userId}', 3000, 1000, 0),
('BE004', 'API004', 'http', 'HTTP', 'POST', 'http://order-service:8080', '/internal/order/create', 10000, 2000, 0),
('BE005', 'API005', 'http', 'HTTP', 'GET', 'http://order-service:8080', '/internal/order/{orderId}', 3000, 1000, 0),
('BE006', 'API006', 'http', 'HTTP', 'GET', 'http://order-service:8080', '/internal/order/list', 5000, 1000, 0),
('BE007', 'API007', 'http', 'HTTP', 'GET', 'http://product-service:8080', '/internal/product/{productId}', 3000, 1000, 0),
('BE008', 'API008', 'http', 'HTTP', 'GET', 'http://product-service:8080', '/internal/product/search', 5000, 1000, 0),
('BE009', 'API009', 'http', 'HTTPS', 'POST', 'https://payment-gateway:8443', '/internal/payment/create', 15000, 3000, 0),
('BE010', 'API010', 'http', 'HTTPS', 'POST', 'https://payment-gateway:8443', '/internal/payment/callback', 10000, 2000, 0),
('BE011', 'API011', 'http', 'HTTP', 'POST', 'http://message-service:8080', '/internal/sms/send', 5000, 1000, 0),
('BE012', 'API012', 'http', 'HTTP', 'POST', 'http://message-service:8080', '/internal/push/send', 5000, 1000, 0),
('BE013', 'API013', 'http', 'HTTP', 'POST', 'http://user-service:8080', '/internal/user/password', 5000, 1000, 0);

-- 4. 创建API请求参数
INSERT INTO `api_request_param` (`id`, `api_id`, `name`, `type`, `location`, `required`, `default_value`, `example`, `description`, `sort`) VALUES
-- 用户登录参数
('P001', 'API001', 'username', 'string', 'body', 1, NULL, 'admin', '用户名', 1),
('P002', 'API001', 'password', 'string', 'body', 1, NULL, '123456', '密码', 2),
('P003', 'API001', 'captcha', 'string', 'body', 0, NULL, 'a1b2', '验证码', 3),
-- 用户注册参数
('P004', 'API002', 'phone', 'string', 'body', 1, NULL, '13800138000', '手机号', 1),
('P005', 'API002', 'password', 'string', 'body', 1, NULL, '123456', '密码', 2),
('P006', 'API002', 'sms_code', 'string', 'body', 1, NULL, '123456', '短信验证码', 3),
-- 获取用户信息参数
('P007', 'API003', 'userId', 'string', 'path', 1, NULL, 'U123456', '用户ID', 1),
('P008', 'API003', 'Authorization', 'string', 'header', 1, NULL, 'Bearer eyJhbGci...', '访问令牌', 2),
-- 创建订单参数
('P009', 'API004', 'Authorization', 'string', 'header', 1, NULL, 'Bearer eyJhbGci...', '访问令牌', 1),
('P010', 'API004', 'product_id', 'string', 'body', 1, NULL, 'P001', '商品ID', 2),
('P011', 'API004', 'quantity', 'integer', 'body', 1, '1', '2', '购买数量', 3),
-- 订单详情参数
('P012', 'API005', 'orderId', 'string', 'path', 1, NULL, 'ORD202312220001', '订单ID', 1),
('P013', 'API005', 'Authorization', 'string', 'header', 1, NULL, 'Bearer eyJhbGci...', '访问令牌', 2),
-- 订单列表参数
('P014', 'API006', 'Authorization', 'string', 'header', 1, NULL, 'Bearer eyJhbGci...', '访问令牌', 1),
('P015', 'API006', 'status', 'string', 'query', 0, NULL, 'paid', '订单状态', 2),
('P016', 'API006', 'page', 'integer', 'query', 0, '1', '1', '页码', 3),
('P017', 'API006', 'size', 'integer', 'query', 0, '10', '10', '每页数量', 4),
-- 商品详情参数
('P018', 'API007', 'productId', 'string', 'path', 1, NULL, 'P001', '商品ID', 1),
-- 商品搜索参数
('P019', 'API008', 'keyword', 'string', 'query', 1, NULL, 'iPhone', '搜索关键词', 1),
('P020', 'API008', 'page', 'integer', 'query', 0, '1', '1', '页码', 2),
('P021', 'API008', 'size', 'integer', 'query', 0, '20', '20', '每页数量', 3),
-- 发起支付参数
('P022', 'API009', 'X-Sign', 'string', 'header', 1, NULL, 'a1b2c3d4...', '请求签名', 1),
('P023', 'API009', 'order_id', 'string', 'body', 1, NULL, 'ORD202312220001', '订单ID', 2),
('P024', 'API009', 'amount', 'number', 'body', 1, NULL, '299.00', '支付金额', 3),
('P025', 'API009', 'channel', 'string', 'body', 1, NULL, 'alipay', '支付渠道', 4),
-- 发送短信参数
('P026', 'API011', 'Authorization', 'string', 'header', 1, NULL, 'Bearer eyJhbGci...', '访问令牌', 1),
('P027', 'API011', 'phone', 'string', 'body', 1, NULL, '13800138000', '手机号', 2),
('P028', 'API011', 'template_id', 'string', 'body', 1, NULL, 'SMS_001', '短信模板ID', 3),
-- 消息推送参数
('P029', 'API012', 'Authorization', 'string', 'header', 1, NULL, 'Bearer eyJhbGci...', '访问令牌', 1),
('P030', 'API012', 'user_ids', 'array', 'body', 1, NULL, '["U001", "U002"]', '目标用户列表', 2),
('P031', 'API012', 'title', 'string', 'body', 1, NULL, '系统通知', '推送标题', 3),
('P032', 'API012', 'content', 'string', 'body', 1, NULL, '您有一条新消息', '推送内容', 4);

-- 5. 创建API标签
INSERT INTO `api_tag` (`id`, `api_id`, `tag_name`) VALUES
('T001', 'API001', '核心接口'),
('T002', 'API001', '认证相关'),
('T003', 'API002', '核心接口'),
('T004', 'API002', '认证相关'),
('T005', 'API003', '数据查询'),
('T006', 'API004', '核心接口'),
('T007', 'API004', '订单相关'),
('T008', 'API005', '数据查询'),
('T009', 'API006', '数据查询'),
('T010', 'API007', '数据查询'),
('T011', 'API008', '数据查询'),
('T012', 'API009', '核心接口'),
('T013', 'API009', '支付相关'),
('T014', 'API010', '支付相关'),
('T015', 'API011', '消息相关'),
('T016', 'API012', '消息相关');
