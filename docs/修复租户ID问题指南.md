# 修复AppKey认证中的租户ID传递问题

## 📋 问题描述

外部用户通过SDK调用开放API时，使用AppKey+AppSecret认证（无需登录）。但是在下游微服务中，MyBatis-Plus租户拦截器会从 `UserContextHolder.getCurrentTenantId()` 获取租户ID。如果租户ID为null，会返回 `"UNKNOWN"`，导致查询不到任何数据。

## ✅ 解决方案

**方案1：确保AppKeyInfo包含租户ID（已实施）**

我们已经在代码中添加了完整的租户ID传递链路和日志追踪：

### 代码修改说明

#### 1. AppCenterDubboServiceImpl.java
添加了日志追踪，记录从数据库查询到的租户ID：
```java
// ✅ 日志追踪：记录租户ID
log.info("[AppKey认证] AppInfo转DTO - AppKey: {}, TenantId: {}", 
    appInfo.getAppKey(), appInfo.getTenantId());
```

#### 2. AppKeyService.java (Gateway)
添加了日志追踪，记录DTO转换后的租户ID：
```java
// ✅ 日志追踪：记录从DTO转换后的租户ID
log.info("[AppKey认证] DTO转AppKeyInfo - AppKey: {}, TenantId: {}", 
    dto.getAppKey(), dto.getTenantId());
```

#### 3. AppKeyAuthenticationFilter.java (Gateway)
增强了日志，记录传递给下游服务的租户ID：
```java
// ✅ 日志追踪：记录传递给下游的租户ID
log.info("AppKey认证成功 - AppKey: {}, AppId: {}, TenantId: {}, ApiId: {}, Path: {}", 
    appKey, appKeyInfo.getAppId(), appKeyInfo.getTenantId(), apiId, path);
```

---

## 🔍 问题诊断步骤

### 步骤1：检查数据库中的租户ID

```sql
-- 1. 检查 app_info 表结构
DESC app_info;

-- 2. 检查现有应用的租户ID
SELECT 
    id,
    name,
    app_key,
    tenant_id,
    status,
    created_at
FROM app_info
WHERE deleted = 0
ORDER BY created_at DESC
LIMIT 10;

-- 3. 检查测试AppKey的租户ID
SELECT 
    id,
    name,
    app_key,
    tenant_id,
    status
FROM app_info
WHERE app_key = 'IH4315340gtRfKPamTHVU4GE';

-- 4. 统计没有租户ID的应用数量
SELECT COUNT(*) as count_without_tenant
FROM app_info
WHERE deleted = 0 
AND (tenant_id IS NULL OR tenant_id = '');
```

### 步骤2：修复数据库中缺少租户ID的应用

如果发现应用的 `tenant_id` 为 NULL，需要修复：

```sql
-- 方案A：如果有默认租户，批量设置
-- 替换 'your-default-tenant-id' 为实际的默认租户ID
UPDATE app_info
SET tenant_id = 'your-default-tenant-id'
WHERE deleted = 0
AND (tenant_id IS NULL OR tenant_id = '');

-- 方案B：为测试AppKey单独设置租户ID
UPDATE app_info
SET tenant_id = 'tenant-test'
WHERE app_key = 'IH4315340gtRfKPamTHVU4GE';

-- 验证修复结果
SELECT 
    id,
    name,
    app_key,
    tenant_id,
    status
FROM app_info
WHERE app_key = 'IH4315340gtRfKPamTHVU4GE';
```

### 步骤3：清除Redis缓存

修复数据库后，必须清除Gateway中的AppKey缓存：

```bash
# 连接Redis
redis-cli

# 删除特定AppKey的缓存
DEL gateway:appkey:IH4315340gtRfKPamTHVU4GE

# 或者删除所有AppKey缓存
KEYS gateway:appkey:*
# 逐个删除或使用 SCAN + DEL

# 验证缓存已清除
GET gateway:appkey:IH4315340gtRfKPamTHVU4GE
```

---

## 🧪 测试验证

