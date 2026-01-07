package com.intellihub.governance.dubbo;

import com.intellihub.dubbo.*;
import com.intellihub.governance.constant.AlertLevel;
import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.service.AlertRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Governance告警Dubbo服务实现
 * 供其他服务调用统一告警能力
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService
@RequiredArgsConstructor
public class GovernanceAlertDubboServiceImpl implements GovernanceAlertDubboService {

    private final AlertRecordService alertRecordService;

    @Override
    public void sendQuotaAlert(QuotaAlertRequest request) {
        log.info("[告警] 收到配额预警 - tenantId: {}, service: {}, usage: {}%", 
                request.getTenantId(), request.getServiceName(), request.getUsagePercentage());

        try {
            AlertRecord record = AlertRecord.builder()
                    .tenantId(request.getTenantId())
                    .ruleName(request.getServiceName() + " - 配额预警")
                    .apiPath(request.getServiceName())
                    .alertLevel(request.getAlertLevel())
                    .alertMessage(request.getMessage() != null ? request.getMessage() : 
                            buildQuotaAlertMessage(request))
                    .currentValue(BigDecimal.valueOf(request.getUsagePercentage()))
                    .thresholdValue(BigDecimal.valueOf(80.0)) // 默认80%阈值
                    .status("pending")
                    .firedAt(LocalDateTime.now())
                    .build();

            alertRecordService.createRecord(record);
            log.info("[告警] 配额预警记录已创建 - recordId: {}", record.getId());
        } catch (Exception e) {
            log.error("[告警] 配额预警创建失败 - tenantId: {}, error: {}", 
                    request.getTenantId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendCostAlert(CostAlertRequest request) {
        log.info("[告警] 收到成本预警 - tenantId: {}, service: {}, currentCost: {}, forecastCost: {}", 
                request.getTenantId(), request.getServiceName(), 
                request.getCurrentCost(), request.getForecastCost());

        try {
            AlertRecord record = AlertRecord.builder()
                    .tenantId(request.getTenantId())
                    .ruleName(request.getServiceName() + " - 成本预警")
                    .apiPath(request.getServiceName())
                    .alertLevel(request.getAlertLevel())
                    .alertMessage(request.getMessage() != null ? request.getMessage() : 
                            buildCostAlertMessage(request))
                    .currentValue(BigDecimal.valueOf(request.getCurrentCost()))
                    .thresholdValue(BigDecimal.valueOf(request.getThreshold()))
                    .status("pending")
                    .firedAt(LocalDateTime.now())
                    .build();

            alertRecordService.createRecord(record);
            log.info("[告警] 成本预警记录已创建 - recordId: {}", record.getId());
        } catch (Exception e) {
            log.error("[告警] 成本预警创建失败 - tenantId: {}, error: {}", 
                    request.getTenantId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendErrorAlert(ErrorAlertRequest request) {
        log.info("[告警] 收到异常告警 - tenantId: {}, service: {}, errorCount: {}, errorRate: {}%", 
                request.getTenantId(), request.getServiceName(), 
                request.getErrorCount(), request.getErrorRate());

        try {
            AlertRecord record = AlertRecord.builder()
                    .tenantId(request.getTenantId())
                    .ruleName(request.getServiceName() + " - 异常告警")
                    .apiPath(request.getServiceName())
                    .alertLevel(request.getAlertLevel())
                    .alertMessage(buildErrorAlertMessage(request))
                    .currentValue(BigDecimal.valueOf(request.getErrorRate() != null ? request.getErrorRate() : 0))
                    .thresholdValue(BigDecimal.valueOf(10.0)) // 默认10%错误率阈值
                    .status("pending")
                    .firedAt(LocalDateTime.now())
                    .build();

            alertRecordService.createRecord(record);
            log.info("[告警] 异常告警记录已创建 - recordId: {}", record.getId());
        } catch (Exception e) {
            log.error("[告警] 异常告警创建失败 - tenantId: {}, error: {}", 
                    request.getTenantId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendPerformanceAlert(PerformanceAlertRequest request) {
        log.info("[告警] 收到性能告警 - tenantId: {}, service: {}, model: {}, avgDuration: {}ms, failureRate: {}%", 
                request.getTenantId(), request.getServiceName(), request.getModelName(),
                request.getAvgDuration(), request.getFailureRate());

        try {
            AlertRecord record = AlertRecord.builder()
                    .tenantId(request.getTenantId())
                    .ruleName(request.getServiceName() + " - 性能告警")
                    .apiPath(request.getServiceName() + "/" + request.getModelName())
                    .alertLevel(request.getAlertLevel())
                    .alertMessage(request.getMessage() != null ? request.getMessage() : 
                            buildPerformanceAlertMessage(request))
                    .currentValue(BigDecimal.valueOf(request.getAvgDuration()))
                    .thresholdValue(BigDecimal.valueOf(5000.0)) // 默认5秒阈值
                    .status("pending")
                    .firedAt(LocalDateTime.now())
                    .build();

            alertRecordService.createRecord(record);
            log.info("[告警] 性能告警记录已创建 - recordId: {}", record.getId());
        } catch (Exception e) {
            log.error("[告警] 性能告警创建失败 - tenantId: {}, error: {}", 
                    request.getTenantId(), e.getMessage(), e);
        }
    }

    @Override
    public void sendGenericAlert(GenericAlertRequest request) {
        log.info("[告警] 收到通用告警 - tenantId: {}, service: {}, type: {}, level: {}", 
                request.getTenantId(), request.getServiceName(), 
                request.getAlertType(), request.getAlertLevel());

        try {
            AlertRecord record = AlertRecord.builder()
                    .tenantId(request.getTenantId())
                    .ruleName(request.getTitle() != null ? request.getTitle() : 
                            request.getServiceName() + " - " + request.getAlertType())
                    .apiPath(request.getServiceName())
                    .alertLevel(request.getAlertLevel())
                    .alertMessage(request.getMessage())
                    .currentValue(request.getCurrentValue() != null ? 
                            new BigDecimal(request.getCurrentValue()) : null)
                    .thresholdValue(request.getThresholdValue() != null ? 
                            new BigDecimal(request.getThresholdValue()) : null)
                    .status("pending")
                    .firedAt(LocalDateTime.now())
                    .build();

            alertRecordService.createRecord(record);
            log.info("[告警] 通用告警记录已创建 - recordId: {}", record.getId());
        } catch (Exception e) {
            log.error("[告警] 通用告警创建失败 - tenantId: {}, error: {}", 
                    request.getTenantId(), e.getMessage(), e);
        }
    }

    /**
     * 构建配额告警消息
     */
    private String buildQuotaAlertMessage(QuotaAlertRequest request) {
        return String.format("配额使用率已达到 %.2f%%（已使用 %d / 总额 %d）",
                request.getUsagePercentage(),
                request.getUsedQuota(),
                request.getTotalQuota());
    }

    /**
     * 构建成本告警消息
     */
    private String buildCostAlertMessage(CostAlertRequest request) {
        return String.format("成本预警：当前成本 %.2f 元，预测本月成本 %.2f 元，超过阈值 %.2f 元",
                request.getCurrentCost(),
                request.getForecastCost(),
                request.getThreshold());
    }

    /**
     * 构建异常告警消息
     */
    private String buildErrorAlertMessage(ErrorAlertRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("异常告警：错误次数 %d 次", request.getErrorCount()));
        if (request.getErrorRate() != null) {
            sb.append(String.format("，错误率 %.2f%%", request.getErrorRate()));
        }
        sb.append("\n错误消息：").append(request.getErrorMessage());
        return sb.toString();
    }

    /**
     * 构建性能告警消息
     */
    private String buildPerformanceAlertMessage(PerformanceAlertRequest request) {
        return String.format("性能告警：模型 %s 平均耗时 %.2f ms，失败率 %.2f%%（总请求 %d 次，失败 %d 次）",
                request.getModelName(),
                request.getAvgDuration(),
                request.getFailureRate(),
                request.getTotalRequests(),
                request.getFailureCount());
    }
}
