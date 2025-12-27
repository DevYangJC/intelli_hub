# IntelliHub API 下发指南

本文档详细说明如何在 IntelliHub 平台创建和发布一个开放API，以 **auth服务的验证码接口** 为例。

---

## 一、概念说明

### 什么是"下发API"？

将内部服务的接口通过网关暴露给外部调用方，实现：
- 统一入口：所有API通过网关访问
- 安全控制：认证、限流、IP白名单
- 路径转换：对外路径与内部路径解耦

```
外部调用方                    网关                        内部服务
    │                         │                            │
    │  GET /open/auth/captcha │                            │
    │ ───────────────────────→│                            │
    │                         │  GET /iam/v1/auth/captcha  │
    │                         │ ──────────────────────────→│
    │                         │                            │
    │       验证码图片         │       验证码图片            │
    │ ←───────────────────────│ ←──────────────────────────│
```

---

## 二、创建API字段详解

### 📋 1. 基本信息

| 字段 | 必填 | 说明 | 验证码示例 |
|------|------|------|-----------|
| **API名称** | ✅ | 接口的中文名称，用于展示和搜索 | `获取验证码` |
| **API编码** | ✅ | 唯一标识符，创建后不可修改。只能包含小写字母、数字和连字符，以字母开头 | `get-captcha` |
| **API分组** | ✅ | 将API归类到某个分组，便于管理 | 选择"认证服务"或新建 |
| **版本号** | ✅ | API版本，用于版本管理 | `v1` |
| **API描述** | ❌ | 详细描述API功能，会展示在API文档中 | `获取图形验证码，用于登录时人机验证` |
| **标签** | ❌ | 多个标签，用于筛选和分类 | `认证相关`、`公开接口` |

---

### 🔧 2. 后端服务类型

选择API后端的实现方式：

| 类型 | 说明 | 适用场景 |
|------|------|----------|
| **HTTP/HTTPS** | 转发到HTTP服务 | 最常用，调用Spring Cloud微服务 |
| **Dubbo** | 调用Dubbo RPC服务 | 后端是Dubbo服务时使用 |
| **内部API** | 关联平台已有的API | API编排、组合接口 |
| **Mock** | 返回模拟数据 | 开发调试、前端联调 |

#### 验证码接口选择：`HTTP/HTTPS`

---

### 🌐 3. HTTP后端配置（选择HTTP时显示）

| 字段 | 必填 | 说明 | 验证码示例 |
|------|------|------|-----------|
| **协议** | ✅ | HTTP或HTTPS | `HTTP` |
| **后端地址** | ✅ | 后端服务地址。可以是：<br>- Nacos服务名：`http://intelli-auth-iam-service`<br>- IP地址：`http://192.168.1.100:8081`<br>- 域名：`https://api.example.com` | `http://intelli-auth-iam-service` |
| **后端路径** | ✅ | 后端服务的实际接口路径 | `/iam/v1/auth/captcha` |
| **请求方式** | ✅ | 调用后端使用的HTTP方法（默认自动同步请求配置） | `GET` |

---

### 📡 4. 请求配置

定义**对外暴露**的接口规范：

| 字段 | 必填 | 说明 | 验证码示例 |
|------|------|------|-----------|
| **请求方式** | ✅ | 外部调用方使用的HTTP方法 | `GET` |
| **请求路径** | ✅ | 对外暴露的API路径，建议以`/open/`开头 | `/open/auth/captcha` |
| **Content-Type** | ✅ | 请求体格式 | `application/json` |

> **路径设计建议**：
> - 公开接口：`/open/xxx`
> - 需要认证：`/api/xxx`
> - 支持路径参数：`/open/users/{userId}`

---

### 📝 5. 请求参数（可选）

定义接口的入参，用于生成API文档：

| 参数位置 | 说明 | 示例 |
|----------|------|------|
| **Query参数** | URL查询参数 `?key=value` | `?type=image` |
| **Path参数** | 路径参数 `/users/{id}` | `id` |
| **Header参数** | 请求头参数 | `X-Token` |
| **Body参数** | 请求体参数（POST/PUT时） | `{"username": "xxx"}` |

**验证码接口**：无需参数，跳过此部分

---

### 📄 6. 响应配置（可选）

定义接口的返回格式，用于生成API文档：

| 字段 | 说明 | 验证码示例 |
|------|------|-----------|
| **成功响应示例** | 请求成功时的返回JSON | 见下方 |
| **错误响应示例** | 请求失败时的返回JSON | 见下方 |

```json
// 成功响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "abc123",
    "captchaImage": "data:image/png;base64,..."
  }
}

// 错误响应示例
{
  "code": 500,
  "message": "验证码生成失败",
  "data": null
}
```

