package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.page.PageData;
import com.intellihub.governance.constant.AlertLevel;
import com.intellihub.governance.constant.AlertStatus;
import com.intellihub.governance.dto.AlertRecordDetailDTO;
import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.entity.AlertRequestDetail;
import com.intellihub.governance.entity.AlertRule;
import com.intellihub.governance.mapper.AlertRecordMapper;
import com.intellihub.governance.mapper.AlertRequestDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警记录服务
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRecordService {

    private final AlertRecordMapper alertRecordMapper;
    private final AlertRequestDetailMapper alertRequestDetailMapper;

    /**
     * 创建告警记录（带请求详情）
     */
    @Transactional
    public AlertRecord createRecordWithDetails(AlertRule rule, String apiPath, BigDecimal currentValue,
                                               String alertLevel, String alertMessage,
                                               List<Map<String, Object>> requestDetails) {
        // 创建告警记录
        AlertRecord record = createRecord(rule, apiPath, currentValue, alertLevel, alertMessage);
        
        // 保存请求详情
        if (requestDetails != null && !requestDetails.isEmpty()) {
            for (Map<String, Object> detail : requestDetails) {
                AlertRequestDetail requestDetail = new AlertRequestDetail();
                requestDetail.setAlertRecordId(record.getId());
                requestDetail.setRequestId((String) detail.get("requestId"));
                requestDetail.setApiPath((String) detail.get("apiPath"));
                requestDetail.setMethod((String) detail.get("method"));
                
                Object statusCode = detail.get("statusCode");
                if (statusCode != null) {
                    requestDetail.setStatusCode(((Number) statusCode).intValue());
                }
                
                requestDetail.setSuccess((Boolean) detail.get("success"));
                
                Object latency = detail.get("latency");
                if (latency != null) {
                    requestDetail.setLatency(((Number) latency).intValue());
                }
                
                requestDetail.setErrorMessage((String) detail.get("errorMessage"));
                requestDetail.setClientIp((String) detail.get("clientIp"));
                
                String timestamp = (String) detail.get("timestamp");
                if (timestamp != null) {
                    try {
                        requestDetail.setRequestTime(LocalDateTime.parse(timestamp));
                    } catch (Exception e) {
                        requestDetail.setRequestTime(LocalDateTime.now());
                    }
                }
                
                requestDetail.setCreatedAt(LocalDateTime.now());
                alertRequestDetailMapper.insert(requestDetail);
            }
            log.info("保存告警请求详情 - alertRecordId: {}, count: {}", record.getId(), requestDetails.size());
        }
        
        return record;
    }

    /**
     * 创建告警记录
     */
    @Transactional
    public AlertRecord createRecord(AlertRule rule, String apiPath, BigDecimal currentValue, 
                                    String alertLevel, String alertMessage) {
        AlertRecord record = new AlertRecord();
        record.setTenantId(rule.getTenantId());
        record.setRuleId(rule.getId());
        record.setRuleName(rule.getName());
        record.setApiId(rule.getApiId());
        record.setApiPath(apiPath);
        record.setAlertLevel(alertLevel);
        record.setAlertMessage(alertMessage);
        record.setCurrentValue(currentValue);
        record.setThresholdValue(rule.getThreshold());
        record.setStatus(AlertStatus.FIRING.getCode());
        record.setFiredAt(LocalDateTime.now());
        record.setNotified(false);
        record.setCreatedAt(LocalDateTime.now());
        
        alertRecordMapper.insert(record);
        log.warn("告警触发 - rule: {}, apiPath: {}, currentValue: {}, threshold: {}", 
                rule.getName(), apiPath, currentValue, rule.getThreshold());
        return record;
    }

    /**
     * 创建告警记录（直接接受AlertRecord对象）
     * <p>
     * 用于外部服务通过Dubbo调用时直接构建告警记录
     * </p>
     */
    @Transactional
    public AlertRecord createRecord(AlertRecord record) {
        if (record.getStatus() == null) {
            record.setStatus(AlertStatus.FIRING.getCode());
        }
        if (record.getFiredAt() == null) {
            record.setFiredAt(LocalDateTime.now());
        }
        if (record.getNotified() == null) {
            record.setNotified(false);
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        
        alertRecordMapper.insert(record);
        log.warn("告警触发 - name: {}, apiPath: {}, currentValue: {}, threshold: {}", 
                record.getRuleName(), record.getApiPath(), record.getCurrentValue(), record.getThresholdValue());
        return record;
    }

    /**
     * 标记告警已恢复
     */
    @Transactional
    public void resolveAlert(Long recordId) {
        AlertRecord record = alertRecordMapper.selectById(recordId);
        if (record != null && AlertStatus.FIRING.getCode().equals(record.getStatus())) {
            record.setStatus(AlertStatus.RESOLVED.getCode());
            record.setResolvedAt(LocalDateTime.now());
            alertRecordMapper.updateById(record);
            log.info("告警恢复 - id: {}, rule: {}", recordId, record.getRuleName());
        }
    }

    /**
     * 标记告警已通知
     */
    @Transactional
    public void markNotified(Long recordId) {
        AlertRecord record = alertRecordMapper.selectById(recordId);
        if (record != null) {
            record.setNotified(true);
            alertRecordMapper.updateById(record);
        }
    }

    /**
     * 批量标记已通知
     */
    @Transactional
    public void markNotified(List<Long> recordIds) {
        for (Long id : recordIds) {
            markNotified(id);
        }
    }

    /**
     * 分页查询告警记录
     */
    public PageData<AlertRecord> listRecords(String tenantId, String status, String alertLevel,
                                          Long ruleId, LocalDateTime startTime, LocalDateTime endTime,
                                          int pageNum, int pageSize) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getTenantId, tenantId);
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(AlertRecord::getStatus, status);
        }
        if (StringUtils.hasText(alertLevel)) {
            wrapper.eq(AlertRecord::getAlertLevel, alertLevel);
        }
        if (ruleId != null) {
            wrapper.eq(AlertRecord::getRuleId, ruleId);
        }
        if (startTime != null) {
            wrapper.ge(AlertRecord::getFiredAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AlertRecord::getFiredAt, endTime);
        }
        
        wrapper.orderByDesc(AlertRecord::getFiredAt);
        
        Page<AlertRecord> page = alertRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageData.of(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    /**
     * 获取未通知的告警记录
     */
    public List<AlertRecord> getUnnotifiedRecords() {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getNotified, false)
               .eq(AlertRecord::getStatus, AlertStatus.FIRING.getCode());
        return alertRecordMapper.selectList(wrapper);
    }

    /**
     * 获取告警详情（包含请求详情列表）
     */
    public AlertRecordDetailDTO getAlertDetails(Long alertRecordId) {
        log.info("[告警详情] 查询告警ID: {}", alertRecordId);
        
        AlertRecord record = alertRecordMapper.selectById(alertRecordId);
        log.info("[告警详情] 查询到记录: {}", record);
        
        if (record == null) {
            log.warn("[告警详情] 告警记录不存在: {}", alertRecordId);
            return null;
        }
        
        List<AlertRequestDetail> requestDetails = alertRequestDetailMapper.selectByAlertRecordId(alertRecordId);
        log.info("[告警详情] 查询到请求详情数量: {}", requestDetails != null ? requestDetails.size() : 0);
        
        AlertRecordDetailDTO dto = new AlertRecordDetailDTO();
        dto.setRecord(record);
        dto.setRequestDetails(requestDetails != null ? requestDetails : new java.util.ArrayList<>());
        dto.setRequestCount(requestDetails != null ? requestDetails.size() : 0);
        
        log.info("[告警详情] 返回DTO: record={}, requestCount={}", dto.getRecord() != null, dto.getRequestCount());
        return dto;
    }

    /**
     * 获取告警统计
     */
    public AlertStats getAlertStats(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getTenantId, tenantId);
        
        if (startTime != null) {
            wrapper.ge(AlertRecord::getFiredAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AlertRecord::getFiredAt, endTime);
        }
        
        List<AlertRecord> records = alertRecordMapper.selectList(wrapper);
        
        AlertStats stats = new AlertStats();
        stats.setTotal(records.size());
        stats.setFiring((int) records.stream().filter(r -> AlertStatus.FIRING.getCode().equals(r.getStatus())).count());
        stats.setResolved((int) records.stream().filter(r -> AlertStatus.RESOLVED.getCode().equals(r.getStatus())).count());
        stats.setCritical((int) records.stream().filter(r -> AlertLevel.CRITICAL.getCode().equals(r.getAlertLevel())).count());
        stats.setWarning((int) records.stream().filter(r -> AlertLevel.WARNING.getCode().equals(r.getAlertLevel())).count());
        stats.setInfo((int) records.stream().filter(r -> AlertLevel.INFO.getCode().equals(r.getAlertLevel())).count());
        
        return stats;
    }

    /**
     * 告警统计内部类
     */
    @lombok.Data
    public static class AlertStats {
        private int total;
        private int firing;
        private int resolved;
        private int critical;
        private int warning;
        private int info;
    }
}
