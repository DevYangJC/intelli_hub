package com.intellihub.governance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intellihub.common.model.Result;
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
@RequestMapping("/api/v1/alert/rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /**
     * 创建告警规则
     */
    @PostMapping
    public Result<AlertRule> createRule(@RequestBody @Valid AlertRuleDTO dto,
                                         @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
                                         @RequestHeader(value = "X-User-Id", required = false) String userId) {
        AlertRule rule = alertRuleService.createRule(tenantId, dto, userId);
        return Result.success(rule);
    }

    /**
     * 更新告警规则
     */
    @PutMapping("/{id}")
    public Result<AlertRule> updateRule(@PathVariable Long id,
                                         @RequestBody @Valid AlertRuleDTO dto,
                                         @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        AlertRule rule = alertRuleService.updateRule(id, tenantId, dto);
        return Result.success(rule);
    }

    /**
     * 删除告警规则
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id,
                                    @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        alertRuleService.deleteRule(id, tenantId);
        return Result.success();
    }

    /**
     * 获取告警规则详情
     */
    @GetMapping("/{id}")
    public Result<AlertRule> getRule(@PathVariable Long id,
                                      @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        AlertRule rule = alertRuleService.getRuleById(id, tenantId);
        if (rule == null) {
            return Result.fail("告警规则不存在");
        }
        return Result.success(rule);
    }

    /**
     * 分页查询告警规则
     */
    @GetMapping
    public Result<IPage<AlertRule>> listRules(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AlertRule> page = alertRuleService.listRules(tenantId, ruleType, status, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 启用告警规则
     */
    @PostMapping("/{id}/enable")
    public Result<Void> enableRule(@PathVariable Long id,
                                    @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        alertRuleService.enableRule(id, tenantId);
        return Result.success();
    }

    /**
     * 禁用告警规则
     */
    @PostMapping("/{id}/disable")
    public Result<Void> disableRule(@PathVariable Long id,
                                     @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId) {
        alertRuleService.disableRule(id, tenantId);
        return Result.success();
    }
}
