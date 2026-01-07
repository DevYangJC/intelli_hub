package com.intellihub.aigc.controller;

import com.intellihub.aigc.monitor.PerformanceMonitor;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监控控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/aigc/monitor")
@RequiredArgsConstructor
@Tag(name = "性能监控", description = "AIGC性能监控统计")
public class MonitorController {

    private final PerformanceMonitor performanceMonitor;

    /**
     * 获取所有模型统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取所有模型统计", description = "获取所有模型的性能统计信息")
    public ApiResponse<Map<String, Map<String, Object>>> getAllStats() {
        Map<String, Map<String, Object>> stats = performanceMonitor.getAllStats();
        return ApiResponse.success(stats);
    }

    /**
     * 获取指定模型统计
     */
    @GetMapping("/stats/{model}")
    @Operation(summary = "获取指定模型统计", description = "获取指定模型的性能统计信息")
    public ApiResponse<Map<String, Object>> getModelStats(@PathVariable String model) {
        Map<String, Object> stats = performanceMonitor.getModelStats(model);
        return ApiResponse.success(stats);
    }

    /**
     * 重置所有统计
     */
    @DeleteMapping("/stats")
    @Operation(summary = "重置所有统计", description = "重置所有模型的性能统计数据")
    public ApiResponse<Void> resetAllStats() {
        performanceMonitor.reset();
        return ApiResponse.success(null);
    }

    /**
     * 重置指定模型统计
     */
    @DeleteMapping("/stats/{model}")
    @Operation(summary = "重置指定模型统计", description = "重置指定模型的性能统计数据")
    public ApiResponse<Void> resetModelStats(@PathVariable String model) {
        performanceMonitor.resetModel(model);
        return ApiResponse.success(null);
    }
}