### 测试1：查看日志追踪

启动服务后，使用SDK调用API，观察日志：

```log
# 预期看到的日志序列：

# 1. App Center Service - 从数据库查询
[AppKey认证] AppInfo转DTO - AppKey: IH4315340gtRfKPamTHVU4GE, TenantId: tenant-test

# 2. Gateway Service - DTO转换
[AppKey认证] DTO转AppKeyInfo - AppKey: IH4315340gtRfKPamTHVU4GE, TenantId: tenant-test

# 3. Gateway Filter - 传递给下游
AppKey认证成功 - AppKey: IH4315340gtRfKPamTHVU4GE, AppId: xxx, TenantId: tenant-test, ...

# 4. 下游服务 - 接收租户ID（在任意微服务的日志中）
多租户拦截器 - 获取租户ID: tenant-test
```

**如果看到以下日志，说明有问题**：

```log
# ❌ 问题1：数据库中租户ID为null
[AppKey认证] AppInfo转DTO - AppKey: xxx, TenantId: null

# ❌ 问题2：传递给下游的租户ID为null
AppKey认证成功 - AppKey: xxx, AppId: xxx, TenantId: null, ...

# ❌ 问题3：下游服务使用UNKNOWN
多租户拦截器 - 未找到租户ID，使用默认值: UNKNOWN
```

### 测试2：使用SDK调用API

```java
// QuickTest.java
public static void main(String[] args) {
    IntelliHubConfig config = IntelliHubConfig.builder()
            .baseUrl("http://localhost:8080")
            .appKey("IH4315340gtRfKPamTHVU4GE")
            .appSecret("l8AhxFW5SIQJ5L4IDrxiVNU7jlZAplsF")
            .build();

    IntelliHubClient client = IntelliHubClient.create(config);
    
    try {
        // 调用API
        ApiResponse<Map> response = client.get(
            "/open/app/IH4315340gtRfKPamTHVU4GE", 
            Map.class
        );
        
        System.out.println("调用成功: " + response.isSuccess());
        System.out.println("响应数据: " + response.getData());
        
        // ✅ 如果能成功获取数据，说明租户ID传递正常
    } catch (Exception e) {
        System.err.println("调用失败: " + e.getMessage());
        e.printStackTrace();
    }
}
```

### 测试3：检查MyBatis生成的SQL

在下游服务的日志中，应该看到类似的SQL（如果启用了MyBatis SQL日志）：

```sql
-- ✅ 正确的SQL（包含正确的tenant_id）
SELECT * FROM app_info WHERE id = ? AND tenant_id = 'tenant-test'

-- ❌ 错误的SQL（tenant_id为UNKNOWN）
SELECT * FROM app_info WHERE id = ? AND tenant_id = 'UNKNOWN'
```

---

## 📊 完整的数据流

```
┌─────────────────┐
│  SDK调用        │
│  (QuickTest)    │
└────────┬────────┘
         │ AppKey + Signature
         ↓
┌─────────────────────────────────────────┐
│  Gateway - AppKeyAuthenticationFilter   │
│  1. 验证AppKey签名                      │
│  2. 从AppKeyService获取AppKeyInfo       │
└────────┬────────────────────────────────┘
         │ Dubbo RPC
         ↓
┌─────────────────────────────────────────┐
│  App Center Service                     │
│  1. 查询数据库: app_info                │
│  2. 获取 tenant_id 字段 ⭐              │
│  3. 返回 AppKeyInfoDTO                  │
└────────┬────────────────────────────────┘
         │ 返回DTO
         ↓
┌─────────────────────────────────────────┐
│  Gateway - AppKeyService                │
│  1. convertFromDTO                      │
│  2. 设置 tenantId ⭐                    │
│  3. 返回 AppKeyInfo                     │
└────────┬────────────────────────────────┘
         │ 返回Info
         ↓
┌─────────────────────────────────────────┐
│  Gateway - AppKeyAuthenticationFilter   │
│  1. 添加请求头: X-Tenant-Id ⭐          │
│  2. 转发请求到下游                      │
└────────┬────────────────────────────────┘
         │ HTTP + Header: X-Tenant-Id
         ↓
┌─────────────────────────────────────────┐
│  下游微服务 (API Platform等)           │
│  1. UserContextInterceptor读取请求头    │
│  2. 设置到 ThreadLocal ⭐               │
└────────┬────────────────────────────────┘
         │ ThreadLocal
         ↓
┌─────────────────────────────────────────┐
│  MyBatis-Plus租户拦截器                 │
│  1. UserContextHolder.getTenantId() ⭐  │
│  2. 自动拼接 WHERE tenant_id = ?        │
└─────────────────────────────────────────┘
```

