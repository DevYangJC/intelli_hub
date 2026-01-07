# IntelliHub API 网关技术实现文档

## 目录

1. [服务职责](#服务职责)
2. [整体架构](#整体架构)
3. [核心组件](#核心组件)
4. [过滤器链](#过滤器链)
5. [认证机制](#认证机制)
6. [设计模式](#设计模式)
7. [路由转发](#路由转发)
8. [限流机制](#限流机制)
9. [配置说明](#配置说明)
10. [常见问题](#常见问题)

---

## 服务职责

API 网关是 IntelliHub 平台的**统一入口**，负责所有外部请求的接入、认证、路由和转发。

### 核心职责

| 职责 | 说明 |
|------|------|
| **统一入口** | 所有请求通过网关进入，隐藏内部服务拓扑 |
| **身份认证** | JWT Token 认证（管理后台）、AppKey 签名认证（开放 API） |
| **动态路由** | 静态路由（配置文件）+ 动态路由（API 平台管理） |
| **限流保护** | 多维度限流（IP、路径、组合），防止资源耗尽 |
| **协议转换** | HTTP → HTTP、HTTP → Dubbo 泛化调用 |
| **访问日志** | 记录请求日志，支持调用链追踪 |
| **租户隔离** | 提取租户信息，传递给下游服务 |

---

## 整体架构

### 系统架构图

```mermaid
graph TB
    subgraph "外部请求"
        Browser[浏览器<br/>管理后台]
        ThirdParty[第三方系统<br/>开放API]
    end
    
    subgraph "API 网关 intelli-gateway-service"
        direction TB
        
        subgraph "过滤器链 Filter Chain"
            F1[CacheBodyFilter<br/>缓存请求体]
            F2[AccessLogFilter<br/>访问日志]
            F3[OpenApiRouteMatchFilter<br/>API路由匹配]
            F4[RateLimitFilter<br/>限流控制]
            F5[GlobalTenantFilter<br/>租户上下文]
            F6[JwtAuthenticationFilter<br/>JWT认证]
            F7[AppKeyAuthenticationFilter<br/>AppKey认证]
            F8[OpenApiRouteFilter<br/>动态路由转发]
        end
        
        subgraph "服务层 Services"
            RouteService[OpenApiRouteService<br/>路由配置服务]
            AppKeyService[AppKeyService<br/>应用密钥服务]
            RateLimitService[RateLimitService<br/>限流服务]
            DubboService[DubboGenericService<br/>Dubbo泛化调用]
        end
    end
    
    subgraph "后端服务"
        AuthService[认证服务<br/>intelli-auth-iam-service]
        ApiPlatform[API平台服务<br/>intelli-api-platform-service]
        EventService[事件服务<br/>intelli-event-service]
        AigcService[AIGC服务<br/>intelli-aigc-service]
        OtherService[其他服务...]
    end
    
    subgraph "基础设施"
        Redis[(Redis<br/>限流/缓存)]
        Nacos[Nacos<br/>服务注册]
    end
    
    Browser -->|JWT Token| F1
    ThirdParty -->|AppKey签名| F1
    
    F1 --> F2 --> F3 --> F4 --> F5 --> F6 --> F7 --> F8
    
    F6 -.->|验证Token| Redis
    F7 -.->|验证AppKey| AppKeyService
    F4 -.->|限流计数| RateLimitService
    F3 -.->|匹配路由| RouteService
    
    RouteService -.->|Dubbo| ApiPlatform
    AppKeyService -.->|Dubbo| ApiPlatform
    
    F8 -->|HTTP| AuthService
    F8 -->|HTTP| ApiPlatform
    F8 -->|HTTP| EventService
    F8 -->|HTTP| AigcService
    F8 -->|Dubbo泛化| OtherService
    
    RateLimitService --> Redis
```

### 技术栈

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| 网关框架 | Spring Cloud Gateway | 响应式网关，高性能 |
| 注册中心 | Nacos | 服务发现、配置中心 |
| RPC 框架 | Dubbo | 泛化调用后端服务 |
| 缓存 | Redis | 限流计数、Nonce 缓存 |
| 负载均衡 | Spring Cloud LoadBalancer | 服务负载均衡 |

---

## 核心组件

### 组件关系图

```mermaid
classDiagram
    class GlobalFilter {
        <<interface>>
        +filter(exchange, chain) Mono~Void~
        +getOrder() int
    }
    
    class CacheBodyFilter {
        -order: -200
        +缓存请求Body供后续使用
    }
    
    class AccessLogFilter {
        -order: -100
        +记录请求开始时间
        +记录访问日志
    }
    
    class RateLimitFilter {
        -order: 100
        -rateLimitService
        +多维度限流检查
    }
    
    class JwtAuthenticationFilter {
        -order: 1000
        -jwtUtil
        +JWT Token本地验证
        +用户信息传递
    }
    
    class AppKeyAuthenticationFilter {
        -order: 1100
        -appKeyService
        +AppKey有效性验证
        +HMAC签名验证
        +防重放攻击
    }
    
    class OpenApiRouteFilter {
        -order: 1200
        -routeService
        -dubboGenericService
        +HTTP后端转发
        +Dubbo泛化调用
        +Mock响应
    }
    
    GlobalFilter <|.. CacheBodyFilter
    GlobalFilter <|.. AccessLogFilter
    GlobalFilter <|.. RateLimitFilter
    GlobalFilter <|.. JwtAuthenticationFilter
    GlobalFilter <|.. AppKeyAuthenticationFilter
    GlobalFilter <|.. OpenApiRouteFilter
```

### 服务组件

| 组件 | 职责 | 依赖 |
|------|------|------|
| `OpenApiRouteService` | 加载/缓存 API 路由配置 | Dubbo (ApiPlatformDubboService) |
| `AppKeyService` | 验证 AppKey、检查订阅关系 | Dubbo (ApiPlatformDubboService) |
| `RateLimitService` | 限流计数、窗口控制 | Redis |
| `DubboGenericService` | Dubbo 泛化调用 | Dubbo Registry |

---

## 过滤器链

### 执行顺序

```mermaid
flowchart LR
    A[请求进入] --> B[CacheBodyFilter<br/>-200]
    B --> C[AccessLogFilter<br/>-100]
    C --> D[OpenApiRouteMatchFilter<br/>-50]
    D --> E[RateLimitFilter<br/>100]
    E --> F[GlobalTenantFilter<br/>500]
    F --> G[JwtAuthenticationFilter<br/>1000]
    G --> H[AppKeyAuthenticationFilter<br/>1100]
    H --> I[OpenApiRouteFilter<br/>1200]
    I --> J[后端服务]
```

### 过滤器详解

#### 1. CacheBodyFilter (order: -200)

**职责**：缓存请求 Body，供后续过滤器读取

```java
// 将Body缓存到exchange属性
exchange.getAttributes().put(ATTR_CACHED_BODY, bodyString);
```

#### 2. AccessLogFilter (order: -100)

**职责**：记录访问日志，包含请求开始时间、耗时等

#### 3. OpenApiRouteMatchFilter (order: -50)

**职责**：匹配开放 API 路由，将路由配置存入 exchange 属性

```java
// 设置路由属性供后续过滤器使用
exchange.getAttributes().put(ATTR_API_ROUTE, route);
exchange.getAttributes().put(ATTR_API_ID, route.getApiId());
exchange.getAttributes().put(ATTR_IS_OPEN_API, true);
```

#### 4. RateLimitFilter (order: 100)

**职责**：多维度限流控制

- IP 级别限流（宽松）
- 路径级别限流
- IP+路径组合限流（严格）

#### 5. GlobalTenantFilter (order: 500)

**职责**：从请求头提取租户信息，设置租户上下文

#### 6. JwtAuthenticationFilter (order: 1000)

**职责**：验证 JWT Token（管理后台请求）

- 白名单检查
- Token 本地验证（无需调用 Auth 服务）
- 用户信息传递（X-User-Id, X-Username, X-Tenant-Id, X-User-Roles）

#### 7. AppKeyAuthenticationFilter (order: 1100)

**职责**：验证 AppKey 签名（开放 API 请求）

- AppKey 有效性验证
- HMAC-SHA256 签名验证
- 时间戳 + Nonce 防重放
- 订阅关系检查

#### 8. OpenApiRouteFilter (order: 1200)

**职责**：动态路由转发

- HTTP 后端转发（LoadBalancer 解析服务名）
- Dubbo 泛化调用
- Mock 响应返回

---

## 认证机制

### 双认证体系

```mermaid
flowchart TD
    A[请求进入] --> B{请求类型}
    
    B -->|管理后台请求<br/>/api/**| C[JWT 认证]
    B -->|开放API请求<br/>/open/**| D[AppKey 认证]
    
    C --> C1{在白名单?}
    C1 -->|是| C2[跳过认证]
    C1 -->|否| C3[验证 JWT Token]
    C3 --> C4{Token 有效?}
    C4 -->|是| C5[提取用户信息<br/>传递给下游]
    C4 -->|否| C6[返回 401]
    
    D --> D1{API认证类型?}
    D1 -->|none| D2[跳过认证]
    D1 -->|signature| D3[验证签名]
    D3 --> D4[验证 AppKey]
    D4 --> D5[验证时间戳]
    D5 --> D6[验证 Nonce]
    D6 --> D7[验证 HMAC 签名]
    D7 --> D8[检查订阅关系]
    D8 --> D9{全部通过?}
    D9 -->|是| D10[传递应用信息]
    D9 -->|否| D11[返回 401/403]
```

### JWT 认证

**适用场景**：管理后台请求（/api/**）

**认证流程**：
1. 检查白名单（跳过）
2. 获取 `Authorization: Bearer <token>`
3. 本地验证 JWT（无需调用 Auth 服务）
4. 提取用户信息，添加到请求头

**传递的请求头**：
- `X-User-Id` - 用户ID
- `X-Username` - 用户名
- `X-Tenant-Id` - 租户ID
- `X-User-Roles` - 角色列表

### AppKey 认证

**适用场景**：开放 API 请求（/open/**, /external/**）

**认证流程**：
1. 检查 API 认证类型（none 跳过）
2. 验证必要请求头
3. 验证时间戳（防过期）
4. 验证 Nonce（防重放）
5. 验证 HMAC-SHA256 签名
6. 检查应用订阅关系

**请求头要求**：
- `X-App-Key` - 应用 Key
- `X-Timestamp` - 时间戳（秒级）
- `X-Nonce` - 随机字符串
- `X-Signature` - HMAC-SHA256 签名

**签名算法**：
```
签名字符串 = Method + Path + Timestamp + Nonce
签名 = HMAC-SHA256(签名字符串, AppSecret)
```

---

## 设计模式

网关在处理不同类型的外部接口时，采用了多种设计模式来提高代码的可扩展性和可维护性。

### 设计模式总览

```mermaid
flowchart TB
    subgraph "建造者模式 Builder"
        B1[DubboInvocationContextBuilder]
        B2[创建 DubboInvocationContext]
        B1 --> B2
    end
    
    subgraph "责任链模式 Chain of Responsibility"
        C1[PathParameterExtractor]
        C2[QueryParameterExtractor]
        C3[BodyParameterExtractor]
        C1 --> C2 --> C3
    end
    
    subgraph "策略模式 Strategy"
        S1[InvocationStrategy]
        S2[NoArgInvocationStrategy]
        S3[SingleArgInvocationStrategy]
        S4[MultiArgInvocationStrategy]
        S1 --> S2
        S1 --> S3
        S1 --> S4
    end
    
    B2 -->|构建上下文| C1
    C3 -->|提取参数后| S1
```

### 1. 建造者模式 (Builder Pattern)

**应用场景**：构建 Dubbo 调用上下文

**核心类**：`DubboInvocationContextBuilder`

```java
/**
 * Dubbo调用上下文建造者
 * 负责构建完整的 DubboInvocationContext
 */
@Component
public class DubboInvocationContextBuilder {
    
    private final List<ParameterExtractor> extractors;
    
    public DubboInvocationContext build(ServerWebExchange exchange, ApiRouteDTO route) {
        // 1. 创建上下文对象，设置基础信息
        DubboInvocationContext context = DubboInvocationContext.builder()
                .route(route)
                .originalPath(originalPath)
                .httpMethod(exchange.getRequest().getMethod().name())
                .build();
        
        // 2. 执行参数提取器链
        for (ParameterExtractor extractor : extractors) {
            if (extractor.supports(exchange, context)) {
                extractor.extract(exchange, context);
            }
        }
        
        return context;
    }
}
```

**优点**：
- 将复杂对象的构建过程封装
- 支持分步骤构建
- 便于扩展新的构建逻辑

---

### 2. 责任链模式 (Chain of Responsibility Pattern)

**应用场景**：从 HTTP 请求中提取 Dubbo 调用参数

**核心接口**：`ParameterExtractor`

```java
/**
 * 参数提取器接口
 * 不同实现类负责从不同来源提取参数
 */
public interface ParameterExtractor {
    int getOrder();  // 执行顺序
    void extract(ServerWebExchange exchange, DubboInvocationContext context);
    default boolean supports(ServerWebExchange exchange, DubboInvocationContext context) {
        return true;
    }
}
```

**实现类**：

| 提取器 | 顺序 | 职责 |
|--------|------|------|
| `PathParameterExtractor` | 100 | 从 URL 路径提取参数（如 `/user/{id}`） |
| `QueryParameterExtractor` | 200 | 从 Query String 提取参数（如 `?name=xxx`） |
| `BodyParameterExtractor` | 300 | 从请求 Body 提取参数（JSON） |

**执行流程**：

```mermaid
flowchart LR
    A[HTTP请求] --> B[PathParameterExtractor<br/>提取路径参数]
    B --> C[QueryParameterExtractor<br/>提取Query参数]
    C --> D[BodyParameterExtractor<br/>提取Body参数]
    D --> E[DubboInvocationContext<br/>参数完整]
```

**优点**：
- 解耦参数提取逻辑
- 新增参数来源只需添加新的提取器
- 提取器顺序可配置

---

### 3. 策略模式 (Strategy Pattern)

**应用场景**：根据参数数量选择不同的 Dubbo 调用方式

**核心接口**：`InvocationStrategy`

```java
/**
 * Dubbo调用策略接口
 * 定义不同参数数量场景下的泛化调用策略
 */
public interface InvocationStrategy {
    boolean supports(DubboInvocationContext context);
    Object invoke(GenericService genericService, DubboInvocationContext context);
    String getStrategyName();
}
```

**实现类**：

| 策略 | 适用场景 | 示例 |
|------|----------|------|
| `NoArgInvocationStrategy` | 无参数方法 | `listAll()` |
| `SingleArgInvocationStrategy` | 单参数方法 | `getById(String id)` |
| `MultiArgInvocationStrategy` | 多参数方法 | `query(String name, Integer page)` |

**策略选择流程**：

```mermaid
flowchart TD
    A[DubboGenericService.invoke] --> B{选择策略}
    B --> C{paramCount == 0?}
    C -->|是| D[NoArgInvocationStrategy]
    C -->|否| E{paramCount == 1?}
    E -->|是| F[SingleArgInvocationStrategy]
    E -->|否| G[MultiArgInvocationStrategy]
    
    D --> H[执行泛化调用]
    F --> H
    G --> H
```

**代码示例（策略选择）**：

```java
@Service
public class DubboGenericService {
    
    private final List<InvocationStrategy> strategies;
    
    public Mono<Object> invoke(DubboInvocationContext context) {
        // 选择合适的调用策略
        InvocationStrategy strategy = selectStrategy(context);
        return strategy.invoke(genericService, context);
    }
    
    private InvocationStrategy selectStrategy(DubboInvocationContext context) {
        for (InvocationStrategy strategy : strategies) {
            if (strategy.supports(context)) {
                return strategy;
            }
        }
        throw new IllegalStateException("没有找到合适的调用策略");
    }
}
```

**优点**：
- 不同调用方式解耦
- 新增调用策略无需修改现有代码
- 便于单元测试

---

### 4. 组合使用示例

**完整调用流程**：

```mermaid
sequenceDiagram
    participant Client as 外部请求
    participant Filter as OpenApiRouteFilter
    participant Builder as ContextBuilder
    participant Extractors as 提取器链
    participant Service as DubboGenericService
    participant Strategy as 调用策略
    participant Backend as 后端服务
    
    Client->>Filter: HTTP请求
    Filter->>Builder: build(exchange, route)
    
    Note over Builder,Extractors: 建造者模式 + 责任链模式
    Builder->>Extractors: 执行提取器链
    Extractors-->>Builder: 返回完整Context
    Builder-->>Filter: DubboInvocationContext
    
    Filter->>Service: invoke(context)
    
    Note over Service,Strategy: 策略模式
    Service->>Strategy: selectStrategy(context)
    Strategy->>Backend: genericService.$invoke()
    Backend-->>Strategy: 调用结果
    Strategy-->>Service: 返回结果
    Service-->>Filter: Mono<Object>
    Filter-->>Client: HTTP响应
```

---

## 路由转发

### 路由类型

```mermaid
flowchart TD
    A[请求] --> B{路由类型}
    
    B -->|静态路由| C[application.yml 配置]
    B -->|动态路由| D[API 平台管理]
    
    C --> C1["/api/auth/** → auth-service"]
    C --> C2["/api/event/** → event-service"]
    C --> C3["/api/aigc/** → aigc-service"]
    
    D --> D1{后端类型}
    D1 -->|HTTP| D2[WebClient 转发]
    D1 -->|Dubbo| D3[泛化调用]
    D1 -->|Mock| D4[返回Mock数据]
    
    D2 --> E[LoadBalancer<br/>解析服务名]
    D3 --> F[DubboGenericService<br/>调用]
```

### 静态路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://intelli-auth-iam-service
          predicates:
            - Path=/api/auth/**,/api/iam/**
          filters:
            - StripPrefix=1
            
        - id: event-service
          uri: lb://intelli-event-service
          predicates:
            - Path=/api/event/**
          filters:
            - StripPrefix=1
```

### 动态路由

由 `OpenApiRouteService` 从 API 平台服务加载：

- 启动时加载所有已发布路由
- 支持 Redis Pub/Sub 实时刷新
- 本地缓存 + Dubbo 懒加载

---

## 限流机制

### 限流架构

```mermaid
flowchart TD
    A[RateLimitFilter] --> B[获取限流配置]
    B --> C[构建限流Key]
    
    C --> D1[IP Key<br/>ip:127.0.0.1]
    C --> D2[Path Key<br/>path:/api/xxx]
    C --> D3[Combined Key<br/>combined:ip:path]
    
    D1 --> E1[RateLimitService.isAllowed<br/>requests*2]
    D2 --> E2[RateLimitService.isAllowed<br/>requests*10]
    D3 --> E3[RateLimitService.isAllowed<br/>requests]
    
    E1 --> F{全部通过?}
    E2 --> F
    E3 --> F
    
    F -->|是| G[继续执行]
    F -->|否| H[返回 429]
```

### 限流配置

```yaml
intellihub:
  gateway:
    rate-limit:
      enabled: true
      
      # 默认限流（每分钟100次）
      default-limit:
        requests: 100
        window: 60
      
      # 特定路径限流
      limits:
        "/api/auth/**":
          requests: 5
          window: 60
        "/api/search/**":
          requests: 200
          window: 60
```

### 限流算法

使用 Redis 实现固定窗口限流：
- `INCR` 计数
- `EXPIRE` 设置窗口过期
- 每次检查 TTL，丢失时重新设置

---

## 配置说明

### 核心配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 网关端口 | 8080 |
| `intellihub.gateway.auth.enabled` | 是否启用 JWT 认证 | true |
| `intellihub.gateway.auth.secret` | JWT 密钥 | - |
| `intellihub.gateway.rate-limit.enabled` | 是否启用限流 | true |
| `gateway.appkey.enabled` | 是否启用 AppKey 认证 | true |
| `gateway.appkey.timestamp-tolerance` | 时间戳容差（秒） | 300 |

### 白名单配置

```yaml
intellihub:
  gateway:
    whitelist:
      paths:
        - /actuator/**
        - /health/**
        - /api/auth/**
        - /swagger-ui/**
        - /open/**
        - /external/**
```

---

## 常见问题

### Q1: 请求返回 401 Unauthorized

**可能原因**：
1. JWT Token 过期或无效
2. AppKey 签名错误
3. 时间戳超出允许范围
4. Nonce 重复使用

**排查步骤**：
1. 检查 Authorization 头格式
2. 检查 Token 是否过期
3. 检查签名算法是否正确
4. 检查系统时间是否同步

### Q2: 请求返回 403 Forbidden

**可能原因**：
1. 应用未订阅该 API
2. 应用已禁用
3. 应用已过期

### Q3: 请求返回 429 Too Many Requests

**原因**：触发限流

**解决**：
1. 降低请求频率
2. 调整限流配置
3. 使用 Redis 清理限流计数

### Q4: 动态路由不生效

**可能原因**：
1. API 未发布
2. 路由缓存未刷新

**解决**：
1. 检查 API 发布状态
2. 调用刷新接口或重启网关

### Q5: Dubbo 泛化调用失败

**可能原因**：
1. 后端服务未启动
2. Dubbo 注册中心连接失败
3. 接口/方法名配置错误

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-07 | 初始版本，实现 JWT/AppKey 认证、动态路由、限流 |
