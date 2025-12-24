package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.entity.AlertRule;
import com.intellihub.governance.mapper.AlertRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        record.setStatus("firing");
        record.setFiredAt(LocalDateTime.now());
        record.setNotified(false);
        record.setCreatedAt(LocalDateTime.now());
        
        alertRecordMapper.insert(record);
        log.warn("告警触发 - rule: {}, apiPath: {}, currentValue: {}, threshold: {}", 
                rule.getName(), apiPath, currentValue, rule.getThreshold());
        return record;
    }

    /**
     * 标记告警已恢复
     */
    @Transactional
    public void resolveAlert(Long recordId) {
        AlertRecord record = alertRecordMapper.selectById(recordId);
        if (record != null && "firing".equals(record.getStatus())) {
            record.setStatus("resolved");
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
    public IPage<AlertRecord> listRecords(String tenantId, String status, String alertLevel,
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
        
        return alertRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取未通知的告警记录
     */
    public List<AlertRecord> getUnnotifiedRecords() {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getNotified, false)
               .eq(AlertRecord::getStatus, "firing");
        return alertRecordMapper.selectList(wrapper);
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
        stats.setFiring((int) records.stream().filter(r -> "firing".equals(r.getStatus())).count());
        stats.setResolved((int) records.stream().filter(r -> "resolved".equals(r.getStatus())).count());
        stats.setCritical((int) records.stream().filter(r -> "critical".equals(r.getAlertLevel())).count());
        stats.setWarning((int) records.stream().filter(r -> "warning".equals(r.getAlertLevel())).count());
        stats.setInfo((int) records.stream().filter(r -> "info".equals(r.getAlertLevel())).count());
        
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
