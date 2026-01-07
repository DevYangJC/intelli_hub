# IntelliHub 聚合搜索服务实现文档

## 目录

1. [概述](#概述)
2. [整体架构](#整体架构)
3. [与其他服务的关系](#与其他服务的关系)
4. [核心概念](#核心概念)
5. [搜索功能](#搜索功能)
6. [数据同步](#数据同步)
7. [定时任务架构](#定时任务架构)
8. [索引设计](#索引设计)
9. [配置说明](#配置说明)
10. [常见问题](#常见问题)

---

## 概述

聚合搜索服务是 IntelliHub 的**全文搜索和数据检索中心**，基于 Elasticsearch 提供跨资源类型的聚合搜索能力，支持 API、应用、用户等多种资源的统一检索。

### 核心能力

| 能力 | 说明 |
|------|------|
| 聚合搜索 | 一次查询多种资源类型，统一返回 |
| 全文检索 | 支持中文分词、关键词高亮 |
| 分面统计 | 按类型统计命中数量 |
| 数据同步 | 定时从源服务同步数据到 ES |
| 增量同步 | 支持全量和增量两种同步模式 |

### 服务信息

| 项目 | 值 |
|------|-----|
| 服务名称 | intelli-search-service |
| 端口 | 8086 |
| 搜索引擎 | Elasticsearch |
| 索引前缀 | intellihub |

---

## 整体架构

### 系统架构图

```mermaid
graph TB
    subgraph "前端"
        Console[管理控制台<br/>全局搜索框]
    end
    
    subgraph "搜索服务 intelli-search-service"
        subgraph "Controller层"
            SearchCtrl[SearchController<br/>搜索接口]
            SyncCtrl[SyncController<br/>同步管理]
        end
        
        subgraph "Service层"
            AggService[AggregateSearchService<br/>聚合搜索]
            ApiIndex[ApiIndexService]
            AppIndex[AppIndexService]
            UserIndex[UserIndexService]
        end
        
        subgraph "Sync层"
            ApiSync[ApiSyncTask]
            AppSync[AppSyncTask]
            UserSync[UserSyncTask]
        end
    end
    
    subgraph "数据源服务"
        ApiPlatform[API平台服务<br/>intelli-api-platform-service]
        AppCenter[应用中心服务<br/>intelli-app-center-service]
        UserService[用户服务<br/>intelli-user-service]
    end
    
    subgraph "存储"
        ES[(Elasticsearch<br/>搜索索引)]
        Redis[(Redis<br/>分布式锁)]
    end
    
    Console -->|HTTP| SearchCtrl
    Console -->|HTTP| SyncCtrl
    
    SearchCtrl --> AggService
    AggService --> ApiIndex
    AggService --> AppIndex
    AggService --> UserIndex
    
    ApiIndex --> ES
    AppIndex --> ES
    UserIndex --> ES
    
    ApiSync -->|Dubbo| ApiPlatform
    AppSync -->|Dubbo| AppCenter
    UserSync -->|Dubbo| UserService
    
    ApiSync --> ApiIndex
    AppSync --> AppIndex
    UserSync --> UserIndex
    
    ApiSync --> Redis
    AppSync --> Redis
    UserSync --> Redis
```

### 技术栈

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| 框架 | Spring Boot 2.x | 微服务基础框架 |
| 搜索引擎 | Elasticsearch 7.x | 全文搜索 |
| RPC | Dubbo | 从源服务获取数据 |
| 分布式锁 | Redis | 防止重复同步 |
| 注册中心 | Nacos | 服务注册发现 |

---

## 与其他服务的关系

### 服务依赖关系图

```mermaid
flowchart TB
    subgraph "搜索服务"
        Search[intelli-search-service<br/>:8086]
    end
    
    subgraph "数据源服务"
        API[intelli-api-platform-service<br/>:8082]
        App[intelli-app-center-service<br/>:8085]
        User[intelli-user-service<br/>:8081]
    end
    
    subgraph "基础设施"
        ES[(Elasticsearch<br/>:9200)]
        Redis[(Redis<br/>:6379)]
        Nacos[Nacos<br/>:8848]
    end
    
    Search -->|Dubbo<br/>getAllApiInfoForSync| API
    Search -->|Dubbo<br/>getAllAppInfoForSync| App
    Search -->|Dubbo<br/>getAllUserInfoForSync| User
    
    Search -->|索引读写| ES
    Search -->|分布式锁| Redis
    Search -->|服务发现| Nacos
    
    API -->|服务注册| Nacos
    App -->|服务注册| Nacos
    User -->|服务注册| Nacos
```

### Dubbo 接口依赖

| 源服务 | 接口 | 方法 | 说明 |
|--------|------|------|------|
| API 平台服务 | `ApiPlatformDubboService` | `getAllApiInfoForSync(tenantId)` | 获取全量 API 数据 |
| API 平台服务 | `ApiPlatformDubboService` | `getApiInfoUpdatedAfter(tenantId, time)` | 获取增量 API 数据 |
| 应用中心服务 | `AppCenterDubboService` | `getAllAppInfoForSync(tenantId)` | 获取全量应用数据 |
| 应用中心服务 | `AppCenterDubboService` | `getAppInfoUpdatedAfter(tenantId, time)` | 获取增量应用数据 |
| 用户服务 | `UserDubboService` | `getAllUserInfoForSync(tenantId)` | 获取全量用户数据 |
| 用户服务 | `UserDubboService` | `getUserInfoUpdatedAfter(tenantId, time)` | 获取增量用户数据 |

### 数据流向

```mermaid
flowchart LR
    subgraph "源数据"
        A1[(api_info<br/>MySQL)]
        A2[(app_info<br/>MySQL)]
        A3[(user_info<br/>MySQL)]
    end
    
    subgraph "Dubbo 接口"
        B1[ApiPlatformDubboService]
        B2[AppCenterDubboService]
        B3[UserDubboService]
    end
    
    subgraph "同步任务"
        C1[ApiSyncTask]
        C2[AppSyncTask]
        C3[UserSyncTask]
    end
    
    subgraph "ES 索引"
        D1[(intellihub_api)]
        D2[(intellihub_app)]
        D3[(intellihub_user)]
    end
    
    A1 --> B1
    A2 --> B2
    A3 --> B3
    
    B1 -->|DTO| C1
    B2 -->|DTO| C2
    B3 -->|DTO| C3
    
    C1 -->|ApiDoc| D1
    C2 -->|AppDoc| D2
    C3 -->|UserDoc| D3
```

---

## 核心概念

### 搜索类型

```mermaid
mindmap
  root((搜索类型))
    API
      api_info
      名称/路径/描述
    APP
      app_info
      名称/描述/AppKey
    USER
      user_info
      用户名/昵称/邮箱
    AUDIT
      审计日志
      TODO
    ALERT
      告警记录
      TODO
```

### 索引文档

| 文档类型 | 索引名称 | 主要字段 |
|----------|----------|----------|
| `ApiDoc` | `intellihub_api` | name, code, path, method, description, status |
| `AppDoc` | `intellihub_app` | name, code, description, appKey, status |
| `UserDoc` | `intellihub_user` | username, nickname, email, phone, status |

### 聚合搜索响应

```java
public class AggregateSearchResponse {
    private Long total;           // 总命中数
    private Integer page;         // 当前页
    private Integer size;         // 每页大小
    private Integer totalPages;   // 总页数
    private Long took;            // 耗时(ms)
    private List<SearchItem> items;  // 搜索结果
    private Map<String, Map<String, Long>> facets;  // 分面统计
}

public class SearchItem {
    private String type;          // 资源类型
    private String id;            // 资源ID
    private String name;          // 名称
    private String description;   // 描述
    private Double score;         // 相关性得分
    private Map<String, List<String>> highlights;  // 高亮
    private Object data;          // 原始文档
}
```

---

## 搜索功能

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/search/aggregate` | 聚合搜索（多类型） |
| GET | `/v1/search/api` | 搜索 API |
| GET | `/v1/search/api/{id}` | 获取 API 详情 |
| POST | `/v1/search/api/index` | 手动索引 API |
| DELETE | `/v1/search/api/{id}` | 删除 API 索引 |

### 聚合搜索流程

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Controller as SearchController
    participant Service as AggregateSearchService
    participant ApiIndex as ApiIndexService
    participant AppIndex as AppIndexService
    participant UserIndex as UserIndexService
    participant ES as Elasticsearch
    
    Client->>Controller: POST /aggregate
    Controller->>Service: aggregateSearch(request)
    
    par 并行搜索各类型
        Service->>ApiIndex: searchApis()
        ApiIndex->>ES: search(intellihub_api)
        ES-->>ApiIndex: SearchResponse<ApiDoc>
    and
        Service->>AppIndex: searchApps()
        AppIndex->>ES: search(intellihub_app)
        ES-->>AppIndex: SearchResponse<AppDoc>
    and
        Service->>UserIndex: searchUsers()
        UserIndex->>ES: search(intellihub_user)
        ES-->>UserIndex: SearchResponse<UserDoc>
    end
    
    Service->>Service: 合并结果
    Service->>Service: 按得分排序
    Service->>Service: 构建分面统计
    
    Service-->>Controller: AggregateSearchResponse
    Controller-->>Client: ApiResponse
```

### 搜索请求示例

```json
{
  "keyword": "用户管理",
  "types": ["api", "app"],
  "filters": {
    "status": "published"
  },
  "page": 1,
  "size": 20,
  "highlight": true
}
```

### 搜索响应示例

```json
{
  "code": 0,
  "data": {
    "total": 15,
    "page": 1,
    "size": 20,
    "totalPages": 1,
    "took": 45,
    "items": [
      {
        "type": "api",
        "id": "api-123",
        "name": "用户管理接口",
        "description": "提供用户增删改查功能",
        "score": 12.5,
        "highlights": {
          "name": ["<em>用户管理</em>接口"],
          "description": ["提供<em>用户</em>增删改查功能"]
        },
        "data": { ... }
      }
    ],
    "facets": {
      "types": {
        "api": 10,
        "app": 5,
        "user": 0
      }
    }
  }
}
```

---

## 数据同步

### 同步模式

| 模式 | 说明 | 调度规则 |
|------|------|----------|
| 全量同步 | 从源服务拉取全部数据重建索引 | 每天凌晨2点 |
| 增量同步 | 只同步上次同步后更新的数据 | 每5分钟 |

### 同步管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/search/sync/full` | 手动触发全量同步（所有类型） |
| POST | `/v1/search/sync/api/full` | 手动触发 API 全量同步 |
| POST | `/v1/search/sync/api/incremental` | 手动触发 API 增量同步 |
| POST | `/v1/search/sync/app/full` | 手动触发应用全量同步 |
| POST | `/v1/search/sync/user/full` | 手动触发用户全量同步 |

### 同步流程

```mermaid
flowchart TD
    A[定时任务触发] --> B{获取分布式锁}
    
    B -->|失败| C[跳过执行]
    B -->|成功| D{同步模式}
    
    D -->|全量| E[调用 Dubbo 获取全部数据]
    D -->|增量| F{lastSyncTime?}
    
    F -->|null| E
    F -->|有值| G[调用 Dubbo 获取增量数据]
    
    E --> H[转换为索引文档]
    G --> H
    
    H --> I[批量写入 ES]
    I --> J[更新 lastSyncTime]
    J --> K[释放分布式锁]
```

---

## 定时任务架构

### 定时任务总览

```mermaid
flowchart TB
    subgraph "搜索服务定时任务"
        subgraph "API同步 ApiSyncTask"
            A1["fullSync<br/>每天凌晨2点"]
            A2["incrementalSync<br/>每5分钟"]
        end
        
        subgraph "应用同步 AppSyncTask"
            B1["fullSync<br/>每天凌晨2点"]
            B2["incrementalSync<br/>每5分钟"]
        end
        
        subgraph "用户同步 UserSyncTask"
            C1["fullSync<br/>每天凌晨2点"]
            C2["incrementalSync<br/>每5分钟"]
        end
    end
    
    subgraph "分布式锁 Redis"
        L1[api_full_sync_lock]
        L2[api_incremental_sync_lock]
        L3[app_full_sync_lock]
        L4[app_incremental_sync_lock]
        L5[user_full_sync_lock]
        L6[user_incremental_sync_lock]
    end
    
    subgraph "数据源 Dubbo"
        D1[ApiPlatformDubboService]
        D2[AppCenterDubboService]
        D3[UserDubboService]
    end
    
    subgraph "ES索引"
        E1[(intellihub_api)]
        E2[(intellihub_app)]
        E3[(intellihub_user)]
    end
    
    A1 --> L1
    A2 --> L2
    B1 --> L3
    B2 --> L4
    C1 --> L5
    C2 --> L6
    
    A1 --> D1
    A2 --> D1
    B1 --> D2
    B2 --> D2
    C1 --> D3
    C2 --> D3
    
    D1 --> E1
    D2 --> E2
    D3 --> E3
```

### 定时任务详情

| 任务类 | 方法 | 调度规则 | 锁超时 | 说明 |
|--------|------|----------|--------|------|
| `ApiSyncTask` | `fullSync` | `0 0 2 * * ?` | 600s | API 全量同步 |
| `ApiSyncTask` | `incrementalSync` | `0 */5 * * * ?` | 120s | API 增量同步 |
| `AppSyncTask` | `fullSync` | `0 0 2 * * ?` | 600s | 应用全量同步 |
| `AppSyncTask` | `incrementalSync` | `0 */5 * * * ?` | 120s | 应用增量同步 |
| `UserSyncTask` | `fullSync` | `0 0 2 * * ?` | 600s | 用户全量同步 |
| `UserSyncTask` | `incrementalSync` | `0 */5 * * * ?` | 120s | 用户增量同步 |

### 分布式锁机制

```mermaid
sequenceDiagram
    participant Task as SyncTask
    participant Redis as Redis
    participant Dubbo as Dubbo服务
    participant ES as Elasticsearch
    
    Task->>Redis: tryLock(lockKey, timeout)
    
    alt 获取锁成功
        Redis-->>Task: true
        Task->>Dubbo: 获取数据
        Dubbo-->>Task: List<DTO>
        Task->>Task: 转换为Doc
        Task->>ES: bulkIndex(docs)
        ES-->>Task: 索引成功
        Task->>Task: 更新 lastSyncTime
        Task->>Redis: unlock(lockKey)
    else 获取锁失败
        Redis-->>Task: false
        Task->>Task: 跳过执行
    end
```

---

## 索引设计

### 索引命名规范

| 索引 | 名称 | 说明 |
|------|------|------|
| API 索引 | `intellihub_api` | 存储 API 信息 |
| 应用索引 | `intellihub_app` | 存储应用信息 |
| 用户索引 | `intellihub_user` | 存储用户信息 |

### API 索引 Mapping

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "tenantId": { "type": "keyword" },
      "name": { 
        "type": "text", 
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      },
      "code": { "type": "keyword" },
      "path": { 
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "method": { "type": "keyword" },
      "description": { 
        "type": "text", 
        "analyzer": "ik_max_word" 
      },
      "groupId": { "type": "keyword" },
      "groupName": { "type": "keyword" },
      "status": { "type": "keyword" },
      "authType": { "type": "keyword" },
      "version": { "type": "keyword" },
      "createdBy": { "type": "keyword" },
      "creatorName": { "type": "keyword" },
      "publishedAt": { "type": "date" },
      "createdAt": { "type": "date" },
      "updatedAt": { "type": "date" }
    }
  },
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

---

## 配置说明

### Elasticsearch 配置

```yaml
intellihub:
  elasticsearch:
    enabled: true
    hosts:
      - 192.168.200.130:9200
    username:
    password:
    connect-timeout: 5s
    socket-timeout: 30s
    max-connections: 100
    index-prefix: intellihub
    default-shards: 1
    default-replicas: 0
```

### 同步任务配置

```yaml
intellihub:
  search:
    sync:
      full-cron: "0 0 2 * * ?"           # API全量同步
      incremental-cron: "0 */5 * * * ?"  # API增量同步
      app-full-cron: "0 0 2 * * ?"       # 应用全量同步
      app-incremental-cron: "0 */5 * * * ?"
      user-full-cron: "0 0 2 * * ?"      # 用户全量同步
      user-incremental-cron: "0 */5 * * * ?"
```

### Dubbo 配置

```yaml
dubbo:
  application:
    name: intelli-search-service
  consumer:
    group: intellihub
    check: false
    timeout: 30000
```

---

## 常见问题

### Q1: 搜索结果为空？

**可能原因**：
1. 数据未同步到 ES
2. ES 索引不存在
3. 租户ID不匹配

**排查**：
1. 检查同步任务日志
2. 检查 ES 索引是否存在
3. 确认请求头中的 `X-Tenant-Id`

### Q2: 同步任务不执行？

**可能原因**：
1. 分布式锁被占用
2. Dubbo 服务不可用
3. Cron 表达式配置错误

**排查**：
1. 检查 Redis 中的锁状态
2. 检查 Dubbo 服务注册状态
3. 验证 Cron 表达式

### Q3: 搜索性能慢？

**优化建议**：
1. 调整 ES 分片数
2. 使用更精确的查询条件
3. 减少返回字段
4. 开启 ES 查询缓存

### Q4: 增量同步数据丢失？

**原因**：`lastSyncTime` 在内存中，服务重启后丢失。

**解决**：首次增量同步会自动触发全量同步。

### Q5: 如何添加新的搜索类型？

1. 创建新的 `XxxDoc` 文档类
2. 创建 `XxxIndexService` 索引服务
3. 创建 `XxxSyncTask` 同步任务
4. 在 `AggregateSearchService` 中添加搜索逻辑
5. 在 `SearchType` 中添加新类型

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-07 | 初始版本，实现聚合搜索、API/应用/用户索引、全量/增量同步 |