---

### ⚙️ 7. 高级配置（可选）

| 字段 | 默认值 | 说明 | 验证码示例 |
|------|--------|------|-----------|
| **超时时间** | 5000ms | 后端调用超时时间，超时返回504 | `5000` |
| **重试次数** | 0 | 失败后重试次数，0表示不重试 | `0` |

---

### 🔒 8. 安全配置

#### 认证方式

| 选项 | 说明 | 适用场景 |
|------|------|----------|
| **无需认证** | 任何人都可以调用 | 公开接口：验证码、公告 |
| **签名认证** | 需要AppKey+签名 | 开放给第三方的接口 |
| **JWT Token** | 需要登录Token | 用户登录后的接口 |

**验证码接口**：选择 `无需认证`（因为是登录前调用）

#### 限流策略

| 字段 | 说明 | 验证码示例 |
|------|------|-----------|
| **开启限流** | 是否启用限流 | ✅ 开启 |
| **限流值** | 单位时间内允许的请求数 | `100` |
| **限流单位** | 秒/分钟/小时 | `秒` |

> **建议**：验证码接口开启限流，防止恶意刷取

#### IP白名单

| 字段 | 说明 | 验证码示例 |
|------|------|-----------|
| **开启白名单** | 是否限制调用IP | ❌ 不开启 |
| **IP列表** | 允许访问的IP，支持CIDR | - |

---

## 三、完整填写示例：验证码接口

### 表单填写

```yaml
# 基本信息
API名称: 获取验证码
API编码: get-captcha
API分组: 认证服务
版本号: v1
API描述: 获取图形验证码，用于登录时人机验证
标签: [认证相关, 公开接口]

# 后端服务类型
服务类型: HTTP/HTTPS

# HTTP后端配置
协议: HTTP
后端地址: http://intelli-auth-iam-service
后端路径: /iam/v1/auth/captcha
请求方式: GET (自动同步)

# 请求配置
请求方式: GET
请求路径: /open/auth/captcha
Content-Type: application/json

# 请求参数
(无)

# 响应配置
(可选填写)

# 高级配置
超时时间: 5000毫秒
重试次数: 0

# 安全配置
认证方式: ☑ 无需认证
限流策略: ☑ 开启, 100次/秒
IP白名单: ☐ 不开启
```

### 创建后测试

```bash
# 直接调用（无需认证）
curl http://localhost:8080/open/auth/captcha

# 预期返回
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "xxx",
    "captchaImage": "data:image/png;base64,..."
  }
}
```

---

## 四、API生命周期

```
创建 → 草稿 → 发布 → 已发布 → 下线 → 已下线
              ↑                    │
              └────────────────────┘
                    可重新发布
```

| 状态 | 说明 | 可执行操作 |
|------|------|-----------|
| **草稿** | 刚创建，未发布 | 编辑、删除、发布 |
| **已发布** | 正常对外服务 | 编辑、下线 |
| **已下线** | 暂停服务 | 编辑、发布、删除 |
| **已废弃** | 即将删除 | 删除 |

---

## 五、常见问题

### Q1: 后端地址填IP还是服务名？

**推荐使用Nacos服务名**：
- ✅ `http://intelli-auth-iam-service` - 自动负载均衡
- ❌ `http://192.168.1.100:8081` - 单点，不推荐

### Q2: 请求路径和后端路径有什么区别？

| 路径 | 作用 | 示例 |
|------|------|------|
| 请求路径 | 对外暴露，调用方使用 | `/open/auth/captcha` |
| 后端路径 | 内部服务实际路径 | `/iam/v1/auth/captcha` |

### Q3: 认证方式怎么选？

| 场景 | 认证方式 |
|------|----------|
| 登录前的接口（验证码、注册） | 无需认证 |
| 开放给第三方系统调用 | 签名认证（AppKey） |
| 用户登录后的接口 | JWT Token |

### Q4: 限流怎么设置合理？

| 接口类型 | 建议QPS |
|----------|---------|
| 验证码 | 50-100/秒 |
| 登录 | 10-20/秒 |
| 普通查询 | 100-500/秒 |
| 数据导出 | 1-5/秒 |

---

## 六、Dubbo接口配置示例：获取用户信息

以 **AuthDubboService.getUserById** 接口为例，演示如何将Dubbo接口暴露为HTTP API。

### Dubbo接口信息

```java
// 接口定义位置：intelli-common/src/main/java/com/intellihub/common/dubbo/AuthDubboService.java
package com.intellihub.common.dubbo;

public interface AuthDubboService {
    /**
     * 根据用户ID获取用户信息
     */
    UserInfoDTO getUserById(String userId);
}

// 实现类注解
@DubboService(version = "1.0.0", group = "intellihub")
public class AuthDubboServiceImpl implements AuthDubboService { ... }
```

