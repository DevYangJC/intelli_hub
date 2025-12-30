-- 事件中心初始化脚本 - 第一阶段事件定义
-- 包含：API 生命周期事件 + 告警事件

-- API 发布事件
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
