package com.intellihub.aigc.controller;

import com.intellihub.ApiResponse;
import com.intellihub.aigc.service.CostAnalysisService;
import com.intellihub.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 成本分析控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/aigc/cost")
@RequiredArgsConstructor
@Tag(name = "成本分析", description = "AIGC成本分析与报表")
public class CostAnalysisController {

    private final CostAnalysisService costAnalysisService;

    /**
     * 获取成本概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取成本概览", description = "获取指定天数的成本概览")
    public ApiResponse<Map<String, Object>> getCostOverview(
            @RequestParam(defaultValue = "7") int days) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> overview = costAnalysisService.getCostOverview(tenantId, days);
        return ApiResponse.success(overview);
    }

    /**
     * 按模型分析成本
     */
    @GetMapping("/by-model")
    @Operation(summary = "按模型分析成本", description = "获取各模型的成本分布")
    public ApiResponse<Map<String, Object>> getCostByModel(
            @RequestParam(defaultValue = "30") int days) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> costByModel = costAnalysisService.getCostByModel(tenantId, days);
        return ApiResponse.success(costByModel);
    }

    /**
     * 按日期分析成本
     */
    @GetMapping("/by-date")
    @Operation(summary = "按日期分析成本", description = "获取每日成本趋势")
    public ApiResponse<Map<String, Object>> getCostByDate(
            @RequestParam(defaultValue = "30") int days) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> costByDate = costAnalysisService.getCostByDate(tenantId, days);
        return ApiResponse.success(costByDate);
    }

    /**
     * 获取成本预测
     */
    @GetMapping("/forecast")
    @Operation(summary = "获取成本预测", description = "预测本月总成本")
    public ApiResponse<Map<String, Object>> getCostForecast() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> forecast = costAnalysisService.getCostForecast(tenantId);
        return ApiResponse.success(forecast);
    }

    /**
     * 导出成本报表
     */
    @GetMapping("/export")
    @Operation(summary = "导出成本报表", description = "导出指定日期范围的成本报表")
    public ApiResponse<Map<String, Object>> exportCostReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> report = costAnalysisService.exportCostReport(tenantId, startDate, endDate);
        return ApiResponse.success(report);
    }
}
