# IntelliHub 开发问题与知识点整理

> 本文档记录项目开发过程中遇到的问题、解决方案及相关知识点，供后续开发参考。

---

## 目录

1. [数据库相关](#1-数据库相关)
2. [认证授权相关](#2-认证授权相关)
3. [网关相关](#3-网关相关)
4. [前后端联调](#4-前后端联调)

---

## 1. 数据库相关

### 1.1 MyBatis-Plus 实体类与数据库表结构不匹配

**问题描述**：
执行登录时报错 `java.sql.SQLSyntaxErrorException: Unknown column 'login_result' in 'field list'`

**原因分析**：
Java实体类字段与数据库表结构不一致，MyBatis-Plus根据实体类生成的SQL包含数据库中不存在的列。

**涉及表**：
- `iam_login_log` - 缺少 `login_result`、`fail_reason` 等字段
- `iam_user_role` - `id` 字段类型应为 `bigint AUTO_INCREMENT`，而非 `varchar`
- `iam_role_permission` - `id` 字段类型应为 `bigint AUTO_INCREMENT`
- `iam_permission` - 缺少 `group_name`、`description` 字段
- `iam_menu` - 缺少 `permission` 字段，`type` 字段类型不一致

**解决方案**：
1. 对比Java实体类（`@TableField`注解）与SQL表定义
2. 修改 `init.sql` 使表结构与实体类一致
3. 重新执行SQL初始化脚本

**关键代码示例**：
```java
// IamLoginLog.java 实体类
@TableField("login_result")
private String loginResult;

@TableField("fail_reason")
private String failReason;
```

```sql
-- init.sql 对应表结构
CREATE TABLE `iam_login_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `login_result` varchar(20) NOT NULL COMMENT '登录结果：success/fail',
    `fail_reason` varchar(256) DEFAULT NULL COMMENT '失败原因',
    -- ...
);
```

**经验总结**：
- 新建表时，先定义好实体类，再根据实体类生成SQL
- 使用 `@TableField` 注解明确指定数据库列名
- 关联表（如 `user_role`、`role_permission`）的 `id` 字段建议使用 `bigint AUTO_INCREMENT`

---

## 2. 认证授权相关

### 2.1 JWT密钥长度不足（HS512算法）

**问题描述**：
注册/登录时报错：
```
The signing key's size is 272 bits which is not secure enough for the HS512 algorithm.
The JWT JWA Specification (RFC 7518, Section 3.2) states that keys used with HS512 
MUST have a size >= 512 bits
```

**原因分析**：
HS512算法要求密钥长度至少为512位（64字节），原配置的密钥 `intellihub-iam-jwt-secret-key-2024` 只有272位。

**解决方案**：
修改 `application.yml` 中的JWT密钥，确保长度>=64字节：

```yaml
# 修改前（272位，不满足要求）
intellihub:
  auth:
    jwt:
      secret: intellihub-iam-jwt-secret-key-2024

# 修改后（>=512位）
intellihub:
  auth:
    jwt:
      # HS512算法需要至少512位(64字节)的密钥
      secret: intellihub-iam-jwt-secret-key-2024-this-is-a-very-long-secret-key-for-hs512-algorithm-security
```

**知识点**：
| 算法 | 最小密钥长度 |
|------|-------------|
| HS256 | 256位（32字节） |
| HS384 | 384位（48字节） |
| HS512 | 512位（64字节） |

**参考文档**：[RFC 7518 Section 3.2](https://tools.ietf.org/html/rfc7518#section-3.2)

### 2.2 BCrypt密码加密与验证

**知识点**：
- 前端发送明文密码
- 后端使用 `BCryptPasswordEncoder` 进行加密存储和验证
- `passwordEncoder.matches(rawPassword, encodedPassword)` 用于验证

**正确流程**：
```java
// 注册时加密存储
user.setPassword(passwordEncoder.encode(request.getPassword()));

// 登录时验证
if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
    throw new BusinessException(ResponseStatus.ACCOUNT_INCORRECT);
}
```

**注意事项**：
- 数据库中存储的是BCrypt哈希值，格式如：`$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi`
- 每次加密同一密码会产生不同的哈希值（因为包含随机盐）
- 验证时使用 `matches()` 方法，不能直接比较字符串

---

## 3. 网关相关

### 3.1 认证接口401 Unauthorized

**问题描述**：
前端调用 `/api/iam/v1/auth/captcha` 返回401，尽管该接口已在白名单中。

**原因分析**：
1. 网关日志配置的包名错误（`com.intellihub.gateway` vs `com.intellihub.gateway`）
2. 白名单路径匹配不完整

**解决方案**：

1. 修正日志包名：
```yaml
# application.yml
logging:
  level:
    com.intellihub.gateway: DEBUG  # 正确的包名
```

2. 完善白名单配置：
```java
// ReactiveAuthenticationFilter.java
private final List<String> whiteList = Arrays.asList(
    "/actuator/**",
    // 认证相关接口
    "/api/iam/v1/auth/login",
    "/api/iam/v1/auth/register",
    "/api/iam/v1/auth/captcha",
    "/api/iam/v1/auth/refresh",
    "/iam/v1/auth/login",
    "/iam/v1/auth/register",
    "/iam/v1/auth/captcha",
    "/iam/v1/auth/refresh",
    // ...
);
```

**注意事项**：
- 网关 `StripPrefix=1` 会去掉第一段路径，需要同时配置带 `/api` 和不带 `/api` 的路径
- 使用 `AntPathMatcher` 进行路径匹配，支持 `**` 通配符

### 3.2 Spring Security白名单配置

**配置位置**：`SecurityConfig.java`

```java
http.authorizeRequests()
    .antMatchers(
        "/iam/v1/auth/login",
        "/iam/v1/auth/register",
        "/iam/v1/auth/refresh",
        "/iam/v1/auth/captcha",
        "/iam/v1/auth/verify",
        "/iam/v1/auth/validate",
        "/actuator/**",
        "/error"
    ).permitAll()
    .anyRequest().authenticated();
```

---

## 4. 前后端联调

### 4.1 认证接口清单

| 接口路径 | 方法 | 说明 | 是否需要认证 |
|---------|------|------|-------------|
| `/iam/v1/auth/login` | POST | 用户登录 | 否 |
| `/iam/v1/auth/register` | POST | 用户注册 | 否 |
| `/iam/v1/auth/captcha` | GET | 获取验证码 | 否 |
| `/iam/v1/auth/refresh` | POST | 刷新Token | 否 |
| `/iam/v1/auth/logout` | POST | 退出登录 | 是 |
| `/iam/v1/auth/me` | GET | 获取当前用户 | 是 |
| `/iam/v1/auth/password` | POST | 修改密码 | 是 |
| `/iam/v1/auth/validate` | GET | Token验证（网关调用） | 否 |

### 4.2 前端API调用示例

```typescript
// api/auth.ts
export const authApi = {
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/iam/v1/auth/login', data)
  },
  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/iam/v1/auth/register', data)
  },
  getCaptcha(): Promise<ApiResponse<CaptchaResponse>> {
    return request.get('/iam/v1/auth/captcha')
  },
}
```

### 4.3 登录/注册请求参数

**登录请求**：
```json
{
  "username": "admin",
  "password": "admin123",
  "captchaKey": "xxx-xxx-xxx",
  "captcha": "abcd"
}
```

**注册请求**：
```json
{
  "username": "newuser",
  "password": "password123",
  "confirmPassword": "password123",
  "email": "user@example.com",
  "captchaKey": "xxx-xxx-xxx",
  "captcha": "abcd"
}
```

---

## 附录

### A. 常用调试命令

```bash
# 查看服务日志
tail -f logs/auth-service.log

