-- 事件中心初始化脚本 -- 事件中心 - 事件定义初始化脚本
-- 第一阶段：API 生命周期事件 + 告警事件
-- 第二阶段：用户事件

-- ========================================
-- 第一阶段：API 生命周期事件 + 告警事件
-- ========================================

    use intelli_hub_event;

-- API 生命周期事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'api.published', 'API发布事件', 'API', 'API发布时触发', 'ACTIVE', NOW(), NOW());

-- API 更新事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'api.updated', 'API更新事件', 'API', 'API更新时触发', 'ACTIVE', NOW(), NOW());

-- API 下线事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'api.offline', 'API下线事件', 'API', 'API下线时触发', 'ACTIVE', NOW(), NOW());

-- API 删除事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'api.deleted', 'API删除事件', 'API', 'API删除时触发', 'ACTIVE', NOW(), NOW());

-- 告警触发事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'alert.triggered', '告警触发事件', 'ALERT', '告警触发时触发', 'ACTIVE', NOW(), NOW());

-- 告警恢复事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'alert.resolved', '告警恢复事件', 'ALERT', '告警恢复时触发', 'ACTIVE', NOW(), NOW());

-- ========================================
-- 第二阶段：用户事件
-- ========================================

-- 用户创建事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'user.created', '用户创建事件', 'USER', '用户创建时触发', 'ACTIVE', NOW(), NOW());

-- 用户更新事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'user.updated', '用户更新事件', 'USER', '用户更新时触发', 'ACTIVE', NOW(), NOW());

-- 用户删除事件
INSERT INTO event_definition (id, tenant_id, event_code, event_name, event_type, description, status, created_at, updated_at)
VALUES (REPLACE(UUID(), '-', ''), 'system', 'user.deleted', '用户删除事件', 'USER', '用户删除时触发', 'ACTIVE', NOW(), NOW());
