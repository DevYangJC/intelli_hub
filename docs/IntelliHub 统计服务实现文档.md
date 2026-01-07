# IntelliHub 统计服务实现文档

## 目录

1. [概述](#概述)
2. [整体架构](#整体架构)
3. [核心概念](#核心概念)
4. [数据采集](#数据采集)
5. [统计查询](#统计查询)
6. [实时统计](#实时统计)
7. [数据模型](#数据模型)
8. [Redis Key 设计](#redis-key-设计)
9. [定时任务架构](#定时任务架构)
10. [配置说明](#配置说明)
11. [常见问题](#常见问题)

---

## 概述

统计服务是 IntelliHub 的**API 调用监控和数据分析中心**，负责采集、存储和查询 API 调用数据，为运维监控和告警提供数据支撑。

### 核心能力

| 能力 | 说明 |
|------|------|
| 日志采集 | 从 Kafka 消费网关上报的调用日志 |
| 实时统计 | 基于 Redis 的实时 QPS、错误率、延迟统计 |
| 历史统计 | 小时/天维度的预聚合统计数据 |
| 趋势分析 | 调用量、成功率、延迟趋势图表 |
| Top 排行 | 热门 API 排行榜 |
| 分布统计 | 状态码分布、响应时间分布 |

### 服务信息

| 项目 | 值 |
|------|-----|
| 服务名称 | intelli-governance-service |
| 端口 | 8083 |
| 数据库 | intelli_hub_governance |
| Kafka Topic | `call-log` |

---

## 整体架构

### 系统架构图

```mermaid
graph TB
    subgraph "数据源"
        Gateway[API网关]
    end
    
    subgraph "消息队列"
        Kafka[(Kafka<br/>call-log topic)]
    end
    
    subgraph "治理服务 intelli-governance-service"
        subgraph "数据采集层"
            Consumer[CallLogConsumer<br/>日志消费者]
        end
        
        subgraph "服务层"
            CallLogService[CallLogService<br/>日志服务]
            StatsService[StatsService<br/>统计服务]
        end
        
        subgraph "控制器层"
            StatsController[StatsController<br/>统计查询]
            CallLogController[CallLogController<br/>日志查询]
        end
    end
    
    subgraph "数据存储"
        MySQL[(MySQL<br/>调用日志<br/>预聚合统计)]
        Redis[(Redis<br/>实时统计)]
    end
    
    subgraph "前端"
        Dashboard[监控仪表盘]
    end
    
    Gateway -->|上报日志| Kafka
    Kafka -->|消费| Consumer
    Consumer --> CallLogService
    CallLogService -->|保存日志| MySQL
    CallLogService -->|更新统计| Redis
    
    StatsController --> StatsService
    StatsService -->|查询历史| MySQL
    StatsService -->|查询实时| Redis
    
    Dashboard -->|HTTP| StatsController
    Dashboard -->|HTTP| CallLogController
```

### 数据流向

```mermaid
flowchart LR
    A[网关请求] --> B[AccessLogFilter]
    B --> C[Kafka]
    C --> D[CallLogConsumer]
    D --> E{数据处理}
    
    E --> F[MySQL<br/>调用日志表]
    E --> G[Redis<br/>实时统计]
    
    F --> H[定时任务<br/>预聚合]
    H --> I[MySQL<br/>小时统计表]
    H --> J[MySQL<br/>天统计表]
    
    G --> K[告警检测]
    I --> L[趋势查询]
    J --> L
```

---

## 核心概念

### 调用日志 (ApiCallLog)

记录每次 API 调用的详细信息。

| 字段 | 说明 |
|------|------|
| `apiPath` | API 路径 |
| `apiMethod` | 请求方法 |
| `appId/appKey` | 调用方应用 |
| `statusCode` | HTTP 状态码 |
| `success` | 是否成功 |
| `latency` | 响应时间 (ms) |
| `requestTime` | 请求时间 |
| `clientIp` | 客户端 IP |

### 统计维度

```mermaid
mindmap
  root((统计维度))
    时间维度
      实时 Redis
      小时 api_call_stats_hourly
      天 api_call_stats_daily
    空间维度
      全局统计
      单API统计
      单应用统计
    指标维度
      调用量 totalCount
      成功数 successCount
      失败数 failCount
      平均延迟 avgLatency
      成功率 successRate
```

### 统计概览 (StatsOverviewDTO)

| 指标 | 说明 |
|------|------|
| `todayTotalCount` | 今日总调用量 |
| `todaySuccessCount` | 今日成功数 |
| `todayFailCount` | 今日失败数 |
| `todaySuccessRate` | 今日成功率 |
| `todayAvgLatency` | 今日平均延迟 |
| `yesterdayTotalCount` | 昨日总调用量 |
| `dayOverDayRate` | 日环比增长率 |
| `currentQps` | 当前 QPS |

---

## 数据采集

### Kafka 消费流程

```mermaid
sequenceDiagram
    participant Gateway as API网关
    participant Kafka as Kafka
    participant Consumer as CallLogConsumer
    participant Service as CallLogService
    participant MySQL as MySQL
    participant Redis as Redis
    
    Gateway->>Kafka: 发送调用日志
    Kafka->>Consumer: 消费消息
    Consumer->>Consumer: JSON解析为DTO
    Consumer->>Service: saveCallLog(dto)
    
    par 并行处理
        Service->>MySQL: 保存日志记录
    and
        Service->>Redis: 更新实时统计
    end
```

### 日志消费者

**核心代码**：

```java
@KafkaListener(topics = KafkaTopics.CALL_LOG, groupId = "governance-call-log-group")
public void consumeCallLog(ConsumerRecord<String, String> record) {
    // 解析日志数据
    Map<String, Object> logData = objectMapper.readValue(record.value(), Map.class);
    
    // 转换为DTO
    CallLogDTO dto = convertToDTO(logData);
    
    // 保存调用日志
    callLogService.saveCallLog(dto);
}
```

### 日志消息格式

```json
{
  "tenantId": "tenant-001",
  "apiId": "api-123",
  "apiPath": "/api/user/list",
  "apiMethod": "GET",
  "appId": "app-456",
  "appKey": "ak_xxxxxx",
  "clientIp": "192.168.1.100",
  "statusCode": 200,
  "success": true,
  "latency": 45,
  "requestTime": "2025-01-07T10:30:00",
  "userAgent": "Mozilla/5.0..."
}
```

---

## 统计查询

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/governance/v1/stats/overview` | 统计概览 |
| GET | `/governance/v1/stats/trend/hourly` | 小时趋势 |
| GET | `/governance/v1/stats/trend/daily` | 天趋势 |
| GET | `/governance/v1/stats/api/{apiPath}` | 单 API 趋势 |
| GET | `/governance/v1/stats/top` | Top N API |
| GET | `/governance/v1/stats/logs` | 调用日志分页 |
| GET | `/governance/v1/stats/realtime` | 实时调用数 |
| GET | `/governance/v1/stats/api-detail/{apiId}` | API 统计详情 |

### 统计概览查询

```mermaid
flowchart TD
    A[getOverview] --> B[查询今日统计]
    B --> C[从 daily 表聚合]
    
    A --> D[查询昨日统计]
    D --> E[从 daily 表聚合]
    
    A --> F[计算环比]
    F --> G[dayOverDayRate]
    
    A --> H[获取当前 QPS]
    H --> I[从 Redis 获取]
    
    C --> J[构建 DTO]
    E --> J
    G --> J
    I --> J
```

### 趋势查询

**小时趋势**：

```java
public StatsTrendDTO getHourlyTrend(LocalDateTime startTime, LocalDateTime endTime) {
    List<ApiCallStatsHourly> stats = hourlyMapper.selectTrend(tenantId, startTime, endTime);
    return buildTrendDTO(stats);
}
```

**返回数据结构**：

```json
{
  "timePoints": ["01-07 10:00", "01-07 11:00", "01-07 12:00"],
  "totalCounts": [1500, 1800, 2100],
  "successCounts": [1450, 1750, 2050],
  "failCounts": [50, 50, 50],
  "avgLatencies": [42, 45, 48],
  "successRates": [96.67, 97.22, 97.62]
}
```

### API 统计详情

包含更丰富的统计信息：

| 指标 | 说明 |
|------|------|
| `todayCalls` | 今日调用量 |
| `totalCalls` | 总调用量 |
| `avgLatency` | 平均延迟 |
| `successRate` | 成功率 |
| `todayTrend` | 调用量环比 |
| `latencyTrend` | 延迟环比 |
| `statusDistribution` | 状态码分布 |
| `latencyDistribution` | 响应时间分布 |

---

## 实时统计

### Redis 数据结构

```mermaid
flowchart TD
    subgraph "实时统计 Hash"
        A["alert:stats:{tenantId}:{hour}"]
        A1["totalCount: 1500"]
        A2["successCount: 1450"]
        A3["failCount: 50"]
        A4["latencySum: 67500"]
    end
    
    subgraph "QPS 计数 String"
        B["alert:qps:{tenantId}:{minute}"]
        B1["value: 150"]
    end
    
    subgraph "请求详情 List"
        C["alert:requests:{tenantId}:{hour}"]
        C1["{requestJson1}"]
        C2["{requestJson2}"]
        C3["..."]
    end
```

### QPS 计算

使用**固定窗口**算法：

```java
public double getQps() {
    // 获取上一分钟的时间标识
    LocalDateTime lastMinute = LocalDateTime.now().minusMinutes(1);
    String minute = lastMinute.format(MINUTE_FORMATTER);
    String qpsKey = RedisKeyConstants.buildQpsKey(tenantId, minute);
    
    // 获取上一分钟的请求数
    String countStr = stringRedisTemplate.opsForValue().get(qpsKey);
    long count = countStr != null ? Long.parseLong(countStr) : 0;
    
    // QPS = 请求数 / 60秒
    return count / 60.0;
}
```

### 实时统计查询

```java
public Map<String, Object> getRealtimeStats(String apiId) {
    String hour = LocalDateTime.now().format(HOUR_FORMATTER);
    String statsKey = RedisKeyConstants.buildAlertStatsKey(tenantId, hour);
    
    // 从 Hash 获取所有字段
    Map<Object, Object> hashData = stringRedisTemplate.opsForHash().entries(statsKey);
    
    // 计算指标
    long totalCount = parseLong(hashData.get("totalCount"));
    long failCount = parseLong(hashData.get("failCount"));
    long latencySum = parseLong(hashData.get("latencySum"));
    
    // 计算错误率和平均延迟
    double errorRate = totalCount > 0 ? (failCount * 100.0 / totalCount) : 0.0;
    int avgLatency = totalCount > 0 ? (int) (latencySum / totalCount) : 0;
    
    return Map.of(
        "totalCount", totalCount,
        "failCount", failCount,
        "errorRate", errorRate,
        "avgLatency", avgLatency
    );
}
```

---

## 数据模型

### E-R 图

```mermaid
erDiagram
    API_CALL_LOG {
        bigint id PK
        string tenant_id
        string api_id
        string api_path
        string api_method
        string app_id
        string app_key
        string client_ip
        int status_code
        boolean success
        int latency
        datetime request_time
        string error_message
    }
    
    API_CALL_STATS_HOURLY {
        bigint id PK
        string tenant_id
        string api_id
        string api_path
        datetime stat_time
        bigint total_count
        bigint success_count
        bigint fail_count
        int avg_latency
    }
    
    API_CALL_STATS_DAILY {
        bigint id PK
        string tenant_id
        string api_id
        string api_path
        date stat_date
        bigint total_count
        bigint success_count
        bigint fail_count
        int avg_latency
    }
```

### 建表语句

```sql
-- 调用日志表（分区表，按月分区）
CREATE TABLE api_call_log (
    id BIGINT PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    api_id VARCHAR(36),
    api_path VARCHAR(200) NOT NULL,
    api_method VARCHAR(10),
    app_id VARCHAR(36),
    app_key VARCHAR(50),
    client_ip VARCHAR(50),
    status_code INT,
    success TINYINT,
    latency INT COMMENT '响应时间(ms)',
    request_time DATETIME NOT NULL,
    error_message TEXT,
    user_agent VARCHAR(500),
    request_body TEXT,
    response_body TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_time (tenant_id, request_time),
    INDEX idx_api_path (api_path),
    INDEX idx_app_id (app_id)
) PARTITION BY RANGE (TO_DAYS(request_time)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01'))
);

-- 小时统计预聚合表
CREATE TABLE api_call_stats_hourly (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(36) NOT NULL,
    api_id VARCHAR(36),
    api_path VARCHAR(200),
    stat_time DATETIME NOT NULL COMMENT '统计时间（精确到小时）',
    total_count BIGINT DEFAULT 0,
    success_count BIGINT DEFAULT 0,
    fail_count BIGINT DEFAULT 0,
    avg_latency INT DEFAULT 0 COMMENT '平均响应时间(ms)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_api_time (tenant_id, api_path, stat_time),
    INDEX idx_stat_time (stat_time)
);

-- 天统计预聚合表
CREATE TABLE api_call_stats_daily (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(36) NOT NULL,
    api_id VARCHAR(36),
    api_path VARCHAR(200),
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_count BIGINT DEFAULT 0,
    success_count BIGINT DEFAULT 0,
    fail_count BIGINT DEFAULT 0,
    avg_latency INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_api_date (tenant_id, api_path, stat_date),
    INDEX idx_stat_date (stat_date)
);
```

---

## Redis Key 设计

### Key 命名规范

| Key 模式 | 类型 | TTL | 说明 |
|----------|------|-----|------|
| `alert:stats:{tenantId}:{hour}` | Hash | 2h | 小时级实时统计 |
| `alert:qps:{tenantId}:{minute}` | String | 2min | 分钟级请求计数 |
| `alert:requests:{tenantId}:{hour}` | List | 2h | 请求详情（供告警） |

### Key 示例

```
# 实时统计 Hash
alert:stats:tenant-001:2025010710
  - totalCount: 1500
  - successCount: 1450
  - failCount: 50
  - latencySum: 67500

# QPS 计数
alert:qps:tenant-001:202501071030 = "150"

# 请求详情列表
alert:requests:tenant-001:2025010710
  [0] = "{\"apiPath\":\"/api/user\",\"latency\":45,\"success\":true...}"
  [1] = "{\"apiPath\":\"/api/order\",\"latency\":120,\"success\":false...}"
```

---

## 定时任务架构

### 定时任务总览

```mermaid
flowchart TB
    subgraph "治理服务定时任务"
        subgraph "统计聚合任务 StatsAggregationJob"
            A1["aggregateHourlyStats<br/>每小时整点后5分钟"]
            A2["aggregateDailyStats<br/>每天凌晨0:10"]
            A3["aggregateOnStartup<br/>服务启动时"]
        end
        
        subgraph "调用次数同步任务 StatsCallCountSyncJob"
            B1["syncCallCounts<br/>每5分钟"]
            B2["syncOnStartup<br/>服务启动30秒后"]
        end
        
        subgraph "告警任务"
            C1["AlertDetectionJob<br/>每分钟检测"]
            C2["AlertNotifyJob<br/>每30秒通知"]
        end
    end
    
    subgraph "数据源"
        D1[(api_call_log<br/>调用日志表)]
    end
    
    subgraph "聚合目标"
        E1[(api_call_stats_hourly<br/>小时统计)]
        E2[(api_call_stats_daily<br/>天统计)]
        E3[(api_call_stats_distribution<br/>分布统计)]
    end
    
    subgraph "跨服务同步 Dubbo"
        F1[API平台服务<br/>api_info.todayCalls<br/>api_info.totalCalls]
        F2[应用中心服务<br/>app_info.quotaUsed]
    end
    
    A1 --> D1
    A2 --> D1
    A3 --> D1
    
    D1 --> E1
    D1 --> E2
    D1 --> E3
    
    B1 --> D1
    B1 -->|Dubbo| F1
    B1 -->|Dubbo| F2
```

### 定时任务详情

| 任务类 | 方法 | 调度规则 | 说明 |
|--------|------|----------|------|
| `StatsAggregationJob` | `aggregateHourlyStats` | `0 5 * * * ?` | 每小时整点后5分钟，聚合上一小时数据 |
| `StatsAggregationJob` | `aggregateDailyStats` | `0 10 0 * * ?` | 每天凌晨0:10，聚合昨日数据 |
| `StatsAggregationJob` | `aggregateOnStartup` | 启动时执行一次 | 补充今日和昨日统计数据 |
| `StatsCallCountSyncJob` | `syncCallCounts` | `0 */5 * * * ?` | 每5分钟，同步调用次数到其他服务 |
| `StatsCallCountSyncJob` | `syncOnStartup` | 启动30秒后 | 首次同步调用次数 |
| `AlertDetectionJob` | `detectAlerts` | 每60秒 | 检测告警规则 |
| `AlertNotifyJob` | `notifyAlerts` | 每30秒 | 发送未通知的告警 |

---

### 统计聚合任务

**聚合流程**：

```mermaid
sequenceDiagram
    participant Job as StatsAggregationJob
    participant LogMapper as ApiCallLogMapper
    participant HourlyMapper as HourlyMapper
    participant DailyMapper as DailyMapper
    participant DistMapper as DistributionMapper
    
    Note over Job: 每小时整点后5分钟
    Job->>LogMapper: 查询上一小时日志
    LogMapper-->>Job: List<ApiCallLog>
    Job->>Job: 按 tenantId+apiPath+appId 分组
    Job->>Job: 计算 totalCount/successCount/avgLatency/P95/P99
    Job->>HourlyMapper: 插入或更新小时统计
    
    Note over Job: 每天凌晨0:10
    Job->>LogMapper: 查询昨日日志
    LogMapper-->>Job: List<ApiCallLog>
    Job->>Job: 按 tenantId+apiPath+appId 分组
    Job->>DailyMapper: 插入或更新天统计
    Job->>DistMapper: 插入或更新分布统计
```

**聚合指标**：

| 指标 | 说明 |
|------|------|
| `totalCount` | 总调用次数 |
| `successCount` | 成功次数 |
| `failCount` | 失败次数 |
| `avgLatency` | 平均延迟 |
| `maxLatency` | 最大延迟 |
| `minLatency` | 最小延迟 |
| `p95Latency` | P95 延迟 |
| `p99Latency` | P99 延迟 |

---

### 调用次数同步任务

**同步流程**：

```mermaid
sequenceDiagram
    participant Job as StatsCallCountSyncJob
    participant LogMapper as ApiCallLogMapper
    participant ApiDubbo as ApiPlatformDubboService
    participant AppDubbo as AppCenterDubboService
    
    Note over Job: 每5分钟执行
    
    rect rgb(240, 248, 255)
        Note over Job,ApiDubbo: 1. 同步 API 调用次数
        Job->>LogMapper: countTodayCallsByApiId()
        LogMapper-->>Job: 今日各API调用次数
        Job->>LogMapper: countTotalCallsByApiId()
        LogMapper-->>Job: 历史总调用次数
        Job->>Job: 构建 ApiCallCountDTO 列表
        Job->>ApiDubbo: batchUpdateApiCallCounts(list)
        ApiDubbo-->>Job: 更新成功数
    end
    
    rect rgb(255, 248, 240)
        Note over Job,AppDubbo: 2. 同步 App 配额使用
        Job->>LogMapper: countCallsByAppId()
        LogMapper-->>Job: 各App调用次数
        Job->>Job: 构建 AppCallCountDTO 列表
        Job->>AppDubbo: batchUpdateAppQuotaUsed(list)
        AppDubbo-->>Job: 更新成功数
    end
```

**同步目标**：

| 目标服务 | 目标表 | 更新字段 | 说明 |
|----------|--------|----------|------|
| API 平台服务 | `api_info` | `today_calls` | 今日调用次数 |
| API 平台服务 | `api_info` | `total_calls` | 历史总调用次数 |
| 应用中心服务 | `app_info` | `quota_used` | 已使用配额 |

**Dubbo 接口**：

```java
// API 平台服务接口
public interface ApiPlatformDubboService {
    /**
     * 批量更新 API 调用次数
     */
    int batchUpdateApiCallCounts(List<ApiCallCountDTO> callCounts);
}

// 应用中心服务接口
public interface AppCenterDubboService {
    /**
     * 批量更新 App 配额使用
     */
    int batchUpdateAppQuotaUsed(List<AppCallCountDTO> callCounts);
}
```

---

### 告警任务

**检测流程**：

```mermaid
flowchart TD
    A[AlertDetectionJob<br/>每分钟执行] --> B[查询所有 active 规则]
    B --> C{遍历规则}
    
    C --> D[从 Redis 获取实时统计]
    D --> E{计算指标值}
    
    E -->|error_rate| F[failCount/totalCount×100]
    E -->|latency| G[avgLatency]
    E -->|qps| H[上分钟请求数/60]
    
    F --> I{触发告警?}
    G --> I
    H --> I
    
    I -->|否| C
    I -->|是| J[确定告警级别]
    J --> K[创建 AlertRecord]
    K --> L[保存请求详情]
    L --> M[发布告警事件]
    M --> N[清理 Redis 数据]
    N --> C
```

**通知流程**：

```mermaid
flowchart TD
    A[AlertNotifyJob<br/>每30秒执行] --> B[查询未通知告警]
    B --> C{有未通知?}
    
    C -->|否| D[结束]
    C -->|是| E[遍历告警记录]
    
    E --> F[获取通知渠道配置]
    F --> G[NotifyChannelFactory]
    G --> H{渠道类型}
    
    H -->|email| I[EmailChannel]
    H -->|webhook| J[WebhookChannel]
    H -->|kafka| K[KafkaChannel]
    
    I --> L[发送通知]
    J --> L
    K --> L
    
    L --> M[标记已通知]
    M --> E
```

---

## 配置说明

### Kafka 配置

```yaml
spring:
  kafka:
    bootstrap-servers: 192.168.200.130:9092
    consumer:
      group-id: governance-call-log-group
      auto-offset-reset: earliest
      enable-auto-commit: true
```

### Redis 配置

```yaml
spring:
  redis:
    host: 192.168.200.130
    port: 6379
    database: 0
```

---

## 常见问题

### Q1: 统计数据与实际调用量不一致？

**可能原因**：
1. Kafka 消费延迟
2. 网关日志上报失败
3. 预聚合任务未执行

**排查**：
1. 检查 Kafka 消费者 lag
2. 检查网关日志上报配置
3. 检查定时任务执行日志

### Q2: QPS 显示为 0？

**原因**：QPS 使用上一分钟的数据计算，刚启动时可能为 0。

### Q3: 小时/天统计数据缺失？

**原因**：预聚合定时任务未执行或失败。

**解决**：手动触发聚合任务或检查定时任务配置。

### Q4: 调用日志表数据量过大？

**建议**：
1. 使用分区表，按月分区
2. 定期归档历史数据
3. 考虑使用 Elasticsearch 存储日志

### Q5: 实时统计数据丢失？

**原因**：Redis 重启或 Key 过期。

**建议**：
1. 确保 Redis 持久化配置
2. 检查 Key TTL 设置

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-07 | 初始版本，实现日志采集、实时统计、趋势查询、分布统计 |
