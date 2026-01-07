package com.intellihub.aigc.controller;

import com.intellihub.aigc.service.StatisticsService;
import com.intellihub.context.UserContextHolder;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计Dashboard控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/aigc/dashboard")
@RequiredArgsConstructor
@Tag(name = "统计Dashboard", description = "AIGC调用统计与分析")
public class DashboardController {

    private final StatisticsService statisticsService;

    /**
     * 获取租户统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取租户统计", description = "获取指定天数的租户调用统计")
    public ApiResponse<Map<String, Object>> getTenantStatistics(
            @RequestParam(defaultValue = "7") int days) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> statistics = statisticsService.getTenantStatistics(tenantId, days);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取模型排行
     */
    @GetMapping("/model-ranking")
    @Operation(summary = "获取模型排行", description = "获取最近7天的模型使用排行")
    public ApiResponse<Map<String, Object>> getModelRanking(
            @RequestParam(defaultValue = "10") int limit) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> ranking = statisticsService.getModelRanking(tenantId, limit);
        return ApiResponse.success(ranking);
    }

    /**
     * 获取每日趋势
     */
    @GetMapping("/daily-trend")
    @Operation(summary = "获取每日趋势", description = "获取指定天数的调用趋势")
    public ApiResponse<Map<String, Object>> getDailyTrend(
            @RequestParam(defaultValue = "7") int days) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> trend = statisticsService.getDailyTrend(tenantId, days);
        return ApiResponse.success(trend);
    }

    /**
     * 获取实时概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取实时概览", description = "获取今日和最近1小时的实时统计")
    public ApiResponse<Map<String, Object>> getRealTimeOverview() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> overview = statisticsService.getRealTimeOverview(tenantId);
        return ApiResponse.success(overview);
    }

    /**
     * 获取用户排行
     */
    @GetMapping("/user-ranking")
    @Operation(summary = "获取用户排行", description = "获取最近7天的用户使用排行")
    public ApiResponse<Map<String, Object>> getUserRanking(
            @RequestParam(defaultValue = "10") int limit) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Map<String, Object> ranking = statisticsService.getUserRanking(tenantId, limit);
        return ApiResponse.success(ranking);
    }
}
