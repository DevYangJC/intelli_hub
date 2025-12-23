# 多租户支持文档

## 概述

IntelliHub IAM 服务支持多租户架构，允许系统同时为多个独立的租户提供服务，同时保证租户间的数据隔离。

## 核心概念

### 租户（Tenant）
- **租户ID**: 每个租户的唯一标识符
- **租户编码**: 租户的业务编码，用于业务场景识别
- **租户类型**: 试用、标准、高级、企业
- **状态**: 正常、禁用、过期

### 用户租户关联（TenantUser）
- 一个用户可以属于多个租户
- 支持主租户概念（用户的主要归属租户）
- 支持租户管理员角色
- 每个租户独立管理用户权限

## 实现机制

### 1. 数据隔离
- 通过 MyBatis Plus 的多租户插件实现数据自动隔离
- 在 SQL 查询时自动添加 `tenant_id` 条件
- 支持表级别的多租户隔离配置

### 2. 上下文管理
- 使用 ThreadLocal 存储当前请求的租户信息
- 通过拦截器从请求头提取租户信息
- 请求结束后自动清除租户上下文

### 3. API 支持方式
客户端需要在请求头中传递租户信息：
```
X-Tenant-Id: tenant-001
X-Tenant-Code: tenant-a
```

## 数据库设计

### 租户表（sys_tenant）
```sql
- tenant_id: 租户ID（主键）
- tenant_code: 租户编码（唯一）
- tenant_name: 租户名称
- tenant_type: 租户类型
- status: 租户状态
- max_users: 最大用户数限制
- current_users: 当前用户数
```

### 用户租户关联表（sys_user_tenant）
```sql
- id: 主键
- user_id: 用户ID
- tenant_id: 租户ID
- role_id: 角色ID
- is_primary: 是否主租户
- is_admin: 是否租户管理员
- status: 关联状态
```

## 使用说明

### 1. 创建租户
```bash
POST /tenant
Content-Type: application/json

{
  "tenantCode": "company-a",
  "tenantName": "公司A",
  "tenantType": 2,
  "contactName": "张三",
  "contactEmail": "zhangsan@companya.com"
}
```

### 2. 添加用户到租户
```bash
POST /tenant/{tenantId}/users/{userId}
?roleId=role-001
```

### 3. 获取用户的租户列表
```bash
GET /tenant/user/{userId}
```

### 4. 租户数据查询
所有业务接口会自动根据当前租户上下文过滤数据，无需额外处理。

## 注意事项

### 1. 不需要租户隔离的表
以下表不进行租户隔离：
- 系统管理表（租户、角色、权限等）
- 字典表（全局共享）
- 日志表（全局记录）
- 定时任务表（全局管理）

### 2. 系统管理员接口
系统管理员可以通过不传递租户信息的方式访问所有租户数据。

### 3. 性能考虑
- 租户隔离通过数据库索引优化
- 建议为业务表的 tenant_id 字段添加索引
- 租户数量较多时考虑分库分表策略

## 最佳实践

1. **租户编码规范**: 使用有意义的英文或拼音编码，如 `company-a`
2. **数据迁移**: 新增租户支持时，需要为现有用户分配默认租户
3. **权限控制**: 结合 RBAC 系统，在每个租户内部实现精细的权限管理
4. **监控告警**: 监控租户数据量、用户数等指标，及时扩容