**关键检查点（标记⭐的地方）**：
1. ✅ 数据库 `app_info.tenant_id` 不为 NULL
2. ✅ DTO 转换设置 `tenantId`
3. ✅ Gateway 添加 `X-Tenant-Id` 请求头
4. ✅ 下游服务读取并设置到 ThreadLocal
5. ✅ 租户拦截器获取到正确的租户ID

---

## 🔧 常见问题排查

### 问题1：日志显示 TenantId 为 null

**原因**：数据库中 `app_info.tenant_id` 字段为 NULL

**解决**：
```sql
-- 更新应用的租户ID
UPDATE app_info
SET tenant_id = 'tenant-test'
WHERE app_key = 'IH4315340gtRfKPamTHVU4GE';

-- 清除Redis缓存
redis-cli DEL gateway:appkey:IH4315340gtRfKPamTHVU4GE
```

### 问题2：修复后仍然查询不到数据

**原因**：Redis缓存未清除，仍使用旧数据

**解决**：
```bash
# 清除特定AppKey缓存
redis-cli DEL gateway:appkey:IH4315340gtRfKPamTHVU4GE

# 或重启Gateway服务
```

### 问题3：下游服务拦截器仍使用 UNKNOWN

**原因**：`UserContextInterceptor` 未正确读取 `X-Tenant-Id` 请求头

**检查**：
```java
// 确认 UserContextInterceptor 已配置
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**");
    }
}
```

### 问题4：创建新应用时忘记设置租户ID

**预防措施**：在创建应用的Service中添加校验
```java
// AppServiceImpl.java - createApp方法
public AppInfo createApp(CreateAppRequest request) {
    String tenantId = UserContextHolder.getCurrentTenantId();
    
    // ✅ 确保租户ID不为空
    if (tenantId == null || tenantId.isEmpty()) {
        throw new BusinessException("租户ID不能为空");
    }
    
    AppInfo app = new AppInfo();
    app.setTenantId(tenantId);  // ⭐ 关键：设置租户ID
    // ... 其他字段设置
    
    return app;
}
```

---

## ✅ 验证清单

修复后，请依次确认：

- [ ] 数据库中 `app_info.tenant_id` 字段不为 NULL
- [ ] Redis缓存已清除
- [ ] Gateway日志显示正确的租户ID
- [ ] 下游服务日志显示正确的租户ID
- [ ] MyBatis SQL包含正确的 `tenant_id` 条件
- [ ] SDK调用能够成功获取数据
- [ ] 不同租户的应用相互隔离

---

## 🎉 修复完成标志

当你看到以下日志序列，说明修复成功：

```log
✅ [AppKey认证] AppInfo转DTO - AppKey: IH4315340gtRfKPamTHVU4GE, TenantId: tenant-test
✅ [AppKey认证] DTO转AppKeyInfo - AppKey: IH4315340gtRfKPamTHVU4GE, TenantId: tenant-test
✅ AppKey认证成功 - AppKey: xxx, AppId: xxx, TenantId: tenant-test, ApiId: xxx, Path: xxx
✅ 多租户拦截器 - 获取租户ID: tenant-test
✅ SELECT * FROM app_info WHERE id = ? AND tenant_id = 'tenant-test'
```

**SDK调用成功返回数据！** 🚀

