package com.intellihub.governance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intellihub.ApiResponse;
import com.intellihub.context.UserContextHolder;
import com.intellihub.governance.dto.AlertRuleDTO;
import com.intellihub.governance.entity.AlertRule;
import com.intellihub.governance.service.AlertRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 告警规则控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/governance/v1/alert/rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /**
     * 创建告警规则
     */
    @PostMapping
    public ApiResponse<AlertRule> createRule(@RequestBody @Valid AlertRuleDTO dto) {
        String tenantId = UserContextHolder.getTenantIdStr();
        String userId = UserContextHolder.getUserIdStr();
        AlertRule rule = alertRuleService.createRule(tenantId, dto, userId);
        return ApiResponse.success(rule);
    }

    /**
     * 更新告警规则
     */
    @PutMapping("/{id}")
    public ApiResponse<AlertRule> updateRule(@PathVariable Long id,
                                              @RequestBody @Valid AlertRuleDTO dto) {
        String tenantId = UserContextHolder.getTenantIdStr();
        AlertRule rule = alertRuleService.updateRule(id, tenantId, dto);
        return ApiResponse.success(rule);
    }

    /**
     * 删除告警规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getTenantIdStr();
        alertRuleService.deleteRule(id, tenantId);
        return ApiResponse.success(null);
    }

    /**
     * 获取告警规则详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AlertRule> getRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getTenantIdStr();
        AlertRule rule = alertRuleService.getRuleById(id, tenantId);
        if (rule == null) {
            return ApiResponse.fail("告警规则不存在");
        }
        return ApiResponse.success(rule);
    }

    /**
     * 分页查询告警规则
     */
    @GetMapping
    public ApiResponse<IPage<AlertRule>> listRules(
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String tenantId = UserContextHolder.getTenantIdStr();
        IPage<AlertRule> page = alertRuleService.listRules(tenantId, ruleType, status, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    /**
     * 启用告警规则
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enableRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getTenantIdStr();
        alertRuleService.enableRule(id, tenantId);
        return ApiResponse.success(null);
    }

    /**
     * 禁用告警规则
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disableRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getTenantIdStr();
        alertRuleService.disableRule(id, tenantId);
        return ApiResponse.success(null);
    }
}
