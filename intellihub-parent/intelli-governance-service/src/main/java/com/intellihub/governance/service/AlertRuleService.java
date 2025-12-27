package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.page.PageData;
import com.intellihub.governance.dto.AlertRuleDTO;
import com.intellihub.governance.entity.AlertRule;
import com.intellihub.governance.mapper.AlertRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警规则服务
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;

    /**
     * 创建告警规则
     */
    @Transactional
    public AlertRule createRule(String tenantId, AlertRuleDTO dto, String createdBy) {
        AlertRule rule = new AlertRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setTenantId(tenantId);
        rule.setCreatedBy(createdBy);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        
        alertRuleMapper.insert(rule);
        log.info("创建告警规则成功 - tenantId: {}, name: {}", tenantId, dto.getName());
        return rule;
    }

    /**
     * 更新告警规则
     */
    @Transactional
    public AlertRule updateRule(Long id, String tenantId, AlertRuleDTO dto) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null || !rule.getTenantId().equals(tenantId)) {
            throw new RuntimeException("告警规则不存在");
        }
        
        rule.setName(dto.getName());
        rule.setRuleType(dto.getRuleType());
        rule.setApiId(dto.getApiId());
        rule.setThreshold(dto.getThreshold());
        rule.setOperator(dto.getOperator());
        rule.setDuration(dto.getDuration());
        rule.setNotifyChannels(dto.getNotifyChannels());
        rule.setNotifyTargets(dto.getNotifyTargets());
        rule.setStatus(dto.getStatus());
        rule.setUpdatedAt(LocalDateTime.now());
        
        alertRuleMapper.updateById(rule);
        log.info("更新告警规则成功 - id: {}, name: {}", id, dto.getName());
        return rule;
    }

    /**
     * 删除告警规则
     */
    @Transactional
    public void deleteRule(Long id, String tenantId) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null || !rule.getTenantId().equals(tenantId)) {
            throw new RuntimeException("告警规则不存在");
        }
        alertRuleMapper.deleteById(id);
        log.info("删除告警规则成功 - id: {}", id);
    }

    /**
     * 获取告警规则详情
     */
    public AlertRule getRuleById(Long id, String tenantId) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null || !rule.getTenantId().equals(tenantId)) {
            return null;
        }
        return rule;
    }

    /**
     * 分页查询告警规则
     */
    public PageData<AlertRule> listRules(String tenantId, String ruleType, String status, 
                                       int pageNum, int pageSize) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getTenantId, tenantId);
        
        if (StringUtils.hasText(ruleType)) {
            wrapper.eq(AlertRule::getRuleType, ruleType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AlertRule::getStatus, status);
        }
        
        wrapper.orderByDesc(AlertRule::getCreatedAt);
        
        Page<AlertRule> page = alertRuleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageData.of(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    /**
     * 获取所有启用的告警规则
     */
    public List<AlertRule> getActiveRules(String tenantId) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getTenantId, tenantId)
               .eq(AlertRule::getStatus, "active");
        return alertRuleMapper.selectList(wrapper);
    }

    /**
     * 获取所有租户的启用规则（供定时任务使用）
     */
    public List<AlertRule> getAllActiveRules() {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getStatus, "active");
        return alertRuleMapper.selectList(wrapper);
    }

    /**
     * 启用规则
     */
    @Transactional
    public void enableRule(Long id, String tenantId) {
        updateRuleStatus(id, tenantId, "active");
    }

    /**
     * 禁用规则
     */
    @Transactional
    public void disableRule(Long id, String tenantId) {
        updateRuleStatus(id, tenantId, "disabled");
    }

    private void updateRuleStatus(Long id, String tenantId, String status) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null || !rule.getTenantId().equals(tenantId)) {
            throw new RuntimeException("告警规则不存在");
        }
        rule.setStatus(status);
        rule.setUpdatedAt(LocalDateTime.now());
        alertRuleMapper.updateById(rule);
        log.info("更新告警规则状态 - id: {}, status: {}", id, status);
    }
}