### 表单填写

```yaml
# 基本信息
API名称: 获取用户信息
API编码: get-user-by-id
API分组: 用户服务
版本号: v1
API描述: 根据用户ID获取用户详细信息（Dubbo接口）
标签: [用户管理, Dubbo接口]

# 后端服务类型
服务类型: Dubbo   # ← 选择Dubbo

# Dubbo后端配置
接口全路径: com.intellihub.common.dubbo.AuthDubboService
方法名: getUserById
版本: 1.0.0
分组: intellihub
参数类型: java.lang.String   # 方法参数类型

# 请求配置
请求方式: GET
请求路径: /open/user/{userId}
Content-Type: application/json

# 请求参数
参数名称: userId
参数位置: Path
参数类型: String
是否必填: 是
参数说明: 用户ID

# 安全配置
认证方式: ☑ 签名认证 (AppKey)   # 用户信息需要认证
限流策略: ☑ 开启, 50次/秒
```

### Dubbo配置字段说明

| 字段 | 必填 | 说明 | 示例 |
|------|------|------|------|
| **接口全路径** | ✅ | Dubbo接口的完整包名+类名 | `com.intellihub.common.dubbo.AuthDubboService` |
| **方法名** | ✅ | 要调用的方法名称 | `getUserById` |
| **版本** | ❌ | Dubbo服务版本，对应`@DubboService(version=...)` | `1.0.0` |
| **分组** | ❌ | Dubbo服务分组，对应`@DubboService(group=...)` | `intellihub` |
| **参数类型** | ✅ | 方法参数的Java类型，多个用逗号分隔 | `java.lang.String` |

### 调用测试

```bash
# 1. 生成签名（需要AppKey和AppSecret）
TIMESTAMP=$(date +%s%3N)
NONCE=$(uuidgen | tr -d '-')
APP_KEY="your-app-key"
APP_SECRET="your-app-secret"
METHOD="GET"
PATH="/open/user/123456"

# 2. 计算签名
STRING_TO_SIGN="${METHOD}\n${PATH}\n${TIMESTAMP}\n${NONCE}"
SIGNATURE=$(echo -n "$STRING_TO_SIGN" | openssl dgst -sha256 -hmac "$APP_SECRET" -binary | base64)

# 3. 发起请求
curl -X GET "http://localhost:8080/open/user/123456" \
  -H "X-App-Key: ${APP_KEY}" \
  -H "X-Timestamp: ${TIMESTAMP}" \
  -H "X-Nonce: ${NONCE}" \
  -H "X-Signature: ${SIGNATURE}"

# 预期返回
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "123456",
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "roles": ["admin"],
    "permissions": ["user:read", "user:write"]
  }
}
```

### 多参数Dubbo方法示例

如果Dubbo方法有多个参数：

```java
// 接口方法
UserInfoDTO getUserByCondition(String tenantId, String username, Integer status);
```

配置时参数类型用逗号分隔：

```yaml
参数类型: java.lang.String,java.lang.String,java.lang.Integer
```

请求参数对应配置：

| 参数名 | 位置 | 类型 | 说明 |
|--------|------|------|------|
| tenantId | Query | String | 租户ID |
| username | Query | String | 用户名 |
| status | Query | Integer | 状态 |

调用示例：
```bash
curl "http://localhost:8080/open/user/query?tenantId=1&username=admin&status=1"
```

---

## 七、现有Dubbo接口清单

| 服务 | 接口 | 方法 | 说明 |
|------|------|------|------|
| **AuthDubboService** | `com.intellihub.common.dubbo.AuthDubboService` | `validateToken(String)` | 验证Token |
| | | `getUserById(String)` | 根据ID获取用户 |
| | | `getUserByUsername(String)` | 根据用户名获取用户 |
| **TenantDubboService** | `com.intellihub.common.dubbo.TenantDubboService` | - | 租户服务 |
| **ApiPlatformDubboService** | `com.intellihub.common.dubbo.ApiPlatformDubboService` | - | API平台服务 |
| **AppCenterDubboService** | `com.intellihub.common.dubbo.AppCenterDubboService` | - | 应用中心服务 |

> **注意**：Dubbo版本统一为 `1.0.0`，分组统一为 `intellihub`

---

## 八、下一步

1. **创建应用**：控制台 → 应用管理 → 创建应用
2. **获取密钥**：应用详情 → 密钥管理 → 获取AppKey/AppSecret
3. **订阅API**：应用详情 → API授权 → 勾选需要的API
4. **调用测试**：使用AppKey+签名调用（如果是签名认证）
