package com.intellihub.aigc.task;

import com.intellihub.aigc.entity.AigcQuotaConfig;
import com.intellihub.aigc.mapper.AigcQuotaConfigMapper;
import com.intellihub.aigc.monitor.PerformanceMonitor;
import com.intellihub.aigc.service.CostAnalysisService;
import com.intellihub.dubbo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 告警定时任务
 * 通过Dubbo调用Governance统一告警服务
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduleTask {

    private final AigcQuotaConfigMapper quotaConfigMapper;
    private final CostAnalysisService costAnalysisService;
    private final PerformanceMonitor performanceMonitor;

    /**
     * 告警Dubbo服务
     */
    @DubboReference(check = false, timeout = 5000)
    private GovernanceAlertDubboService alertDubboService;

    /**
     * 配额预警检查（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkQuotaAlert() {
        if (alertDubboService == null) {
            log.warn("[告警] Governance告警服务未连接，跳过配额预警检查");
            return;
        }

        log.info("开始执行配额预警检查");

        List<AigcQuotaConfig> configs = quotaConfigMapper.selectList(null);
        
        for (AigcQuotaConfig config : configs) {
            long dailyQuota = config.getDailyQuota();
            long usedQuota = config.getUsedQuota();
            
            if (dailyQuota > 0) {
                double usagePercentage = (double) usedQuota / dailyQuota * 100;
                
                // 超过95%严重告警，超过80%普通告警
                String alertLevel = null;
                if (usagePercentage >= 95.0) {
                    alertLevel = "critical";
                } else if (usagePercentage >= 80.0) {
                    alertLevel = "warning";
                }
                
                if (alertLevel != null) {
                    QuotaAlertRequest request = QuotaAlertRequest.builder()
                            .tenantId(config.getTenantId())
                            .serviceName("AIGC服务")
                            .usagePercentage(usagePercentage)
                            .usedQuota(usedQuota)
                            .totalQuota(dailyQuota)
                            .alertLevel(alertLevel)
                            .build();
                    
                    try {
                        alertDubboService.sendQuotaAlert(request);
                        log.info("[告警] 配额预警已发送 - tenantId: {}, usage: {}%, level: {}", 
                                config.getTenantId(), usagePercentage, alertLevel);
                    } catch (Exception e) {
                        log.error("[告警] 配额预警发送失败 - tenantId: {}", config.getTenantId(), e);
                    }
                }
            }
        }

        log.info("配额预警检查完成，检查租户数: {}", configs.size());
    }

    /**
     * 成本预警检查（每天执行一次，早上9点）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkCostAlert() {
        if (alertDubboService == null) {
            log.warn("[告警] Governance告警服务未连接，跳过成本预警检查");
            return;
        }

        log.info("开始执行成本预警检查");

        List<AigcQuotaConfig> configs = quotaConfigMapper.selectList(null);
        
        for (AigcQuotaConfig config : configs) {
            String tenantId = config.getTenantId();
            
            try {
                // 获取成本预测
                Map<String, Object> forecast = costAnalysisService.getCostForecast(tenantId);
                
                BigDecimal currentCost = (BigDecimal) forecast.get("currentCost");
                BigDecimal forecastCost = (BigDecimal) forecast.get("forecastCost");
                
                // 预测成本超过阈值发送告警
                String alertLevel = null;
                double threshold = 800.0;
                if (forecastCost.doubleValue() >= 1000.0) {
                    alertLevel = "critical";
                    threshold = 1000.0;
                } else if (forecastCost.doubleValue() >= 800.0) {
                    alertLevel = "warning";
                    threshold = 800.0;
                }
                
                if (alertLevel != null) {
                    CostAlertRequest request = CostAlertRequest.builder()
                            .tenantId(tenantId)
                            .serviceName("AIGC服务")
                            .currentCost(currentCost.doubleValue())
                            .forecastCost(forecastCost.doubleValue())
                            .threshold(threshold)
                            .alertLevel(alertLevel)
                            .build();
                    
                    alertDubboService.sendCostAlert(request);
                    log.info("[告警] 成本预警已发送 - tenantId: {}, currentCost: {}, forecastCost: {}, level: {}", 
                            tenantId, currentCost, forecastCost, alertLevel);
                }
            } catch (Exception e) {
                log.error("[告警] 成本预警检查失败 - tenantId: {}", tenantId, e);
            }
        }

        log.info("成本预警检查完成");
    }

    /**
     * 性能告警检查（每30分钟执行一次）
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkPerformanceAlert() {
        if (alertDubboService == null) {
            log.warn("[告警] Governance告警服务未连接，跳过性能告警检查");
            return;
        }

        log.info("开始执行性能告警检查");

        Map<String, Map<String, Object>> allStats = performanceMonitor.getAllStats();
        
        for (Map.Entry<String, Map<String, Object>> entry : allStats.entrySet()) {
            String model = entry.getKey();
            Map<String, Object> stats = entry.getValue();
            
            long requestCount = (Long) stats.get("requestCount");
            
            // 只对请求量较大的模型进行检查
            if (requestCount >= 10) {
                try {
                    String avgTimeStr = (String) stats.get("avgTime");
                    String successRateStr = (String) stats.get("successRate");
                    
                    double avgTime = Double.parseDouble(avgTimeStr);
                    double successRate = Double.parseDouble(successRateStr);
                    double failureRate = 100.0 - successRate;
                    long failureCount = (Long) stats.get("failureCount");
                    
                    // 平均耗时超过5秒或失败率超过10%发送告警
                    String alertLevel = null;
                    if (failureRate > 10.0) {
                        alertLevel = "critical";
                    } else if (avgTime > 5000.0) {
                        alertLevel = "warning";
                    }
                    
                    if (alertLevel != null) {
                        PerformanceAlertRequest request = PerformanceAlertRequest.builder()
                                .tenantId("system")
                                .serviceName("AIGC服务")
                                .modelName(model)
                                .avgDuration(avgTime)
                                .failureRate(failureRate)
                                .totalRequests(requestCount)
                                .failureCount(failureCount)
                                .alertLevel(alertLevel)
                                .build();
                        
                        alertDubboService.sendPerformanceAlert(request);
                        log.info("[告警] 性能预警已发送 - model: {}, avgTime: {}ms, failureRate: {}%, level: {}", 
                                model, avgTime, failureRate, alertLevel);
                    }
                } catch (Exception e) {
                    log.error("[告警] 性能预警检查失败 - model: {}", model, e);
                }
            }
        }

        log.info("性能告警检查完成，检查模型数: {}", allStats.size());
    }
}
