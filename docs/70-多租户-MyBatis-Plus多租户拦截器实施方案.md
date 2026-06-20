# MyBatis-Plus 多租户拦截器实施方案

> 版本：1.0.0 | 更新日期：2026-01-05
> 目标：通过 MyBatis-Plus 拦截器自动实现 SQL 租户隔离，替代手动拼接方式

---

## 目录

- [1. 背景与目标](#1-背景与目标)
- [2. 现状分析](#2-现状分析)
- [3. 技术方案](#3-技术方案)
- [4. 实施步骤](#4-实施步骤)
- [5. 各服务配置清单](#5-各服务配置清单)
- [6. 测试验证](#6-测试验证)
- [7. 注意事项与最佳实践](#7-注意事项与最佳实践)

---

## 1. 背景与目标

### 1.1 当前问题

目前各微服务的租户隔离采用**手动方式**：
1. 从请求参数或上下文中获取 `tenantId`
2. 在 Service 层或 Mapper 层手动拼接 `WHERE tenant_id = ?` 条件
3. 每个查询都需要显式传递和处理租户ID

**存在的问题**：
- ❌ 代码冗余：每个查询都需要手动处理
- ❌ 容易遗漏：新增接口可能忘记加租户条件
- ❌ 安全风险：遗漏可能导致数据泄露
- ❌ 维护困难：租户逻辑散落在各处

### 1.2 目标

通过 MyBatis-Plus 的 `TenantLineInnerInterceptor` 实现：
- ✅ SQL 自动注入租户条件
- ✅ 零代码侵入
- ✅ 统一配置管理
- ✅ 支持特殊场景豁免

---

## 2. 现状分析

### 2.1 已有基础设施 ✅

项目中 **已经实现** 了多租户拦截器的基础设施：

| 组件 | 位置 | 状态 |
|------|------|------|
| `IntelliHubTenantLineHandler` | `mybatis-helper-spring-boot-starter` | ✅ 已实现 |
| `TenantProperties` | `mybatis-helper-spring-boot-starter` | ✅ 已实现 |
| `MybatisPlusAutoConfiguration` | `mybatis-helper-spring-boot-starter` | ✅ 已实现 |
| `UserContext` | `common-helper` | ✅ 已实现 |
| `UserContextHolder` | `common-helper` | ✅ 已实现 |
| `UserContextInterceptor` | `common-helper` | ✅ 已实现 |

### 2.2 现有实现架构

```
┌─────────────────────────────────────────────────────────────────┐
│  请求流程                                                        │
│                                                                  │
│  Gateway ──> X-Tenant-Id Header ──> UserContextInterceptor      │
│                                           │                      │
│                                           ▼                      │
│                                    UserContextHolder             │
│                                    (ThreadLocal存储)             │
│                                           │                      │
│                                           ▼                      │
│                              IntelliHubTenantLineHandler         │
│                              (从UserContextHolder获取tenantId)   │
│                                           │                      │
│                                           ▼                      │
│                              TenantLineInnerInterceptor          │
│                              (自动注入 WHERE tenant_id = ?)      │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 待解决问题

| 问题 | 详情 |
|------|------|
| **配置未启用** | 部分服务未配置 `intellihub.mybatis.tenant.enabled=true` |
| **忽略表不完整** | 各服务需要根据自身表结构配置 `ignore-tables` |
| **拦截器未注册** | `UserContextInterceptor` 需要在各服务中注册到 WebMvcConfigurer |
| **手动代码残留** | 需要移除 Service/Mapper 中手动处理租户ID的代码 |

---

## 3. 技术方案

### 3.1 核心组件

#### 3.1.1 TenantLineHandler（已实现）

```java
// 位置: mybatis-helper-spring-boot-starter
// 文件: IntelliHubTenantLineHandler.java

@Override
public Expression getTenantId() {
    String tenantId = UserContextHolder.getCurrentTenantId();
    return new StringValue(tenantId != null ? tenantId : "UNKNOWN");
}

@Override
public boolean ignoreTable(String tableName) {
    // 1. 系统管理员豁免
    if (UserContextHolder.isSystemAdmin() || UserContextHolder.isIgnoreTenant()) {
        return true;
    }
    // 2. 配置的忽略表
    return tenantProperties.getIgnoreTables().contains(tableName);
}
```

#### 3.1.2 配置属性（已实现）

```yaml
# 配置前缀: intellihub.mybatis.tenant
intellihub:
  mybatis:
    tenant:
      enabled: true                    # 是否启用
      column: tenant_id                # 租户字段名
      ignore-tables:                   # 忽略表列表
        - iam_tenant
        - sys_config
```

#### 3.1.3 上下文拦截器（已实现）

```java
// 位置: common-helper
// 文件: UserContextInterceptor.java

// 从请求头获取租户ID，存入ThreadLocal
String tenantIdStr = request.getHeader("X-Tenant-Id");
context.setTenantIdStr(tenantIdStr);
UserContextHolder.set(context);
```

### 3.2 SQL 注入效果

**原始 SQL：**
```sql
SELECT * FROM api_info WHERE status = 'published'
```

**拦截后 SQL：**
```sql
SELECT * FROM api_info WHERE status = 'published' AND tenant_id = '1'
```

**支持的 SQL 类型：**
- ✅ SELECT（包括子查询、JOIN）
- ✅ INSERT（自动添加 tenant_id 字段）
- ✅ UPDATE（WHERE 条件添加）
- ✅ DELETE（WHERE 条件添加）

---

## 4. 实施步骤

### 📋 实施清单

| 阶段 | 步骤 | 模块 | 优先级 |
|:----:|------|------|:------:|
| **一** | 完善 Starter 配置 | `mybatis-helper-spring-boot-starter` | P0 |
| **二** | 创建 Web 配置类 | `common-helper` | P0 |
| **三** | 各服务启用配置 | 各业务服务 | P0 |
| **四** | 移除手动租户代码 | 各业务服务 | P1 |
| **五** | 测试验证 | 全部 | P0 |

---

### 阶段一：完善 Starter 配置

**模块**：`inner-integration/mybatis-helper-spring-boot-starter`

#### Step 1.1：检查自动配置注册

确保 `spring.factories` 或 `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 正确注册：

```properties
# META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.intellihub.mybatis.config.MybatisPlusAutoConfiguration
```

#### Step 1.2：优化默认忽略表配置

修改 `TenantProperties.java`，完善默认忽略表：

```java
private List<String> ignoreTables = new ArrayList<String>() {{
    // === IAM 服务 ===
    add("iam_tenant");           // 租户表本身
    add("iam_permission");       // 权限表（全局）
    add("iam_menu");             // 菜单表（全局）
    add("iam_login_log");        // 登录日志
    
    // === 系统共享表 ===
    add("sys_config");           // 系统配置
    add("sys_dict");             // 数据字典
    add("sys_dict_type");        // 字典类型
}};
```

---

### 阶段二：创建 Web 配置类

**模块**：`inner-integration/common-helper`

#### Step 2.1：创建 WebMvcConfig

创建新文件 `com.intellihub.config.WebMvcConfig.java`：

```java
package com.intellihub.config;

import com.intellihub.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 自动注册 UserContextInterceptor
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public WebMvcConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/actuator/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**"
                );
    }
}
```

#### Step 2.2：注册自动配置

在 `common-helper` 的 `META-INF/spring.factories` 中添加：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.intellihub.config.WebMvcConfig
```

---

### 阶段三：各服务启用配置

**模块**：各业务服务的 `application.yml`

#### Step 3.1：IAM 服务 (intelli-auth-iam-service)

```yaml
# application.yml
intellihub:
  mybatis:
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables:
        # IAM 特有忽略表
        - iam_tenant
        - iam_permission
        - iam_menu
        - iam_role_permission
        - iam_login_log
```

#### Step 3.2：API 平台服务 (intelli-api-platform-service)

```yaml
intellihub:
  mybatis:
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables:
        - sys_config
        - sys_announcement    # 全局公告
```

#### Step 3.3：应用中心服务 (intelli-app-center-service)

```yaml
intellihub:
  mybatis:
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables: []       # 无需忽略
```

#### Step 3.4：治理中心服务 (intelli-governance-service)

```yaml
intellihub:
  mybatis:
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables:
        - cfg_alert_rule_template  # 全局告警模板
```

#### Step 3.5：搜索服务 (intelli-search-service)

```yaml
intellihub:
  mybatis:
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables: []
```

#### Step 3.6：事件服务 (intelli-event-service)

```yaml
intellihub:
  mybatis:
    tenant:
      enabled: true
      column: tenant_id
      ignore-tables: []
```

---

### 阶段四：移除手动租户代码

**模块**：各业务服务的 Service/Mapper 层

#### Step 4.1：识别需要修改的代码

搜索以下模式的代码：

```java
// 模式1：手动添加租户条件
queryWrapper.eq("tenant_id", tenantId);

// 模式2：SQL 中硬编码
@Select("SELECT * FROM api_info WHERE tenant_id = #{tenantId}")

// 模式3：在参数中传递租户ID
public List<ApiInfo> listByTenant(Long tenantId);
```

#### Step 4.2：移除手动代码

**修改前：**
```java
public List<ApiInfo> list(ApiQueryRequest request) {
    LambdaQueryWrapper<ApiInfo> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ApiInfo::getTenantId, request.getTenantId());  // 手动添加
    wrapper.eq(ApiInfo::getStatus, request.getStatus());
    return apiInfoMapper.selectList(wrapper);
}
```

**修改后：**
```java
public List<ApiInfo> list(ApiQueryRequest request) {
    LambdaQueryWrapper<ApiInfo> wrapper = new LambdaQueryWrapper<>();
    // tenant_id 由拦截器自动添加
    wrapper.eq(ApiInfo::getStatus, request.getStatus());
    return apiInfoMapper.selectList(wrapper);
}
```

#### Step 4.3：修改 Mapper XML

**修改前：**
```xml
<select id="selectByName" resultType="ApiInfo">
    SELECT * FROM api_info 
    WHERE tenant_id = #{tenantId} AND name = #{name}
</select>
```

**修改后：**
```xml
<select id="selectByName" resultType="ApiInfo">
    SELECT * FROM api_info WHERE name = #{name}
    <!-- tenant_id 由拦截器自动添加 -->
</select>
```

---

### 阶段五：测试验证

#### Step 5.1：单元测试

```java
@SpringBootTest
class TenantInterceptorTest {

    @Autowired
    private ApiInfoMapper apiInfoMapper;

    @Test
    void testAutoTenantInjection() {
        // 设置租户上下文
        UserContext context = new UserContext();
        context.setTenantIdStr("1");
        UserContextHolder.set(context);

        try {
            // 执行查询，应只返回 tenant_id=1 的数据
            List<ApiInfo> list = apiInfoMapper.selectList(null);
            
            // 验证所有数据的 tenant_id
            for (ApiInfo api : list) {
                assertEquals("1", api.getTenantId());
            }
        } finally {
            UserContextHolder.clear();
        }
    }
}
```

#### Step 5.2：集成测试

```bash
# 1. 启动服务
mvn spring-boot:run -pl intelli-api-platform-service

# 2. 调用 API（模拟网关传递租户头）
curl -X GET "http://localhost:8082/api/v1/apis" \
  -H "X-User-Id: 1" \
  -H "X-Tenant-Id: 1"

# 3. 查看 SQL 日志，确认自动添加了 tenant_id 条件
```

#### Step 5.3：SQL 日志验证

开启 MyBatis SQL 日志：

```yaml
logging:
  level:
    com.intellihub: DEBUG
    com.baomidou.mybatisplus: DEBUG
```

预期日志输出：
```
==> Preparing: SELECT * FROM api_info WHERE tenant_id = ? AND status = ?
==> Parameters: 1(String), published(String)
```

---

## 5. 各服务配置清单

### 5.1 配置汇总表

| 服务 | 启用 | 忽略表 |
|------|:----:|--------|
| intelli-auth-iam-service | ✅ | `iam_tenant`, `iam_permission`, `iam_menu`, `iam_role_permission`, `iam_login_log` |
| intelli-api-platform-service | ✅ | `sys_config`, `sys_announcement` |
| intelli-app-center-service | ✅ | (无) |
| intelli-governance-service | ✅ | `cfg_alert_rule_template` |
| intelli-search-service | ✅ | (无) |
| intelli-event-service | ✅ | (无) |

### 5.2 表级租户字段检查

确保所有业务表都有 `tenant_id` 字段：

```sql
-- 检查表结构
SHOW COLUMNS FROM api_info LIKE 'tenant_id';

-- 如果缺少，需要添加
ALTER TABLE api_info ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT '';
CREATE INDEX idx_api_info_tenant_id ON api_info(tenant_id);
```

---

## 6. 测试验证

### 6.1 验证矩阵

| 测试场景 | 预期结果 | 验证方法 |
|----------|----------|----------|
| 普通查询 | SQL 自动添加 `tenant_id = ?` | 查看日志 |
| INSERT 操作 | 自动插入 `tenant_id` 字段 | 检查数据库 |
| UPDATE 操作 | WHERE 条件添加 `tenant_id` | 查看日志 |
| DELETE 操作 | WHERE 条件添加 `tenant_id` | 查看日志 |
| JOIN 查询 | 所有关联表添加条件 | 查看日志 |
| 系统管理员 | 不添加租户条件 | 查看日志 |
| 忽略表 | 不添加租户条件 | 查看日志 |
| 跨租户查询 | 返回空结果 | API 测试 |

### 6.2 安全性验证

```bash
# 测试1：正常租户请求
curl -H "X-Tenant-Id: 1" http://localhost:8082/api/v1/apis
# 预期：只返回 tenant_id=1 的数据

# 测试2：不传租户头
curl http://localhost:8082/api/v1/apis
# 预期：tenant_id='UNKNOWN'，返回空数据

# 测试3：伪造租户头
curl -H "X-Tenant-Id: 999" http://localhost:8082/api/v1/apis
# 预期：只返回 tenant_id=999 的数据（应为空）
```

---

## 7. 注意事项与最佳实践

### 7.1 特殊场景处理

#### 场景1：跨租户查询（管理后台）

```java
// 临时豁免租户隔离
UserContextHolder.setIgnoreTenant(true);
try {
    // 跨租户查询
    List<ApiInfo> allApis = apiInfoMapper.selectList(null);
} finally {
    UserContextHolder.setIgnoreTenant(false);
}
```

#### 场景2：定时任务（无请求上下文）

```java
@Scheduled(cron = "0 0 * * * ?")
public void syncTask() {
    // 设置系统上下文
    UserContext context = new UserContext();
    context.setIgnoreTenant(true);  // 定时任务豁免租户隔离
    UserContextHolder.set(context);
    
    try {
        // 执行任务
    } finally {
        UserContextHolder.clear();
    }
}
```

#### 场景3：异步线程

```java
// 父线程上下文
UserContext parentContext = UserContextHolder.get();

executor.submit(() -> {
    // 传递上下文到子线程
    UserContextHolder.set(parentContext);
    try {
        // 异步任务
    } finally {
        UserContextHolder.clear();
    }
});
```

### 7.2 最佳实践

| 实践 | 说明 |
|------|------|
| **不要在 SQL 中硬编码 tenant_id** | 让拦截器自动处理 |
| **实体类保留 tenant_id 字段** | 用于数据展示和导出 |
| **INSERT 时不需要手动设置 tenant_id** | 拦截器会自动添加 |
| **关联查询使用 JOIN 而非子查询** | 拦截器对 JOIN 支持更好 |
| **测试时务必设置租户上下文** | 避免测试数据混淆 |

### 7.3 常见问题

#### Q1：INSERT 时报错 tenant_id 不能为空？

**原因**：拦截器只在 SQL 层面添加条件，不会修改实体对象。
**解决**：在自动填充处理器中设置 tenant_id。

```java
// AutoFillMetaObjectHandler.java
@Override
public void insertFill(MetaObject metaObject) {
    String tenantId = UserContextHolder.getCurrentTenantId();
    this.strictInsertFill(metaObject, "tenantId", String.class, tenantId);
}
```

#### Q2：某些表不需要租户隔离？

**解决**：在配置中添加到 `ignore-tables` 列表。

#### Q3：系统管理员需要看到所有数据？

**解决**：网关在解析角色后，设置 `X-User-Roles: SYSTEM_ADMIN` 头，拦截器会自动豁免。

---

## 附录

### A. 文件清单

| 文件 | 路径 | 操作 |
|------|------|:----:|
| `IntelliHubTenantLineHandler.java` | `mybatis-helper-spring-boot-starter` | 检查 |
| `TenantProperties.java` | `mybatis-helper-spring-boot-starter` | 修改 |
| `MybatisPlusAutoConfiguration.java` | `mybatis-helper-spring-boot-starter` | 检查 |
| `WebMvcConfig.java` | `common-helper` | **新建** |
| `spring.factories` | `common-helper/META-INF` | 修改 |
| `application.yml` | 各服务 | 修改 |

### B. 实施检查清单

- [ ] Step 1：检查 starter 自动配置注册
- [ ] Step 2：创建 WebMvcConfig 配置类
- [ ] Step 3：各服务添加租户配置
- [ ] Step 4：检查数据库表 tenant_id 字段
- [ ] Step 5：移除手动租户处理代码
- [ ] Step 6：启用 SQL 日志验证
- [ ] Step 7：执行集成测试
- [ ] Step 8：安全性测试

### C. 参考资料

- [MyBatis-Plus 多租户插件官方文档](https://baomidou.com/pages/aef2f2/)
- [项目架构文档](./10-架构设计-项目架构文档.md)

---

*本文档随项目演进持续更新，如有疑问请联系项目组。*
