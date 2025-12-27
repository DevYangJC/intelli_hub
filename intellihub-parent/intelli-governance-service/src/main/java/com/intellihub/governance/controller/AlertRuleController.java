package com.intellihub.governance.controller;

import com.intellihub.ApiResponse;
import com.intellihub.page.PageData;
import com.intellihub.constants.ResultCode;
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
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        AlertRule rule = alertRuleService.createRule(tenantId, dto, userId);
        return ApiResponse.success(rule);
    }

    /**
     * 更新告警规则
     */
    @PutMapping("/{id}")
    public ApiResponse<AlertRule> updateRule(@PathVariable Long id,
                                              @RequestBody @Valid AlertRuleDTO dto) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        AlertRule rule = alertRuleService.updateRule(id, tenantId, dto);
        return ApiResponse.success(rule);
    }

    /**
     * 删除告警规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        alertRuleService.deleteRule(id, tenantId);
        return ApiResponse.success(null);
    }

    /**
     * 获取告警规则详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AlertRule> getRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        AlertRule rule = alertRuleService.getRuleById(id, tenantId);
        if (rule == null) {
            return ApiResponse.failed(ResultCode.THE_ALARM_RULE_DOES_NOT_EXIST);
        }
        return ApiResponse.success(rule);
    }

    /**
     * 分页查询告警规则
     */
    @GetMapping
    public ApiResponse<PageData<AlertRule>> listRules(
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        PageData<AlertRule> page = alertRuleService.listRules(tenantId, ruleType, status, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    /**
     * 启用告警规则
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enableRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        alertRuleService.enableRule(id, tenantId);
        return ApiResponse.success(null);
    }

    /**
     * 禁用告警规则
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disableRule(@PathVariable Long id) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        alertRuleService.disableRule(id, tenantId);
        return ApiResponse.success(null);
    }
}