# 测试登录接口
curl -X POST http://localhost:8081/iam/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 测试验证码接口
curl http://localhost:8081/iam/v1/auth/captcha
```

### B. 相关配置文件

- 网关配置：`intelli-gateway-service/src/main/resources/application.yml`
- 认证服务配置：`intelli-auth-iam-service/src/main/resources/application.yml`
- 数据库初始化：`intelli-auth-iam-service/src/main/resources/db/init.sql`
- 网关过滤器：`intelli-gateway-service/.../filter/ReactiveAuthenticationFilter.java`
- Security配置：`intelli-auth-iam-service/.../config/SecurityConfig.java`

---

## 5. 管理功能模块

### 5.1 后端管理接口

#### 租户管理 (`/iam/v1/tenants`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/iam/v1/tenants` | GET | 获取租户列表（分页） |
| `/iam/v1/tenants` | POST | 创建租户 |
| `/iam/v1/tenants/{id}` | GET | 获取租户详情 |
| `/iam/v1/tenants/{id}` | PUT | 更新租户 |
| `/iam/v1/tenants/{id}` | DELETE | 删除租户 |
| `/iam/v1/tenants/{id}/enable` | POST | 启用租户 |
| `/iam/v1/tenants/{id}/disable` | POST | 禁用租户 |
| `/iam/v1/tenants/{id}/quota` | GET | 获取租户配额 |
| `/iam/v1/tenants/{id}/quota` | PUT | 更新租户配额 |

#### 用户管理 (`/iam/v1/users`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/iam/v1/users` | GET | 获取用户列表（分页） |
| `/iam/v1/users` | POST | 创建用户 |
| `/iam/v1/users/{id}` | GET | 获取用户详情 |
| `/iam/v1/users/{id}` | PUT | 更新用户 |
| `/iam/v1/users/{id}` | DELETE | 删除用户 |
| `/iam/v1/users/{id}/enable` | POST | 启用用户 |
| `/iam/v1/users/{id}/disable` | POST | 禁用用户 |
| `/iam/v1/users/{id}/reset-password` | POST | 重置密码 |
| `/iam/v1/users/{id}/roles` | POST | 分配角色 |

