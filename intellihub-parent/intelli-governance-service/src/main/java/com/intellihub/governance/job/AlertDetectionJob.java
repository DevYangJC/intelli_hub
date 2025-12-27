package com.intellihub.governance.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.governance.entity.AlertRule;
import com.intellihub.governance.service.AlertRecordService;
import com.intellihub.governance.service.AlertRuleService;
import com.intellihub.governance.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 告警检测定时任务
 * <p>
 * 定期检查告警规则，根据统计数据触发告警
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertDetectionJob {

    private final AlertRuleService alertRuleService;
    private final AlertRecordService alertRecordService;
    private final StatsService statsService;
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 每分钟检测一次告警规则
     * <p>
     * 检测流程:
     * 1. 获取所有状态为 active 的告警规则
     * 2. 遍历每个规则，根据规则类型(error_rate/latency/qps)从 Redis 获取实时统计数据
     * 3. 对比当前值与阈值，判断是否触发告警
     * 4. 触发则创建告警记录
     * </p>
     */
    @Scheduled(fixedRate = 60000)
    public void detectAlerts() {
        log.info("========== [告警检测] 开始执行定时任务 ==========");
        try {
            List<AlertRule> activeRules = alertRuleService.getAllActiveRules();
            log.info("[告警检测] 查询到活跃规则数: {}", activeRules.size());
            
            if (activeRules.isEmpty()) {
                log.info("[告警检测] 没有活跃规则，跳过检测");
                return;
            }

            int triggeredCount = 0;
            for (AlertRule rule : activeRules) {
                try {
                    log.info("[告警检测] 检测规则: id={}, name={}, type={}, tenantId={}, threshold={}, operator={}",
                            rule.getId(), rule.getName(), rule.getRuleType(), 
                            rule.getTenantId(), rule.getThreshold(), rule.getOperator());
                    boolean triggered = checkRule(rule);
                    if (triggered) {
                        triggeredCount++;
                    }
                } catch (Exception e) {
                    log.error("[告警检测] 检测规则失败 - ruleId: {}, name: {}", rule.getId(), rule.getName(), e);
                }
            }
            log.info("========== [告警检测] 任务完成，触发告警数: {} ==========", triggeredCount);
        } catch (Exception e) {
            log.error("[告警检测] 任务执行失败", e);
        }
    }

    /**
     * 检查单个规则
     * <p>
     * 新流程:
     * 1. 从 Redis Hash 获取统计数据
     * 2. 计算指标值并判断是否触发告警
     * 3. 触发后获取相关请求详情
     * 4. 保存告警记录和请求详情
     * 5. 删除 Redis 数据避免重复告警
     * </p>
     * @return true 表示触发了告警
     */
    private boolean checkRule(AlertRule rule) {
        String ruleType = rule.getRuleType();
        BigDecimal threshold = rule.getThreshold();
        String operator = rule.getOperator();
        String tenantId = rule.getTenantId();
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);

        // 获取统计数据
        Map<String, Object> stats = statsService.getRealtimeStats(tenantId, null);
        
        // QPS 使用独立的分钟级 Key，不依赖小时级 stats 数据
        // 其他类型（error_rate/latency）需要 stats 数据
        if (!"qps".equals(ruleType)) {
            if (stats == null || (Long) stats.getOrDefault("totalCount", 0L) == 0) {
                log.info("[告警检测] 规则[{}] 无统计数据，跳过", rule.getName());
                return false;
            }
        }

        // 计算当前指标值
        BigDecimal currentValue = calculateMetricValue(ruleType, stats, tenantId);
        if (currentValue == null) {
            log.info("[告警检测] 规则[{}] 无法计算指标值", rule.getName());
            return false;
        }

        // 判断是否触发告警
        boolean triggered = evaluateCondition(currentValue, threshold, operator);
        log.info("[告警检测] 规则[{}] 检测结果: currentValue={}, threshold={}, operator={}, triggered={}",
                rule.getName(), currentValue, threshold, operator, triggered);

        if (triggered) {
            // 获取请求详情列表
            List<String> requestJsonList = statsService.getAlertRequests(tenantId, hour);
            log.info("[告警检测] 规则[{}] 获取到原始请求数: {}", rule.getName(), requestJsonList.size());
            
            List<Map<String, Object>> relatedRequests = filterRequestsByAlertType(ruleType, requestJsonList);
            
            String alertLevel = determineAlertLevel(currentValue, threshold, ruleType);
            String message = buildAlertMessage(rule, currentValue, ruleType);
            log.warn("[告警触发] 规则[{}] 触发告警! level={}, message={}, relatedRequests={}", 
                    rule.getName(), alertLevel, message, relatedRequests.size());
            
            // 保存告警记录和请求详情
            alertRecordService.createRecordWithDetails(rule, "global", currentValue, alertLevel, message, relatedRequests);
            
            // QPS 告警不删除 Redis 数据（因为 QPS 使用独立的分钟级 Key，且请求详情对其他告警类型仍有用）
            // 只有 error_rate 和 latency 告警才删除数据
            if (!"qps".equals(ruleType)) {
                statsService.deleteAlertData(tenantId, hour);
                log.info("[告警检测] 已删除 Redis 告警数据: tenantId={}, hour={}", tenantId, hour);
            } else {
                log.info("[告警检测] QPS 告警不删除 Redis 数据，保留给其他告警类型使用");
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * 根据规则类型计算指标值
     */
    private BigDecimal calculateMetricValue(String ruleType, Map<String, Object> stats, String tenantId) {
        long totalCount = (Long) stats.getOrDefault("totalCount", 0L);
        long failCount = (Long) stats.getOrDefault("failCount", 0L);
        int avgLatency = (Integer) stats.getOrDefault("avgLatency", 0);
        
        switch (ruleType) {
            case "error_rate":
                if (totalCount == 0) return BigDecimal.ZERO;
                return BigDecimal.valueOf(failCount * 100.0 / totalCount);
            case "latency":
                return BigDecimal.valueOf(avgLatency);
            case "qps":
                // 固定窗口：使用上一分钟的请求数 / 60 计算 QPS
                double qps = statsService.getQps(tenantId);
                log.info("[告警检测] QPS 计算（固定窗口）: tenantId={}, QPS={}/s", tenantId, qps);
                return BigDecimal.valueOf(qps);
            default:
                return null;
        }
    }
    
    /**
     * 根据告警类型过滤相关请求
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filterRequestsByAlertType(String ruleType, List<String> requestJsonList) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (String json : requestJsonList) {
            try {
                Map<String, Object> request = objectMapper.readValue(json, Map.class);
                boolean include = false;
                
                switch (ruleType) {
                    case "error_rate":
                        // 只包含失败的请求
                        include = !Boolean.TRUE.equals(request.get("success"));
                        break;
                    case "latency":
                        // 包含所有有延迟数据的请求（按延迟排序后展示）
                        Object latency = request.get("latency");
                        include = latency != null;
                        break;
                    case "qps":
                        // 包含所有请求（后续会按 API 分组统计）
                        include = true;
                        break;
                }
                
                if (include) {
                    result.add(request);
                }
            } catch (Exception e) {
                log.warn("[告警检测] 解析请求JSON失败: {}", e.getMessage());
            }
        }
        
        // 排序和限制数量
        if ("latency".equals(ruleType)) {
            result.sort((a, b) -> {
                int latencyA = a.get("latency") != null ? ((Number) a.get("latency")).intValue() : 0;
                int latencyB = b.get("latency") != null ? ((Number) b.get("latency")).intValue() : 0;
                return Integer.compare(latencyB, latencyA); // 降序
            });
        } else if ("qps".equals(ruleType)) {
            // QPS 告警：按 API 路径分组，统计每个 API 的请求数，按请求数降序排序
            Map<String, Long> apiCountMap = result.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            r -> r.get("apiPath") != null ? r.get("apiPath").toString() : "unknown",
                            java.util.stream.Collectors.counting()));
            
            // 按 API 请求数排序（高流量 API 的请求排在前面）
            result.sort((a, b) -> {
                String apiA = a.get("apiPath") != null ? a.get("apiPath").toString() : "unknown";
                String apiB = b.get("apiPath") != null ? b.get("apiPath").toString() : "unknown";
                return Long.compare(apiCountMap.getOrDefault(apiB, 0L), apiCountMap.getOrDefault(apiA, 0L));
            });
            
            // 记录 API 流量分布
            log.info("[告警检测] QPS 告警 API 流量分布: {}", apiCountMap);
        }
        
        // 最多返回100条
        if (result.size() > 100) {
            result = new ArrayList<>(result.subList(0, 100));
        }
        
        log.info("[告警检测] 过滤后的请求数: {}/{}", result.size(), requestJsonList.size());
        return result;
    }

    /**
     * 计算错误率（全局统计）
     * <p>
     * 从 Redis 获取实时统计数据，计算错误率 = failCount / totalCount * 100%
     * </p>
     * <p>
     * 使用的 Redis Key:
     * - stats:realtime:{tenantId}:global:{hour}:total - 总调用数
     * - stats:realtime:{tenantId}:global:{hour}:fail  - 失败数
     * </p>
     */
    private BigDecimal calculateErrorRate(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("[告警检测] 计算错误率 - tenantId={}", tenantId);
            
            Map<String, Object> stats = statsService.getRealtimeStats(tenantId, null);
            if (stats == null) {
                log.info("[告警检测] 获取实时统计数据为空 - tenantId={}", tenantId);
                return null;
            }
            
            Long total = (Long) stats.getOrDefault("totalCount", 0L);
            Long fail = (Long) stats.getOrDefault("failCount", 0L);
            log.info("[告警检测] Redis统计数据 - totalCount={}, failCount={}", total, fail);
            
            if (total == 0) {
                log.info("[告警检测] 总调用数为0，返回错误率0");
                return BigDecimal.ZERO;
            }
            
            BigDecimal errorRate = BigDecimal.valueOf(fail * 100.0 / total);
            log.info("[告警检测] 计算错误率 = {}%", errorRate);
            return errorRate;
        } catch (Exception e) {
            log.error("[告警检测] 计算错误率失败 - tenantId={}", tenantId, e);
            return null;
        }
    }

    /**
     * 计算平均延迟（全局统计）
     * <p>
     * 从 Redis latency 列表获取延迟数据并计算平均值
     * </p>
     * <p>
     * 使用的 Redis Key:
     * - stats:realtime:{tenantId}:global:{hour}:latency (List类型)
     * </p>
     */
    private BigDecimal calculateAvgLatency(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("[告警检测] 计算平均延迟 - tenantId={}", tenantId);
            
            Map<String, Object> stats = statsService.getRealtimeStats(tenantId, null);
            if (stats == null) {
                log.info("[告警检测] 获取实时统计数据为空");
                return null;
            }
            
            Object avgLatency = stats.get("avgLatency");
            log.info("[告警检测] avgLatency={}", avgLatency);
            
            if (avgLatency == null) {
                return null;
            }
            
            if (avgLatency instanceof Number) {
                BigDecimal result = BigDecimal.valueOf(((Number) avgLatency).doubleValue());
                log.info("[告警检测] 计算平均延迟 = {}ms", result);
                return result;
            }
            return null;
        } catch (Exception e) {
            log.error("[告警检测] 计算平均延迟失败 - tenantId={}", tenantId, e);
            return null;
        }
    }

    /**
     * 计算QPS (Queries Per Second) - 全局统计
     * <p>
     * 从 Redis 获取当前小时的总调用数，除以当前小时已过去的秒数，得到平均 QPS
     * </p>
     * <p>
     * 使用的 Redis Key:
     * - stats:realtime:{tenantId}:global:{hour}:total - 总调用数
     * </p>
     * <p>
     * 计算公式: QPS = totalCount / elapsedSeconds
     * 其中 elapsedSeconds 是当前小时已过去的秒数（最小60秒）
     * </p>
     */
    private BigDecimal calculateQps(String tenantId, LocalDateTime startTime, LocalDateTime endTime, int duration) {
        try {
            Map<String, Object> stats = statsService.getRealtimeStats(tenantId, null);
            if (stats == null) {
                log.info("[告警检测] 计算QPS - 获取实时统计数据为空, tenantId={}", tenantId);
                return null;
            }
            
            Long total = (Long) stats.getOrDefault("totalCount", 0L);
            
            // 计算当前小时已过去的秒数（最小60秒，避免除数过小导致QPS过大）
            LocalDateTime now = LocalDateTime.now();
            int elapsedSeconds = now.getMinute() * 60 + now.getSecond();
            if (elapsedSeconds < 60) {
                elapsedSeconds = 60; // 最小60秒
            }
            
            BigDecimal qps = BigDecimal.valueOf(total * 1.0 / elapsedSeconds);
            log.info("[告警检测] 计算QPS - tenantId={}, totalCount={}, elapsedSeconds={}s, QPS={}/s", 
                    tenantId, total, elapsedSeconds, qps);
            return qps;
        } catch (Exception e) {
            log.error("[告警检测] 计算QPS失败 - tenantId={}", tenantId, e);
            return null;
        }
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(BigDecimal currentValue, BigDecimal threshold, String operator) {
        if (currentValue == null || threshold == null) {
            return false;
        }
        
        int comparison = currentValue.compareTo(threshold);
        
        switch (operator) {
            case "gt":
                return comparison > 0;
            case "gte":
                return comparison >= 0;
            case "lt":
                return comparison < 0;
            case "lte":
                return comparison <= 0;
            case "eq":
                return comparison == 0;
            default:
                return false;
        }
    }

    /**
     * 确定告警级别
     */
    private String determineAlertLevel(BigDecimal currentValue, BigDecimal threshold, String ruleType) {
        double ratio = currentValue.doubleValue() / threshold.doubleValue();
        
        if (ratio >= 2.0) {
            return "critical";
        } else if (ratio >= 1.5) {
            return "warning";
        } else {
            return "info";
        }
    }

    /**
     * 构建告警消息
     */
    private String buildAlertMessage(AlertRule rule, BigDecimal currentValue, String ruleType) {
        String unit = "";
        switch (ruleType) {
            case "error_rate":
                unit = "%";
                break;
            case "latency":
                unit = "ms";
                break;
            case "qps":
                unit = "/s";
                break;
        }
        
        return String.format("[%s] %s 当前值: %.2f%s, 阈值: %.2f%s",
                rule.getName(), getRuleTypeDesc(ruleType),
                currentValue.doubleValue(), unit,
                rule.getThreshold().doubleValue(), unit);
    }

    private String getRuleTypeDesc(String ruleType) {
        switch (ruleType) {
            case "error_rate":
                return "错误率";
            case "latency":
                return "平均延迟";
            case "qps":
                return "QPS";
            default:
                return ruleType;
        }
    }
}
