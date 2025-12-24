package com.intellihub.governance.job;

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
import java.util.List;
import java.util.Map;

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

    /**
     * 每分钟检测一次告警规则
     */
    @Scheduled(fixedRate = 60000)
    public void detectAlerts() {
        try {
            List<AlertRule> activeRules = alertRuleService.getAllActiveRules();
            if (activeRules.isEmpty()) {
                return;
            }

            log.debug("开始告警检测，活跃规则数: {}", activeRules.size());

            for (AlertRule rule : activeRules) {
                try {
                    checkRule(rule);
                } catch (Exception e) {
                    log.error("检测告警规则失败 - ruleId: {}, name: {}", rule.getId(), rule.getName(), e);
                }
            }
        } catch (Exception e) {
            log.error("告警检测任务执行失败", e);
        }
    }

    /**
     * 检查单个规则
     */
    private void checkRule(AlertRule rule) {
        String ruleType = rule.getRuleType();
        BigDecimal threshold = rule.getThreshold();
        String operator = rule.getOperator();
        int duration = rule.getDuration() != null ? rule.getDuration() : 60;

        // 计算检测时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusSeconds(duration);

        BigDecimal currentValue = null;
        String apiPath = rule.getApiId() != null ? rule.getApiId() : "global";

        switch (ruleType) {
            case "error_rate":
                currentValue = calculateErrorRate(rule.getTenantId(), rule.getApiId(), startTime, endTime);
                break;
            case "latency":
                currentValue = calculateAvgLatency(rule.getTenantId(), rule.getApiId(), startTime, endTime);
                break;
            case "qps":
                currentValue = calculateQps(rule.getTenantId(), rule.getApiId(), startTime, endTime, duration);
                break;
            default:
                log.warn("未知的规则类型: {}", ruleType);
                return;
        }

        if (currentValue == null) {
            return;
        }

        // 判断是否触发告警
        boolean triggered = evaluateCondition(currentValue, threshold, operator);

        if (triggered) {
            String alertLevel = determineAlertLevel(currentValue, threshold, ruleType);
            String message = buildAlertMessage(rule, currentValue, ruleType);
            alertRecordService.createRecord(rule, apiPath, currentValue, alertLevel, message);
        }
    }

    /**
     * 计算错误率
     */
    private BigDecimal calculateErrorRate(String tenantId, String apiId, 
                                          LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> stats = statsService.getRealtimeStats(tenantId, apiId);
            if (stats == null) {
                return null;
            }
            
            Long total = (Long) stats.getOrDefault("totalCount", 0L);
            Long fail = (Long) stats.getOrDefault("failCount", 0L);
            
            if (total == 0) {
                return BigDecimal.ZERO;
            }
            
            return BigDecimal.valueOf(fail * 100.0 / total);
        } catch (Exception e) {
            log.debug("计算错误率失败 - tenantId: {}, apiId: {}", tenantId, apiId);
            return null;
        }
    }

    /**
     * 计算平均延迟
     */
    private BigDecimal calculateAvgLatency(String tenantId, String apiId,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> stats = statsService.getRealtimeStats(tenantId, apiId);
            if (stats == null) {
                return null;
            }
            
            Object avgLatency = stats.get("avgLatency");
            if (avgLatency == null) {
                return null;
            }
            
            if (avgLatency instanceof Number) {
                return BigDecimal.valueOf(((Number) avgLatency).doubleValue());
            }
            return null;
        } catch (Exception e) {
            log.debug("计算平均延迟失败 - tenantId: {}, apiId: {}", tenantId, apiId);
            return null;
        }
    }

    /**
     * 计算QPS
     */
    private BigDecimal calculateQps(String tenantId, String apiId,
                                    LocalDateTime startTime, LocalDateTime endTime, int duration) {
        try {
            Map<String, Object> stats = statsService.getRealtimeStats(tenantId, apiId);
            if (stats == null) {
                return null;
            }
            
            Long total = (Long) stats.getOrDefault("totalCount", 0L);
            if (duration <= 0) {
                return BigDecimal.ZERO;
            }
            
            return BigDecimal.valueOf(total * 1.0 / duration);
        } catch (Exception e) {
            log.debug("计算QPS失败 - tenantId: {}, apiId: {}", tenantId, apiId);
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