#### 角色权限管理 (`/iam/v1/roles`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/iam/v1/roles` | GET | 获取角色列表 |
| `/iam/v1/roles` | POST | 创建角色 |
| `/iam/v1/roles/{id}` | PUT | 更新角色 |
| `/iam/v1/roles/{id}` | DELETE | 删除角色 |
| `/iam/v1/roles/{id}/permissions` | GET | 获取角色权限 |
| `/iam/v1/roles/{id}/permissions` | PUT | 更新角色权限 |
| `/iam/v1/permissions` | GET | 获取所有权限列表 |
| `/iam/v1/menus` | GET | 获取菜单树 |

#### API管理 (`/api/v1/apis`) - intelli-api-platform-service
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/apis` | GET | 获取API列表（分页） |
| `/api/v1/apis` | POST | 创建API |
| `/api/v1/apis/{id}` | GET | 获取API详情 |
| `/api/v1/apis/{id}` | PUT | 更新API |
| `/api/v1/apis/{id}` | DELETE | 删除API |
| `/api/v1/apis/{id}/publish` | POST | 发布API |
| `/api/v1/apis/{id}/offline` | POST | 下线API |
| `/api/v1/apis/{id}/deprecate` | POST | 废弃API |
| `/api/v1/apis/{id}/copy` | POST | 复制API |

#### API分组管理 (`/api/v1/api-groups`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/api-groups` | GET | 获取分组列表 |
| `/api/v1/api-groups` | POST | 创建分组 |
| `/api/v1/api-groups/{id}` | GET | 获取分组详情 |
| `/api/v1/api-groups/{id}` | PUT | 更新分组 |
| `/api/v1/api-groups/{id}` | DELETE | 删除分组 |

### 5.2 前端页面结构

```
src/
├── api/
│   ├── auth.ts          # 认证API
│   ├── tenant.ts        # 租户管理API
│   ├── user.ts          # 用户管理API
│   └── role.ts          # 角色权限API
├── views/
│   ├── console/
│   │   ├── tenant/
│   │   │   └── TenantListPage.vue    # 租户列表
│   │   └── user/
│   │       ├── UserListPage.vue       # 用户列表
│   │       └── RolePermissionPage.vue # 角色权限
│   └── user/
│       └── ProfilePage.vue            # 个人中心
└── router/
    └── index.ts         # 路由配置
```

### 5.3 路由配置

| 路由 | 组件 | 说明 |
|------|------|------|
| `/console/tenant/list` | TenantListPage | 租户列表 |
| `/console/users/list` | UserListPage | 用户列表 |
| `/console/users/roles` | RolePermissionPage | 角色权限 |
| `/profile` | ProfilePage | 个人中心 |

### 5.4 认证架构说明

**认证流程**：
```
前端请求 → 网关(ReactiveAuthenticationFilter) → 认证服务(/auth/validate) → 验证Token
                    ↓
              验证通过后，网关将用户信息放入请求头
                    ↓
              下游服务（认证服务的管理接口）
```

**关键配置**：
- 网关负责统一认证，调用认证服务的`/iam/v1/auth/validate`接口验证Token
- 认证服务的管理接口（tenants/users/roles等）在Security中放开，因为网关已做认证
- 用户信息通过请求头传递：`X-User-Id`、`X-Username`、`X-Tenant-Id`等

---

## 6. 常见问题与解决方案

### 6.1 前端登录状态丢失

**问题**：刷新页面或进入其他页面后需要重新登录

**原因**：Pinia store的`user`状态初始化为`null`，`isAuthenticated`计算属性需要`token`和`user`都存在

**解决方案**：
在store定义外部添加`getStoredUser()`函数，store初始化时直接从localStorage恢复用户信息：

```typescript
// 从localStorage恢复用户信息
const getStoredUser = (): User | null => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      return JSON.parse(storedUser)
    } catch {
      return null
    }
  }
  return null
}

// Store初始化时直接恢复
const user = ref<User | null>(getStoredUser())
```

### 6.2 管理接口返回401

**问题**：前端调用租户/用户/角色管理接口返回401 Unauthorized

**原因**：认证服务的Spring Security配置拦截了管理接口

**解决方案**：
在`SecurityConfig.java`中放开管理接口，因为网关已经做了认证：

```java
.antMatchers(
    "/iam/v1/auth/**",
    // 管理接口（网关已做认证，这里放开）
    "/iam/v1/tenants/**",
    "/iam/v1/users/**",
    "/iam/v1/roles/**",
    "/iam/v1/permissions/**",
    "/iam/v1/menus/**",
    "/actuator/**",
    "/error"
).permitAll()
```

### 6.3 请求缺少X-Tenant-Id头

**问题**：后端管理接口需要`X-Tenant-Id`请求头

**解决方案**：
在axios请求拦截器中自动添加：

```typescript
// 添加租户ID到请求头
const storedUser = localStorage.getItem('user')
if (storedUser) {
  const user = JSON.parse(storedUser)
  if (user.tenantId) {
    config.headers['X-Tenant-Id'] = user.tenantId
  }
}
```

---

*文档最后更新：2024年12月*
