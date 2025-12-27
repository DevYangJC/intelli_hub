package com.intellihub.governance.controller;

import com.intellihub.ApiResponse;
import com.intellihub.page.PageData;
import com.intellihub.context.UserContextHolder;
import com.intellihub.governance.dto.StatsOverviewDTO;
import com.intellihub.governance.dto.StatsTrendDTO;
import com.intellihub.governance.entity.ApiCallLog;
import com.intellihub.governance.entity.ApiCallStatsDaily;
import com.intellihub.governance.service.CallLogService;
import com.intellihub.governance.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计查询控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/governance/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final CallLogService callLogService;

    /**
     * 获取统计概览
     */
    @GetMapping("/overview")
    public ApiResponse<StatsOverviewDTO> getOverview() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        StatsOverviewDTO overview = statsService.getOverview(tenantId);
        return ApiResponse.success(overview);
    }

    /**
     * 获取调用趋势（小时维度）
     */
    @GetMapping("/trend/hourly")
    public ApiResponse<StatsTrendDTO> getHourlyTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        StatsTrendDTO trend = statsService.getHourlyTrend(tenantId, startTime, endTime);
        return ApiResponse.success(trend);
    }

    /**
     * 获取调用趋势（天维度）
     */
    @GetMapping("/trend/daily")
    public ApiResponse<StatsTrendDTO> getDailyTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        StatsTrendDTO trend = statsService.getDailyTrend(tenantId, startDate, endDate);
        return ApiResponse.success(trend);
    }

    /**
     * 获取单个API的统计趋势
     */
    @GetMapping("/api/{apiPath}")
    public ApiResponse<StatsTrendDTO> getApiTrend(
            @PathVariable String apiPath,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        // 解码路径
        String decodedPath = "/" + apiPath.replace("_", "/");
        StatsTrendDTO trend = statsService.getApiHourlyTrend(tenantId, decodedPath, startTime, endTime);
        return ApiResponse.success(trend);
    }

    /**
     * 获取Top N API
     */
    @GetMapping("/top")
    public ApiResponse<List<ApiCallStatsDaily>> getTopApis(
            @RequestParam(defaultValue = "10") int limit) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        List<ApiCallStatsDaily> topApis = statsService.getTopApis(tenantId, limit);
        return ApiResponse.success(topApis);
    }

    /**
     * 分页查询调用日志
     */
    @GetMapping("/logs")
    public ApiResponse<PageData<ApiCallLog>> getCallLogs(
            @RequestParam(required = false) String apiId,
            @RequestParam(required = false) String apiPath,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) Boolean success,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        PageData<ApiCallLog> logs = callLogService.pageCallLogs(
                tenantId, apiId, apiPath, appId, startTime, endTime, success, page, size);
        return ApiResponse.success(logs);
    }

    /**
     * 获取实时调用数
     */
    @GetMapping("/realtime")
    public ApiResponse<Long> getRealtimeCount() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        Long count = callLogService.getGlobalRealtimeCount(tenantId);
        return ApiResponse.success(count);
    }
}
