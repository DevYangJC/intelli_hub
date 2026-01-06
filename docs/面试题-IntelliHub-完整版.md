# IntelliHub 企业级API开放平台 - 面试题库（完整版）

> 本文档包含项目相关的高频面试题及详细答案，涵盖项目综合、架构设计、技术难点、性能优化等8大类共50+问题

---

## 📚 目录

- [1. 项目综合类](#1-项目综合类)
- [2. 架构设计类](#2-架构设计类)  
- [3. 网关相关问题](#3-网关相关问题)
- [4. 多租户相关问题](#4-多租户相关问题)
- [5. 性能优化类](#5-性能优化类)
- [6. 分布式系统](#6-分布式系统)
- [7. 缓存与Redis](#7-缓存与redis)
- [8. 场景设计题](#8-场景设计题)

---

## 1. 项目综合类

### Q1: 介绍一下IntelliHub项目，它解决了什么问题？

**标准答案**：

IntelliHub是一个企业级API开放平台，解决企业对外开放API时的统一管理问题。

**业务背景**：
- 公司有大量内部API需要对外开放给合作伙伴
- 传统点对点对接方式，每个系统独立认证，安全标准不统一
- 缺乏统一的流量管控、监控和告警能力
- API版本混乱，变更难以追溯

**核心功能**：
1. **统一认证鉴权**：JWT + AppKey签名双认证
2. **API全生命周期管理**：创建→配置→发布→版本管理→下线
3. **流量治理**：多维限流、熔断降级
4. **实时监控告警**：秒级监控、智能告警
5. **多租户隔离**：全链路数据隔离

**技术架构**：
- 微服务架构：7个核心服务
- 响应式网关：Spring Cloud Gateway (WebFlux)
- 事件驱动：Kafka实现服务解耦
- 分布式缓存：Redis多级缓存
- 搜索引擎：Elasticsearch全文检索

**项目成果**：
- 日均**500万+次**API调用，峰值QPS **2000+**
- 对接周期从**2周缩短至2天**，效率提升7倍
- 系统可用性达**99.95%**，P99延迟**<300ms**
- 支持**100+个API**对外开放，服务**50+家**合作伙伴

---

### Q2: 项目的整体架构是怎样的？为什么这样设计？

**标准答案**：

**整体架构**：采用Spring Cloud微服务架构

```
┌─────────────────────────────────────┐
│  客户端层                            │
│  ├── Vue3前端控制台                  │
│  ├── Java SDK                       │
│  └── 第三方系统                      │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  网关层（Spring Cloud Gateway）      │
│  ├── JWT认证 / AppKey签名            │
│  ├── 多维限流 / 熔断降级             │
│  ├── 动态路由 / 日志上报             │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  业务服务层（微服务集群）             │
│  ├── IAM认证服务 (8081)              │
│  ├── API平台服务 (8082)              │
│  ├── 应用中心服务 (8085)             │
│  ├── 治理中心服务 (8083)             │
│  ├── 聚合搜索服务 (8086) ✅          │
│  └── 事件中心服务 (8087) ✅          │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  数据层                              │
│  ├── MySQL 8.0（主从）               │
│  ├── Redis 6.0（集群）               │
│  └── Elasticsearch 7.x              │
└─────────────────────────────────────┘
        ↑           ↑
┌───────┴───────┬───┴──────┐
│ Nacos注册中心  │ Kafka消息队列 │
└───────────────┴──────────┘
```

**设计理由**：

**1. 为什么用微服务架构？**
- ✅ **职责单一**：每个服务专注特定业务领域
- ✅ **独立部署**：网关服务可以单独扩容，无需重新部署全部服务
- ✅ **技术异构**：搜索服务用ES，监控服务用Redis，各取所长
- ✅ **故障隔离**：某个服务挂了不影响其他服务

**2. 为什么用Spring Cloud Gateway？**
- ✅ 响应式架构（WebFlux），非阻塞IO，高并发性能好
- ✅ 原生支持Nacos服务发现
- ✅ Filter机制灵活，易于扩展
- ✅ 性能优于Zuul 1.x（QPS提升1.5-2倍）

**3. 为什么引入Kafka？**
- ✅ 异步日志上报，不阻塞主流程（响应时间<5ms）
- ✅ 事件驱动架构，服务间解耦
- ✅ 高吞吐量（日均1000万+条日志）
- ✅ 消息持久化，数据不丢失

**4. 为什么用Elasticsearch？**
- ✅ 全文搜索 + 中文分词（IK分词器）
- ✅ 跨多个索引聚合搜索（API、应用、用户）
- ✅ 搜索性能好（响应时间<200ms）

---

### Q3: 你在项目中负责哪些模块？遇到的最大挑战是什么？

**标准答案**：

**主要负责模块**：
1. **统一网关服务**（核心）：认证、路由、限流、日志上报
2. **多租户体系**：全链路租户隔离设计与实现
3. **治理中心服务**：监控统计、智能告警
4. **聚合搜索服务**：Elasticsearch集成

**最大挑战**：高并发下的路由性能优化

**问题描述**：
- 初期网关路由匹配耗时**50ms**，成为性能瓶颈
- 路径参数匹配（`/api/user/{id}`）需要正则表达式，性能差
- 峰值QPS只能达到**200**，远低于预期的2000

**解决方案**：

**优化1 - 三级缓存策略**：
```
L1: 本地Caffeine缓存（TTL 1分钟，命中率95%）
L2: Redis缓存（TTL 5分钟，命中率99%）
L3: Dubbo服务调用（兜底，占比1%）
```

**优化2 - 路由预加载**：
```java
// 服务启动时预加载全部路由到内存
@PostConstruct
public void preloadRoutes() {
    List<ApiRoute> routes = apiService.getAllRoutes();
    for (ApiRoute route : routes) {
        localCache.put(route.getPath(), route);
    }
}
```

**优化3 - 正则表达式缓存**：
```java
// 缓存编译好的Pattern对象
Pattern pattern = patternCache.computeIfAbsent(
    routePath, 
    path -> Pattern.compile(pathToRegex(path))
);
```

**优化4 - 精确匹配优先**：
```java
// 精确匹配（Hash查找O(1)）优先于正则匹配（O(n)）
if (exactMatchMap.containsKey(path)) {
    return exactMatchMap.get(path);
}
```

**优化效果**：
- ✅ 路由匹配耗时：**50ms → <5ms**（性能提升10倍）
- ✅ 峰值QPS：**200 → 2000+**（提升10倍）
- ✅ P99延迟：**500ms → <300ms**
- ✅ 本地缓存命中率：**95%**

---

## 2. 架构设计类

### Q4: 为什么要做服务拆分？拆分的原则是什么？

**标准答案**：

**拆分理由**：
1. **职责单一**：每个服务专注于特定业务领域
2. **独立部署**：核心服务和扩展服务可以独立升级
3. **水平扩展**：流量大的服务（如网关）可以单独扩容
4. **技术异构**：不同服务可以选择不同技术栈

**拆分原则**：

**原则1 - 按业务领域拆分**：
- IAM服务：认证授权（用户、租户、角色、权限）
- API平台：API管理（创建、发布、版本）
- 应用中心：应用管理（AppKey、订阅、配额）
- 治理中心：监控告警（日志、统计、告警）

**原则2 - 核心与扩展分离**：
- **核心服务**：系统正常运行必需（网关、IAM、API平台）
- **扩展服务**：增强功能，可选部署（搜索、事件、AIGC）

**原则3 - 数据隔离**：
- 每个服务有独立的数据库表
- 服务间通过Dubbo接口或事件通信
- 避免跨服务的数据库JOIN

**拆分后的好处**：

| 方面 | 拆分前（单体） | 拆分后（微服务） |
|------|--------------|----------------|
| **部署** | 修改一个模块需要重新部署整个应用 | 只需部署变更的服务 |
| **扩容** | 整体扩容，资源浪费 | 网关3节点，其他服务1节点 |
| **发布** | 发布周期长（2小时） | 单服务发布快（15分钟） |
| **故障隔离** | 一处故障全部不可用 | 搜索服务挂了不影响API调用 |

**实际收益**：
- 发布效率提升**60%**（2小时 → 30分钟）
- 资源利用率提升**40%**（按需扩容）
- 故障影响范围降低**80%**（故障隔离）

---

### Q5: 网关和业务服务之间如何通信？为什么不都用HTTP？

**标准答案**：

**通信方式**：根据场景选择不同的通信协议

**1. HTTP通信**（外部API调用）
```
场景：网关转发到HTTP后端服务
实现：WebClient + Nacos服务发现
优点：简单通用，支持跨语言，易于调试
缺点：性能略低于RPC（序列化开销大）
```

**2. Dubbo RPC通信**（内部服务调用）
```
场景：网关调用内部微服务（验证租户、查询订阅）
实现：Dubbo泛化调用
优点：性能高（比HTTP快20-30%），二进制协议
缺点：只支持Java系统
```

**3. Kafka异步通信**（日志上报、事件通知）
```
场景：日志上报、API发布事件
实现：KafkaTemplate发送消息
优点：完全异步，高吞吐量，不阻塞主流程
缺点：不适合同步场景，消息有延迟
```

**性能对比测试**：

| 通信方式 | 单次调用耗时 | 并发能力 | 适用场景 |
|---------|-------------|---------|---------|
| **HTTP** | 20-50ms | 中 | 外部API转发 |
| **Dubbo RPC** | 5-15ms | 高 | 内部服务调用 |
| **Kafka** | <5ms（异步） | 极高 | 日志、事件 |

**实际应用**：

```java
// 场景1：网关转发到HTTP后端
webClient.post()
    .uri(backendUrl)
    .bodyValue(requestBody)
    .retrieve()
    .bodyToMono(String.class);

// 场景2：网关调用IAM服务验证租户（Dubbo）
@DubboReference
private TenantService tenantService;
boolean valid = tenantService.validateTenant(tenantId);

// 场景3：网关上报日志到Kafka（异步）
kafkaTemplate.send("call-log", logJson);
```

**为什么不都用HTTP？**
- **性能考虑**：内部调用频繁，Dubbo性能比HTTP高20-30%
- **类型安全**：Dubbo有接口定义，编译期就能发现问题
- **负载均衡**：Dubbo内置多种LB策略（随机、轮询、一致性Hash）

---

### Q6: 如何保证微服务之间的数据一致性？

**标准答案**：

**挑战**：微服务架构下，数据分散在不同服务，如何保证一致性？

**我们的策略**：根据业务场景选择不同的一致性级别

**场景1：强一致性（同步调用）**

**业务**：订阅API时，需要验证API是否存在

```java
@Service
public class SubscriptionService {
    
    @Transactional
    public void subscribe(String appId, String apiId) {
        // 1. 远程调用验证API（同步）
        ApiInfo api = apiPlatformService.getApiById(apiId);
        if (api == null || !api.isPublished()) {
            throw new BusinessException("API不存在或未发布");
        }
        
        // 2. 创建订阅关系（本地事务）
        subscriptionMapper.insert(new Subscription(appId, apiId));
    }
}
```

**特点**：
- ✅ 实时性强，立即返回结果
- ❌ 性能较低，RT增加
- 适用于**关键业务流程**

**场景2：最终一致性（事件驱动）**

**业务**：API发布后，通知网关更新路由

```java
// API平台：发布API
@Transactional
public void publishApi(String apiId) {
    // 1. 更新数据库
    apiMapper.updateStatus(apiId, "published");
    
    // 2. 发送事件到Kafka（事务提交后）
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send("api-event", 
                    new ApiPublishedEvent(apiId));
            }
        }
    );
}

// 网关：监听事件
@KafkaListener(topics = "api-event")
@Retryable(maxAttempts = 3)
public void onApiPublished(ApiPublishedEvent event) {
    routeService.refreshRoute(event.getApiId());
}
```

**特点**：
- ✅ 异步执行，不阻塞主流程
- ✅ 通过重试保证最终一致
- 适用于**辅助流程**

**场景3：定时补偿（兜底方案）**

```java
// 每5分钟检查路由一致性
@Scheduled(cron = "0 */5 * * * ?")
public void checkConsistency() {
    // 查询所有已发布API
    List<ApiInfo> apis = apiMapper.selectPublished();
    
    // 查询网关路由
    Set<String> gatewayRoutes = routeService.getAllRouteIds();
    
    // 找出差异并补偿
    for (ApiInfo api : apis) {
        if (!gatewayRoutes.contains(api.getId())) {
            log.warn("路由不一致: {}", api.getId());
            routeService.addRoute(api); // 补偿
        }
    }
}
```

**一致性保障**：

| 场景 | 一致性级别 | 实现方式 | 延迟 |
|------|-----------|---------|------|
| 订阅API | 强一致性 | 同步调用 | <100ms |
| 路由更新 | 最终一致性 | 事件驱动 + 重试 | <5秒 |
| 兜底 | 最终一致性 | 定时补偿 | <5分钟 |

**为什么不用2PC/TCC/Saga？**
- **2PC**：性能差，阻塞时间长，不适合高并发
- **TCC**：实现复杂，需要写大量补偿代码
- **Saga**：适合长事务，我们的场景都是短事务

**我们的选择**：
- 核心流程：强一致性（同步调用）
- 辅助流程：最终一致性（事件驱动 + 补偿）

---

## 3. 网关相关问题

### Q7: 为什么选择Spring Cloud Gateway而不是Zuul？

**标准答案**：

**Spring Cloud Gateway的优势**：

**1. 响应式架构（关键优势）**
```
Zuul 1.x：基于Servlet，阻塞IO，一个线程处理一个请求
Gateway：基于WebFlux和Reactor，非阻塞IO，少量线程处理大量并发
```

**性能对比**：
- 同样硬件（4核8G），Gateway QPS是Zuul的**1.5-2倍**
- 内存占用更少，延迟更低

**2. Spring生态整合**
```
✅ 原生支持Nacos、Sentinel等Spring Cloud组件
✅ 配置方式统一（application.yml）
✅ Filter机制灵活（GlobalFilter、GatewayFilter）
```

**3. 功能对比**

| 功能 | Gateway | Zuul 1.x | Zuul 2.x |
|------|---------|----------|----------|
| **IO模型** | 非阻塞 | 阻塞 | 非阻塞 |
| **性能** | 高 | 中 | 高 |
| **Spring集成** | 原生支持 | 需要适配 | 需要适配 |
| **社区活跃度** | 高 | 停止维护 | 低 |
| **WebSocket** | ✅ | ❌ | ✅ |
| **限流熔断** | ✅ 内置 | ⚠️ 需要集成 | ⚠️ 需要集成 |

**实际效果**：
- 单网关节点支持**2000+ QPS**
- P99延迟**<300ms**
- 4核8G服务器，CPU占用**<50%**

---

### Q8: 网关如何实现动态路由？配置变更后如何生效？

**标准答案**：

**核心设计**：三级缓存 + Redis Pub/Sub热更新

**路由匹配流程**：

```java
public Mono<ApiRouteDTO> matchRoute(String path, String method) {
    String cacheKey = path + ":" + method;
    
    // L1: 本地缓存（Caffeine，TTL 1分钟）
    ApiRouteDTO route = localCache.getIfPresent(cacheKey);
    if (route != null) {
        return Mono.just(route);
    }
    
    // L2: Redis缓存（TTL 5分钟）
    route = redisTemplate.opsForValue().get(cacheKey);
    if (route != null) {
        localCache.put(cacheKey, route);
        return Mono.just(route);
    }
    
    // L3: Dubbo服务调用
    return Mono.fromCallable(() -> {
        ApiRouteDTO r = apiPlatformService.matchRoute(path, method);
        redisTemplate.opsForValue().set(cacheKey, r, 5, TimeUnit.MINUTES);
        localCache.put(cacheKey, r);
        return r;
    });
}
```

**热更新机制**：

```java
// API平台：配置变更后发布消息
public void publishApi(String apiId) {
    // 1. 更新数据库
    apiMapper.updateStatus(apiId, "published");
    
    // 2. 发送Redis Pub/Sub消息
    redisTemplate.convertAndSend("route:refresh", apiId);
}

// 网关：订阅消息并刷新缓存
@Component
public class RouteRefreshListener implements MessageListener {
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String apiId = new String(message.getBody());
        
        // 1. 清除本地缓存
        localCache.invalidate(apiId);
        
        // 2. 清除Redis缓存
        redisTemplate.delete("route:" + apiId);
        
        log.info("路由已刷新: {}", apiId);
    }
}
```

**配置生效时间**：
- Redis Pub/Sub推送：**<1秒**
- 本地缓存过期：最长**1分钟**（TTL）
- **综合效果：秒级生效，无需重启**

**性能数据**：
- 本地缓存命中率：**95%**，耗时**<1ms**
- Redis缓存命中率：**99%**，耗时**<5ms**
- Dubbo调用：**<1%**，耗时**<50ms**

---

### Q9: 网关如何实现限流？限流算法是什么？

**标准答案**：

**限流算法**：滑动窗口算法（基于Redis ZSET实现）

**为什么选择滑动窗口？**

| 算法 | 优点 | 缺点 | 是否采用 |
|------|------|------|---------|
| **固定窗口** | 实现简单 | 有临界问题（窗口交界处流量翻倍） | ❌ |
| **漏桶** | 流量平滑 | 无法应对突发流量 | ❌ |
| **令牌桶** | 允许突发 | 实现复杂，需要定时任务补充令牌 | ❌ |
| **滑动窗口** | 精度高，准确控制QPS | 需要Redis支持 | ✅ |

**实现原理**：

```java
public class SlidingWindowRateLimiter {
    
    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000;
        
        // 使用Redis ZSET，分数是时间戳
        String script = 
            "redis.call('zremrangebyscore', KEYS[1], 0, ARGV[1])\n" +
            "local count = redis.call('zcard', KEYS[1])\n" +
            "if count < tonumber(ARGV[2]) then\n" +
            "    redis.call('zadd', KEYS[1], ARGV[3], ARGV[4])\n" +
            "    redis.call('expire', KEYS[1], ARGV[5])\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key),
            windowStart, limit, now, UUID.randomUUID().toString(), windowSeconds
        );
        
        return result == 1;
    }
}
```

**多维度限流**：

```java
// IP维度
String ipKey = "rate:ip:" + clientIp;
boolean passIp = rateLimiter.tryAcquire(ipKey, 100, 60); // 100次/分钟

// Path维度
String pathKey = "rate:path:" + path;
boolean passPath = rateLimiter.tryAcquire(pathKey, 1000, 60); // 1000次/分钟

// IP+Path组合维度
String combinedKey = "rate:ip_path:" + clientIp + ":" + path;
boolean passCombined = rateLimiter.tryAcquire(combinedKey, 10, 60); // 10次/分钟

if (!passIp || !passPath || !passCombined) {
    return Mono.error(new RateLimitException("请求过于频繁，请稍后再试"));
}
```

**性能优化**：
- 使用**Lua脚本**保证原子性（删除 + 统计 + 添加）
- Redis **Pipeline**批量执行，减少网络开销
- 自动过期清理，无需手动删除

**实际效果**：
- 限流判断耗时：**<5ms**
- 成功抵御多次突发流量攻击
- 限流误差：**<1%**

---

### Q10: 网关如何做认证？JWT和AppKey签名有什么区别？

**标准答案**：

**双流量认证机制**：根据请求路径选择不同的认证方式

**1. JWT Token认证（管理后台流量）**

**适用场景**：`/api/**`路径，前端管理控制台访问

**流程**：
```java
@Component
public class JwtAuthFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, ...) {
        // 1. 提取Token
        String token = exchange.getRequest()
            .getHeaders().getFirst("Authorization");
        
        // 2. 本地验签（不调用IAM服务，性能高）
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token.substring(7))
            .getBody();
        
        // 3. 提取用户信息
        String userId = claims.get("userId", String.class);
        String tenantId = claims.get("tenantId", String.class);
        
        // 4. 注入请求头
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-User-Id", userId)
            .header("X-Tenant-Id", tenantId)
            .build();
        
        return chain.filter(exchange.mutate().request(request).build());
    }
}
```

**优点**：
- ✅ 本地验签，无需远程调用，性能高
- ✅ Token包含用户信息，无需查库
- ✅ 支持过期时间，自动失效

**2. AppKey签名认证（开放API流量）**

**适用场景**：`/open/**`路径，第三方系统调用

**签名算法**：HMAC-SHA256
```java
// 客户端签名
String signContent = httpMethod + "\n" 
                   + requestPath + "\n" 
                   + timestamp + "\n"
                   + MD5(requestBody);
                   
String signature = HmacUtils.hmacSha256Hex(appSecret, signContent);

// 请求头
X-App-Key: IH4315340gtRfKPamTHVU4GE
X-Timestamp: 1640000000000
X-Signature: 3a7f8d2b9c1e...
```

**网关验证**：
```java
@Component
public class AppKeyAuthFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, ...) {
        // 1. 提取签名信息
        String appKey = getHeader("X-App-Key");
        String timestamp = getHeader("X-Timestamp");
        String signature = getHeader("X-Signature");
        
        // 2. 防重放攻击（时间戳校验）
        long now = System.currentTimeMillis();
        if (Math.abs(now - Long.parseLong(timestamp)) > 300000) {
            return unauthorized("请求已过期");
        }
        
        // 3. 查询AppSecret（Redis缓存）
        return getAppSecret(appKey).flatMap(appSecret -> {
            // 4. 计算签名
            String expected = calculateSignature(exchange, appSecret);
            
            // 5. 验证签名
            if (!signature.equals(expected)) {
                return unauthorized("签名验证失败");
            }
            
            // 6. 验证订阅关系
            return validateSubscription(appKey, path);
        });
    }
}
```

**安全措施**：
- ✅ AppSecret不在网络传输，只用于签名
- ✅ 时间戳防止重放攻击（5分钟有效期）
- ✅ 签名包含请求方法、路径、时间戳、Body MD5

**对比**：

| 对比项 | JWT Token | AppKey签名 |
|--------|-----------|-----------|
| **适用场景** | 管理后台，短期访问 | 开放API，长期调用 |
| **性能** | 高（本地验签） | 中（需要查询AppSecret） |
| **安全性** | Token可能被窃取 | 签名更安全，防重放 |
| **有效期** | 24小时（可刷新） | 永久（除非重置） |
| **携带信息** | 包含用户信息 | 只有AppKey |

---

## 4. 多租户相关问题

### Q11: 如何实现多租户数据隔离？如何保证100%不会越权？

**标准答案**：

**多租户方案**：逻辑隔离（共享数据库，tenant_id字段隔离）

**全链路隔离机制**：

**1. 网关层（租户ID注入）**
```java
// 从JWT中提取租户ID
Claims claims = parseJwt(token);
String tenantId = claims.get("tenantId", String.class);

// 注入请求头
ServerHttpRequest request = exchange.getRequest().mutate()
    .header("X-Tenant-Id", tenantId)
    .build();
```

**2. 服务层（ThreadLocal上下文）**
```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String tenantId = request.getHeader("X-Tenant-Id");
        if (tenantId == null) {
            throw new UnauthorizedException("缺少租户信息");
        }
        UserContextHolder.setTenantId(tenantId);
        return true;
    }
    
    @Override
    public void afterCompletion(...) {
        UserContextHolder.clear(); // 防止内存泄漏
    }
}
```

**3. 数据层（MyBatis Plus自动拦截）**
```java
@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor interceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        TenantLineInnerInterceptor tenantInterceptor = 
            new TenantLineInnerInterceptor();
            
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = UserContextHolder.getTenantId();
                return new StringValue(tenantId);
            }
            
            @Override
            public boolean ignoreTable(String tableName) {
                // 系统表不添加租户条件
                return "iam_tenant".equals(tableName);
            }
        });
        
        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}
```

**SQL自动改写示例**：
```sql
-- 原始SQL
SELECT * FROM api_info WHERE id = '123'

-- 自动改写后
SELECT * FROM api_info WHERE id = '123' AND tenant_id = 'tenant_xxx'
```

**安全保障措施（4道防线）**：

**防线1：网关强制注入**
- 租户ID只能从JWT中提取，客户端无法伪造

**防线2：服务层验证**
- 拦截器确保租户ID必须存在，否则拒绝请求

**防线3：数据层拦截**
- MyBatis插件自动添加WHERE条件，代码层面无法绕过

**防线4：单元测试覆盖**
```java
@Test
public void testTenantIsolation() {
    // 租户A创建API
    UserContextHolder.setTenantId("tenantA");
    String apiId = apiService.createApi(apiDTO);
    
    // 租户B查询，应该查不到
    UserContextHolder.setTenantId("tenantB");
    ApiInfo api = apiService.getById(apiId);
    assertNull(api); // 验证隔离有效
}
```

**安全成果**：
- 上线至今**0次**租户数据泄露事故
- 通过渗透测试验证，无越权漏洞
- 支持**50+租户**并发使用，互不干扰

---

### Q12: 为什么选择逻辑隔离而不是物理隔离（独立数据库）？

**标准答案**：

**三种多租户方案对比**：

| 方案 | 优点 | 缺点 | 成本 | 我们的选择 |
|------|------|------|------|----------|
| **独立数据库** | 隔离性最强，性能独立 | 成本高，维护复杂，扩展性差 | 极高 | ❌ |
| **独立Schema** | 隔离性较强 | 连接池管理复杂，Schema数量有限 | 中等 | ❌ |
| **逻辑隔离** | 成本低，维护简单，扩展性好 | 需要代码层面严格控制 | 低 | ✅ |

**我们选择逻辑隔离的原因**：

**1. 成本优势**
- 50个租户如果独立数据库，需要50个数据库实例
- 逻辑隔离只需1个数据库，**成本降低98%**

**2. 运维便利**
```
独立数据库：
- 备份：需要备份50个数据库
- 升级：需要升级50个数据库
- 监控：需要监控50个数据库

逻辑隔离：
- 备份：只需备份1个数据库
- 升级：只需升级1个数据库
- 监控：只需监控1个数据库
```

**3. 性能优势**
- 连接池共享，资源利用率高
- tenant_id有索引，查询性能不受影响
- 数据库连接数减少**98%**

**4. 扩展性**
- 新增租户无需创建数据库，秒级开通
- 理论上支持无限租户

**风险控制**：
- ✅ MyBatis拦截器强制隔离，代码层面无法绕过
- ✅ 定期安全审计，检查是否有漏掉租户过滤的SQL
- ✅ 完善的单元测试覆盖

**实际数据**：
- 单库支持**50+租户**
- 查询性能无明显下降（tenant_id索引生效）
- 数据库维护成本降低**90%**

---

## 5. 性能优化类

### Q13: 网关路由匹配性能如何优化的？从50ms优化到5ms？

**标准答案**：

**初始问题**：
- 路由匹配耗时**50ms**，成为性能瓶颈
- 峰值QPS只能达到**200**，远低于预期
- 支持路径参数（`/api/user/{id}`），正则匹配慢

**优化过程**：

**优化1：三级缓存策略**
```java
// L1: 本地缓存（Caffeine）
ApiRoute route = localCache.getIfPresent(key);
if (route != null) return route; // 命中率95%，<1ms

// L2: Redis缓存
route = redisTemplate.opsForValue().get(key);
if (route != null) {
    localCache.put(key, route);
    return route; // 命中率4%，<5ms
}

// L3: Dubbo服务调用
route = apiPlatformService.matchRoute(path); // 1%，<50ms
localCache.put(key, route);
redisTemplate.opsForValue().set(key, route);
return route;
```

**优化2：路由预加载**
```java
@PostConstruct
public void preloadRoutes() {
    List<ApiRoute> routes = apiService.getAllRoutes();
    for (ApiRoute route : routes) {
        localCache.put(route.getPath(), route);
        
        // 编译正则表达式并缓存
        Pattern pattern = Pattern.compile(pathToRegex(route.getPath()));
        patternCache.put(route.getPath(), pattern);
    }
    log.info("预加载{}条路由", routes.size());
}
```

**优化3：正则表达式缓存**
```java
// 之前：每次都编译
Pattern pattern = Pattern.compile(pathToRegex(routePath));
boolean matched = pattern.matcher(requestPath).matches();

// 之后：缓存Pattern对象
Pattern pattern = patternCache.computeIfAbsent(routePath, 
    path -> Pattern.compile(pathToRegex(path)));
boolean matched = pattern.matcher(requestPath).matches();
```

**优化4：精确匹配优先**
```java
public ApiRoute matchRoute(String path) {
    // 1. 精确匹配（Hash查找，O(1)）
    ApiRoute route = exactMatchMap.get(path);
    if (route != null) {
        return route;
    }
    
    // 2. 路径参数匹配（正则匹配，O(n)）
    for (Map.Entry<Pattern, ApiRoute> entry : patternRoutes.entrySet()) {
        if (entry.getKey().matcher(path).matches()) {
            return entry.getValue();
        }
    }
    
    return null;
}
```

**优化效果对比**：

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| **路由匹配耗时** | 50ms | <5ms | **10倍** |
| **峰值QPS** | 200 | 2000+ | **10倍** |
| **P99延迟** | 500ms | <300ms | **40%** |
| **CPU占用** | 80% | <50% | **38%** |

---

### Q14: Kafka消费延迟问题如何解决的？

**标准答案**：

**问题背景**：
- 网关异步上报调用日志到Kafka
- 治理中心消费Kafka消息，写入MySQL
- 高峰期消息堆积严重，**延迟10+分钟**
- 导致监控数据严重滞后

**问题排查**：
1. Kafka消费速度慢：单线程消费，处理速度<1000条/秒
2. 数据库写入慢：逐条INSERT，未使用批量插入
3. 单个分区：Kafka Topic只有1个分区，无法并行消费

**解决方案**：

**方案1：增加消费者线程数**
```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> 
        kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    
    // 设置并发消费者数量：2 → 8
    factory.setConcurrency(8);
    
    // 批量拉取消息
    factory.setBatchListener(true);
    factory.getContainerProperties().setPollTimeout(1000);
    
    return factory;
}
```

**方案2：数据库批量插入**
```xml
<!-- MyBatis批量插入，500条一批 -->
<insert id="batchInsert" parameterType="java.util.List">
    INSERT INTO call_log (id, api_id, status, latency, create_time)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.id}, #{item.apiId}, #{item.status}, 
         #{item.latency}, #{item.createTime})
    </foreach>
</insert>
```

**方案3：双写策略（Redis + Kafka）**
```java
public Mono<Void> reportLog(CallLogDTO log) {
    return Mono.when(
        // 通道1：写入Kafka（异步持久化）
        Mono.fromRunnable(() -> 
            kafkaTemplate.send("call-log", JSON.toJSONString(log))
        ),
        
        // 通道2：写入Redis（实时统计）
        Mono.fromRunnable(() -> {
            String key = "stats:api:" + log.getApiId() 
                       + ":minute:" + (now / 60000);
            redisTemplate.opsForZSet().add(key, log.getId(), log.getLatency());
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        })
    ).then();
}
```

**方案4：增加Kafka分区数**
```bash
# 增加分区数，支持并行消费
kafka-topics.sh --alter \
    --topic intellihub-call-log \
    --partitions 8
```

**优化效果**：

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| **消费速度** | 1000条/秒 | 8000条/秒 | **8倍** |
| **消息堆积** | 10分钟 | <10秒 | **60倍** |
| **数据库写入** | 逐条INSERT | 批量INSERT | **20倍** |
| **监控实时性** | 10分钟延迟 | 秒级 | **600倍** |

---

### Q15: 如何保证高可用？系统如何做容灾？

**标准答案**：

**高可用架构**：

```
[Nginx负载均衡]
      ↓
[网关集群] → [GW-1] [GW-2] [GW-3]  (3节点)
      ↓
[业务服务] → [API-1] [API-2]  (2节点)
      ↓
[数据库] → [MySQL主从] + [Redis Cluster]
```

**具体措施**：

**1. 服务高可用**
- 网关部署**3个节点**，Nginx做负载均衡（轮询）
- 核心服务至少**2个节点**，非核心服务1个节点
- 数据库主从架构，读写分离

**2. 熔断降级**
```java
@Service
public class ApiPlatformServiceFallback implements ApiPlatformService {
    
    @Override
    public ApiRoute matchRoute(String path) {
        // 降级策略：返回Redis缓存或默认路由
        ApiRoute cached = redisTemplate.opsForValue()
            .get("route:fallback:" + path);
        
        if (cached != null) {
            return cached;
        }
        
        // 返回通用错误路由
        return ApiRoute.builder()
            .path(path)
            .backendUrl("http://error-service/unavailable")
            .build();
    }
}
```

**3. 限流保护**
- **网关限流**：防止流量过大打垮后端（2000 QPS）
- **应用限流**：每个应用有配额限制（1000次/天）
- **数据库限流**：慢查询自动熔断（>1秒）

**4. 监控告警**
```java
// 实时监控关键指标
- 错误率 > 5%：WARNING级别告警
- 错误率 > 10%：CRITICAL级别告警，钉钉通知
- P99延迟 > 1000ms：WARNING告警
- QPS突增 > 正常值3倍：告警并自动扩容
```

**5. 容灾演练**
- **每月进行容灾演练**：随机下线一个节点，验证系统是否正常
- **故障注入测试**：主动制造故障（如网络延迟、磁盘满），测试恢复能力

**高可用指标**：

| 指标 | 目标 | 实际达成 |
|------|------|---------|
| **系统可用性** | 99.9% | **99.95%** |
| **年停机时间** | <8.76小时 | <4.4小时 |
| **故障恢复时间（MTTR）** | <10分钟 | **<5分钟** |
| **恢复时间目标（RTO）** | <10分钟 | **<10分钟** |
| **恢复点目标（RPO）** | <5分钟 | **<1分钟** |

---

## 6. 分布式系统

### Q16: 分布式事务如何处理？有哪些场景？

**标准答案**：

**我们的原则**：**尽量避免分布式事务**，通过业务设计规避

**场景分析**：

**场景1：订阅API（强一致性）**

**业务流程**：
1. 验证API是否存在（调用API平台服务）
2. 创建订阅关系（写入应用中心数据库）

**解决方案**：同步调用 + 本地事务
```java
@Transactional
public void subscribe(String appId, String apiId) {
    // 1. 远程调用验证API（不在事务内）
    ApiInfo api = apiPlatformService.getApiById(apiId);
    if (api == null || !api.isPublished()) {
        throw new BusinessException("API不存在或未发布");
    }
    
    // 2. 本地事务创建订阅
    subscriptionMapper.insert(new Subscription(appId, apiId));
}
```

**为什么不需要分布式事务？**
- API验证失败直接抛异常，不会创建订阅
- API状态变更（如下线）通过事件通知，异步处理

**场景2：API发布（最终一致性）**

**业务流程**：
1. 更新API状态为已发布（API平台服务）
2. 通知网关刷新路由（网关服务）
3. 创建索引（搜索服务）

**解决方案**：事件驱动 + 最终一致性
```java
@Transactional
public void publishApi(String apiId) {
    // 1. 本地事务更新状态
    apiMapper.updateStatus(apiId, ApiStatus.PUBLISHED);
    
    // 2. 事务提交后发送事件
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send("api-event", 
                    new ApiPublishedEvent(apiId));
            }
        }
    );
}

// 网关监听事件
@KafkaListener(topics = "api-event")
@Retryable(maxAttempts = 3)
public void onApiPublished(ApiPublishedEvent event) {
    routeService.refreshRoute(event.getApiId());
}
```

**最终一致性保障**：
- 事件发送失败：Kafka自动重试
- 消费失败：@Retryable注解重试3次
- 兜底方案：定时任务扫描未同步的路由

**场景3：定时补偿（兜底方案）**
```java
// 每5分钟检查路由一致性
@Scheduled(cron = "0 */5 * * * ?")
public void checkConsistency() {
    List<ApiInfo> apis = apiMapper.selectPublished();
    Set<String> gatewayRoutes = routeService.getAllRouteIds();
    
    for (ApiInfo api : apis) {
        if (!gatewayRoutes.contains(api.getId())) {
            log.warn("路由不一致: {}", api.getId());
            routeService.addRoute(api); // 补偿
        }
    }
}
```

**为什么不用2PC/TCC/Saga？**

| 方案 | 优点 | 缺点 | 我们是否采用 |
|------|------|------|------------|
| **2PC** | 强一致性 | 性能差，阻塞时间长，协调者单点 | ❌ |
| **TCC** | 灵活性高 | 实现复杂，需要写大量补偿代码 | ❌ |
| **Saga** | 适合长事务 | 我们的场景都是短事务 | ❌ |
| **最终一致性** | 简单，性能好 | 短暂不一致 | ✅ |

---

### Q17: 如何保证Kafka消息不丢失？

**标准答案**：

**消息丢失的三个环节**：
1. 生产者发送丢失
2. Broker存储丢失
3. 消费者消费丢失

**我们的保障措施**：

**1. 生产者端（网关发送日志）**
```java
@Bean
public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    
    // ACK级别：all（等待所有副本确认）
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    
    // 失败重试3次
    props.put(ProducerConfig.RETRIES_CONFIG, 3);
    
    // 保证顺序：同一时刻只有1个请求在途
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
    
    // 幂等性：防止重试导致重复
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    
    return new DefaultKafkaProducerFactory<>(props);
}

// 发送消息并处理失败
public Mono<Void> sendLog(CallLogDTO log) {
    return Mono.fromFuture(
        kafkaTemplate.send("call-log", log.getId(), JSON.toJSONString(log))
            .addCallback(
                result -> log.info("发送成功: {}", log.getId()),
                ex -> {
                    log.error("发送失败: {}", log.getId(), ex);
                    // 失败后写入本地文件，定时任务重新发送
                    saveToLocalFile(log);
                }
            )
    ).then();
}
```

**2. Broker端（Kafka集群）**
```properties
# 副本数量（3个副本）
replication.factor=3

# 最小同步副本数（至少2个副本写入成功）
min.insync.replicas=2

# 日志刷盘策略
log.flush.interval.messages=1000
log.flush.interval.ms=1000
```

**3. 消费者端（治理中心消费）**
```java
@KafkaListener(
    topics = "call-log",
    groupId = "governance-group",
    properties = {"enable.auto.commit=false"} // 手动提交
)
public void consume(ConsumerRecord<String, String> record,
                   Acknowledgment ack) {
    try {
        // 1. 解析消息
        CallLogDO log = JSON.parseObject(record.value(), CallLogDO.class);
        
        // 2. 处理业务（幂等性保证）
        callLogService.save(log);
        
        // 3. 手动提交offset
        ack.acknowledge();
        
    } catch (Exception e) {
        log.error("消费失败: offset={}", record.offset(), e);
        // 不提交offset，下次重新消费
        
        // 连续失败多次，写入死信队列
        if (getRetryCount(record) > 3) {
            sendToDeadLetterQueue(record);
            ack.acknowledge();
        }
    }
}
```

**幂等性保证**：
```java
@Transactional
public void save(CallLogDO log) {
    // 重复消费不会重复插入
    CallLogDO existing = callLogMapper.selectById(log.getId());
    if (existing != null) {
        log.info("日志已存在，跳过: id={}", log.getId());
        return;
    }
    callLogMapper.insert(log);
}
```

**整体保障**：

| 环节 | 保障措施 | 效果 |
|------|---------|------|
| **生产者** | ACK=all + 重试3次 + 幂等性 | 发送成功率99.99% |
| **Broker** | 3副本 + min.insync.replicas=2 | 允许1个节点故障 |
| **消费者** | 手动提交 + 幂等性 + 死信队列 | 消息不丢失 |

**实际效果**：
- 消息丢失率：**<0.001%**
- 即使Kafka集群1个节点故障，也不会丢消息

---

## 7. 缓存与Redis

### Q18: 如何解决缓存一致性问题？

**标准答案**：

**场景**：API配置变更后，如何保证网关缓存及时更新？

**一致性问题**：
```
数据库更新 → Redis缓存更新 → 本地缓存更新
如果某个环节失败，会导致缓存不一致
```

**我们的方案**：

**方案1：Cache Aside模式（旁路缓存）**
```java
// 更新数据：先更新数据库，再删除缓存
@Transactional
public void updateApi(ApiInfo apiInfo) {
    // 1. 更新数据库
    apiMapper.updateById(apiInfo);
    
    // 2. 删除Redis缓存（而不是更新）
    redisTemplate.delete("api:" + apiInfo.getId());
    
    // 3. 通知网关删除本地缓存
    redisTemplate.convertAndSend("route:refresh", apiInfo.getId());
}

// 读取数据：先查缓存，缓存miss再查数据库
public ApiInfo getApi(String apiId) {
    // 1. 查Redis缓存
    ApiInfo api = redisTemplate.opsForValue().get("api:" + apiId);
    if (api != null) {
        return api;
    }
    
    // 2. 查数据库
    api = apiMapper.selectById(apiId);
    
    // 3. 写入缓存
    if (api != null) {
        redisTemplate.opsForValue().set("api:" + apiId, api, 
            5, TimeUnit.MINUTES);
    }
    
    return api;
}
```

**为什么删除而不是更新？**
- ✅ 删除操作简单，不会失败
- ✅ 更新操作可能失败（序列化异常、网络超时）
- ✅ 延迟双删可以解决并发问题

**方案2：延迟双删**
```java
@Transactional
public void updateApi(ApiInfo apiInfo) {
    // 1. 第一次删除缓存
    redisTemplate.delete("api:" + apiInfo.getId());
    
    // 2. 更新数据库
    apiMapper.updateById(apiInfo);
    
    // 3. 延迟500ms第二次删除缓存
    CompletableFuture.runAsync(() -> {
        try {
            Thread.sleep(500);
            redisTemplate.delete("api:" + apiInfo.getId());
        } catch (Exception e) {
            log.error("延迟删除缓存失败", e);
        }
    });
}
```

**为什么要延迟双删？**

解决并发读写问题：
```
时刻1: 线程A删除缓存
时刻2: 线程B查询，缓存miss，查数据库（旧数据）
时刻3: 线程A更新数据库（新数据）
时刻4: 线程B写入缓存（旧数据） ← 问题出现
时刻5: 线程A再次删除缓存 ← 解决问题
```

**方案3：Redis Pub/Sub通知**
```java
// API平台：配置变更后发布消息
redisTemplate.convertAndSend("route:refresh", apiId);

// 网关：订阅消息
@Component
public class RouteRefreshListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String apiId = new String(message.getBody());
        localCache.invalidate(apiId); // 删除本地缓存
        redisTemplate.delete("route:" + apiId); // 删除Redis缓存
    }
}
```

**一致性保障**：

| 场景 | 策略 | 最终一致性时间 |
|------|------|---------------|
| **普通更新** | Cache Aside + 删除缓存 | <1秒 |
| **并发更新** | 延迟双删 | <1秒 |
| **网关缓存** | Redis Pub/Sub | <1秒 |
| **兜底** | 缓存TTL过期 | <5分钟 |

---

### Q19: Redis如何实现分布式锁？有什么坑？

**标准答案**：

**基础实现**：
```java
public boolean tryLock(String lockKey, String requestId, int expireTime) {
    // SET key value NX EX seconds
    Boolean result = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS);
    return Boolean.TRUE.equals(result);
}

public void unlock(String lockKey, String requestId) {
    // Lua脚本保证原子性：只能删除自己加的锁
    String script = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
        Collections.singletonList(lockKey), requestId);
}
```

**使用示例**：
```java
public void processApiPublish(String apiId) {
    String lockKey = "lock:api:" + apiId;
    String requestId = UUID.randomUUID().toString();
    
    // 尝试加锁
    if (!tryLock(lockKey, requestId, 30)) {
        throw new BusinessException("操作太频繁，请稍后再试");
    }
    
    try {
        // 业务逻辑
        publishApi(apiId);
    } finally {
        // 释放锁
        unlock(lockKey, requestId);
    }
}
```

**常见的坑**：

**坑1：锁被其他线程释放**
```java
// ❌ 错误：直接删除，可能删除别人的锁
redisTemplate.delete(lockKey);

// ✅ 正确：先判断是不是自己的锁
if (requestId.equals(redisTemplate.opsForValue().get(lockKey))) {
    redisTemplate.delete(lockKey);
}

// ✅ 更好：使用Lua脚本保证原子性
```

**坑2：锁超时，业务未执行完**
```java
// 问题：业务执行时间超过锁超时时间
tryLock("lock:api", "req123", 10); // 10秒后自动释放
processApi(); // 实际需要15秒

// 解决方案：看门狗机制（Redisson实现）
RLock lock = redisson.getLock("lock:api");
lock.lock(); // 自动续期
try {
    processApi();
} finally {
    lock.unlock();
}
```

**坑3：Redis主从切换导致锁丢失**
```
时刻1: 客户端A在主节点加锁成功
时刻2: 主节点宕机，锁还未同步到从节点
时刻3: 从节点升级为主节点
时刻4: 客户端B在新主节点加锁成功 ← 两个客户端都持有锁
```

**解决方案**：RedLock算法（Redis作者提出）
```java
// 在多个Redis实例上加锁，超过半数成功才算成功
Config config = new Config();
config.useClusterServers()
    .addNodeAddress("redis://127.0.0.1:6379")
    .addNodeAddress("redis://127.0.0.1:6380")
    .addNodeAddress("redis://127.0.0.1:6381");

RedissonClient redisson = Redisson.create(config);
RLock lock = redisson.getLock("lock:api");
lock.lock();
```

**我们的实践**：
- 普通场景：使用Redis单实例锁（性能好）
- 关键场景：使用Redisson的RedLock（可靠性高）

---

## 8. 场景设计题

### Q20: 如何设计一个API限流系统？

**标准答案**：

**需求分析**：
1. 支持多种限流维度（IP、API、应用、租户）
2. 支持多种限流算法（固定窗口、滑动窗口、令牌桶）
3. 限流规则可动态配置
4. 高性能（不能成为瓶颈）
5. 高可用（Redis故障不影响系统）

**架构设计**：

```
┌─────────────────────────────────────┐
│  网关层                              │
│  ├── RateLimitFilter                │
│  ├── 限流规则加载器                   │
│  └── 限流降级策略                     │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  限流层                              │
│  ├── 滑动窗口限流器（Redis ZSET）     │
│  ├── 令牌桶限流器（Redis + Lua）      │
│  └── 本地限流器（Guava RateLimiter） │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│  配置层                              │
│  ├── 限流规则（MySQL）                │
│  ├── 规则缓存（Redis）                │
│  └── 规则推送（Redis Pub/Sub）        │
└─────────────────────────────────────┘
```

**核心实现**：

**1. 限流规则定义**
```java
public class RateLimitRule {
    private String dimension; // IP, API, APP, TENANT
    private String target; // 具体的IP地址、API ID等
    private int limit; // 限流阈值
    private int windowSeconds; // 时间窗口（秒）
    private String algorithm; // SLIDING_WINDOW, TOKEN_BUCKET
}
```

**2. 滑动窗口限流器**
```java
public class SlidingWindowRateLimiter {
    
    public boolean tryAcquire(RateLimitRule rule) {
        String key = buildKey(rule);
        long now = System.currentTimeMillis();
        long windowStart = now - rule.getWindowSeconds() * 1000;
        
        // Lua脚本保证原子性
        String script =
            "redis.call('zremrangebyscore', KEYS[1], 0, ARGV[1])\n" +
            "local count = redis.call('zcard', KEYS[1])\n" +
            "if count < tonumber(ARGV[2]) then\n" +
            "    redis.call('zadd', KEYS[1], ARGV[3], ARGV[4])\n" +
            "    redis.call('expire', KEYS[1], ARGV[5])\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key),
            windowStart, rule.getLimit(), now, UUID.randomUUID(), 
            rule.getWindowSeconds()
        );
        
        return result == 1;
    }
}
```

**3. 多维度限流**
```java
public Mono<Void> rateLimitFilter(ServerWebExchange exchange, ...) {
    String ip = getClientIp(exchange);
    String apiId = getApiId(exchange);
    String appId = getAppId(exchange);
    String tenantId = getTenantId(exchange);
    
    // 1. IP维度限流
    if (!checkRateLimit("IP", ip)) {
        return reject(exchange, "IP限流");
    }
    
    // 2. API维度限流
    if (!checkRateLimit("API", apiId)) {
        return reject(exchange, "API限流");
    }
    
    // 3. 应用维度限流
    if (!checkRateLimit("APP", appId)) {
        return reject(exchange, "应用限流");
    }
    
    // 4. 租户维度限流
    if (!checkRateLimit("TENANT", tenantId)) {
        return reject(exchange, "租户限流");
    }
    
    return chain.filter(exchange);
}
```

**4. 限流降级策略**
```java
public boolean checkRateLimit(String dimension, String target) {
    try {
        // 1. 查询限流规则（带缓存）
        RateLimitRule rule = getRateLimitRule(dimension, target);
        if (rule == null) {
            return true; // 没有限流规则，放行
        }
        
        // 2. 执行限流检查
        return rateLimiter.tryAcquire(rule);
        
    } catch (Exception e) {
        log.error("限流检查异常", e);
        // 降级策略：Redis故障时，使用本地限流器
        return localRateLimiter.tryAcquire(dimension + ":" + target);
    }
}
```

**5. 限流规则动态更新**
```java
// 管理后台：更新限流规则
public void updateRateLimitRule(RateLimitRule rule) {
    // 1. 更新数据库
    ruleMapper.updateById(rule);
    
    // 2. 更新Redis缓存
    redisTemplate.opsForValue().set("rule:" + rule.getId(), rule);
    
    // 3. 通知网关刷新
    redisTemplate.convertAndSend("rule:refresh", rule.getId());
}

// 网关：监听规则变更
@Component
public class RuleRefreshListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String ruleId = new String(message.getBody());
        // 删除本地缓存，下次查询时重新加载
        localRuleCache.invalidate(ruleId);
    }
}
```

**性能优化**：
- ✅ 限流规则缓存在本地内存（TTL 1分钟）
- ✅ 使用Lua脚本保证原子性，减少网络开销
- ✅ Redis Pipel在批量检查多个维度
- ✅ Redis故障时降级到本地限流器

**实际效果**：
- 限流判断耗时：**<5ms**
- 支持**10000+条**限流规则
- QPS：**2000+**
- 误差率：**<1%**

---

### Q21: 如何设计一个秒杀系统的API？

**标准答案**：

**需求分析**：
1. 高并发：10万QPS
2. 防超卖：库存100，卖出不能超过100
3. 防刷：同一用户只能买1次
4. 高可用：不能因为秒杀影响其他业务

**架构设计**：

```
┌──────────────────────────────────────┐
│  前端                                 │
│  ├── 按钮置灰（开始前禁用）            │
│  ├── 验证码（防机器人）                │
│  └── 限流（防重复点击）                │
└───────────────┬──────────────────────┘
                ↓
┌──────────────────────────────────────┐
│  网关层                               │
│  ├── IP限流（100次/分钟）              │
│  ├── 令牌桶限流（1万QPS）              │
│  └── 熔断降级（错误率>10%）            │
└───────────────┬──────────────────────┘
                ↓
┌──────────────────────────────────────┐
│  应用层                               │
│  ├── Redis预减库存                    │
│  ├── 异步下单（Kafka消息队列）         │
│  └── 幂等性保证（防重复下单）          │
└───────────────┬──────────────────────┘
                ↓
┌──────────────────────────────────────┐
│  数据层                               │
│  ├── MySQL（订单持久化）               │
│  ├── Redis（库存缓存）                 │
│  └── Kafka（消息队列）                 │
└──────────────────────────────────────┘
```

**核心实现**：

**1. Redis预减库存**
```java
public boolean tryDecrStock(Long productId, Long userId) {
    String stockKey = "seckill:stock:" + productId;
    String userKey = "seckill:user:" + productId + ":" + userId;
    
    // Lua脚本保证原子性
    String script =
        "-- 1. 检查用户是否已经购买\n" +
        "if redis.call('exists', KEYS[2]) == 1 then\n" +
        "    return -1\n" +  // 已购买
        "end\n" +
        "-- 2. 预减库存\n" +
        "local stock = redis.call('get', KEYS[1])\n" +
        "if not stock or tonumber(stock) <= 0 then\n" +
        "    return 0\n" +  // 库存不足
        "end\n" +
        "redis.call('decr', KEYS[1])\n" +
        "redis.call('setex', KEYS[2], 3600, 1)\n" +  // 标记已购买
        "return 1";  // 成功
    
    Long result = redisTemplate.execute(
        new DefaultRedisScript<>(script, Long.class),
        Arrays.asList(stockKey, userKey)
    );
    
    return result == 1;
}
```

**2. 异步下单**
```java
@PostMapping("/seckill")
public Result<String> seckill(@RequestParam Long productId, 
                               @RequestParam Long userId) {
    // 1. Redis预减库存
    if (!tryDecrStock(productId, userId)) {
        return Result.fail("库存不足或已购买");
    }
    
    // 2. 发送消息到Kafka（异步下单）
    SeckillMessage msg = new SeckillMessage(productId, userId);
    kafkaTemplate.send("seckill-order", JSON.toJSONString(msg));
    
    // 3. 立即返回（不等待下单完成）
    return Result.success("排队中，请稍后查询订单");
}

// 消费Kafka消息，创建订单
@KafkaListener(topics = "seckill-order")
public void createOrder(ConsumerRecord<String, String> record) {
    SeckillMessage msg = JSON.parseObject(
        record.value(), SeckillMessage.class);
    
    try {
        // 创建订单（幂等性保证）
        orderService.createSeckillOrder(msg.getProductId(), msg.getUserId());
    } catch (Exception e) {
        log.error("创建订单失败", e);
        // 回补库存
        redisTemplate.opsForValue().increment(
            "seckill:stock:" + msg.getProductId());
    }
}
```

**3. 幂等性保证**
```java
@Transactional
public void createSeckillOrder(Long productId, Long userId) {
    String orderKey = "order:" + productId + ":" + userId;
    
    // 1. 分布式锁防止重复下单
    RLock lock = redisson.getLock(orderKey);
    lock.lock();
    
    try {
        // 2. 检查是否已下单
        Order existingOrder = orderMapper.selectByUserAndProduct(userId, productId);
        if (existingOrder != null) {
            return; // 已下单，幂等返回
        }
        
        // 3. 扣减数据库库存（兜底）
        int rows = productMapper.decrStock(productId, 1);
        if (rows == 0) {
            throw new BusinessException("库存不足");
        }
        
        // 4. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        orderMapper.insert(order);
        
    } finally {
        lock.unlock();
    }
}
```

**4. 数据库库存扣减（防超卖）**
```xml
<update id="decrStock">
    UPDATE product
    SET stock = stock - #{quantity}
    WHERE id = #{productId}
      AND stock >= #{quantity}  <!-- 防止超卖 -->
</update>
```

**性能优化**：

| 优化点 | 方案 | 效果 |
|--------|------|------|
| **减少数据库压力** | Redis预减库存 | 99%请求不访问数据库 |
| **提升响应速度** | 异步下单 | 响应时间从200ms降至<10ms |
| **防止超卖** | 数据库乐观锁 | 100%防超卖 |
| **防止重复下单** | 分布式锁 + 幂等性 | 100%防重复 |

**压测数据**：
- 支持QPS：**10万+**
- 响应时间：**<10ms**
- 超卖概率：**0%**
- 重复下单概率：**0%**

---

## 💡 面试技巧

### 回答问题的STAR法则

**S (Situation)**: 项目背景
**T (Task)**: 面临的问题
**A (Action)**: 采取的行动
**R (Result)**: 最终结果（量化）

**示例**：
```
Q: 你在项目中遇到的最大挑战是什么？

S: 网关路由匹配耗时50ms，成为性能瓶颈
T: 需要将路由匹配耗时降至<5ms，支持2000+ QPS
A: 采用三级缓存 + 路由预加载 + 正则缓存 + 精确匹配优先
R: 路由匹配耗时从50ms降至<5ms，QPS从200提升至2000+
```

### 高频追问及应对

**追问1："为什么选择这个技术方案？"**
- 回答：对比其他方案，说明优缺点，解释为什么适合当前场景

**追问2："如果让你重新设计，会怎么改进？"**
- 回答：承认现有方案的不足，提出改进方向（表现谦虚和学习能力）

**追问3："这个方案的性能瓶颈在哪里？"**
- 回答：分析系统的瓶颈（CPU、内存、IO、网络），提出优化方向

**追问4："如果流量增长10倍，系统能支撑吗？"**
- 回答：分析系统容量，说明扩容方案（水平扩展、垂直扩展、架构优化）

---

## 📝 总结

**核心技能点**：
- ✅ 微服务架构设计与实践
- ✅ 高并发系统性能优化
- ✅ 分布式系统一致性保证
- ✅ 多租户SaaS架构
- ✅ 响应式编程（WebFlux）
- ✅ 分布式缓存与限流
- ✅ 消息队列与事件驱动
- ✅ 搜索引擎集成

**可量化的成果**：
- 日均**500万+次**API调用
- 峰值QPS **2000+**
- P99延迟**<300ms**
- 系统可用性**99.95%**
- 对接周期从**2周缩短至2天**

**面试加分项**：
- ✅ 有完整的项目经历（从0到1）
- ✅ 解决过实际的性能问题（有量化数据）
- ✅ 熟悉分布式系统常见问题及解决方案
- ✅ 有技术选型和架构设计经验
- ✅ 代码质量高（单元测试、代码规范）

---

**祝你面试顺利！💪**
