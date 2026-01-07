package com.intellihub.aigc.controller;

import com.intellihub.aigc.monitor.PerformanceMonitor;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
@Tag(name = "健康检查", description = "服务健康检查和就绪检查")
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PerformanceMonitor performanceMonitor;

    /**
     * 健康检查（Liveness）
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否存活")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "intelli-aigc-service");
        health.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(health);
    }

    /**
     * 就绪检查（Readiness）
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "检查服务是否准备好接收流量")
    public ApiResponse<Map<String, Object>> ready() {
        Map<String, Object> readiness = new HashMap<>();
        boolean isReady = true;
        
        // 检查数据库连接
        Map<String, Object> dbStatus = checkDatabase();
        readiness.put("database", dbStatus);
        if (!"UP".equals(dbStatus.get("status"))) {
            isReady = false;
        }
        
        // 检查Redis连接
        Map<String, Object> redisStatus = checkRedis();
        readiness.put("redis", redisStatus);
        if (!"UP".equals(redisStatus.get("status"))) {
            isReady = false;
        }
        
        readiness.put("status", isReady ? "UP" : "DOWN");
        readiness.put("service", "intelli-aigc-service");
        readiness.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(readiness);
    }

    /**
     * 详细信息
     */
    @GetMapping("/info")
    @Operation(summary = "服务信息", description = "获取服务详细信息")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("service", "intelli-aigc-service");
        info.put("version", "1.0.0");
        info.put("description", "IntelliHub AIGC服务 - 集成国内主流AI大模型");
        
        // 支持的模型
        info.put("supportedModels", new String[]{
            "qwen-turbo", "qwen-plus", "qwen-max", "qwen-max-longcontext",
            "ernie-bot-turbo", "ernie-bot", "ernie-bot-4",
            "hunyuan-lite", "hunyuan-standard", "hunyuan-pro"
        });
        
        // 提供商
        info.put("providers", new String[]{"aliyun_qwen", "baidu_ernie", "tencent_hunyuan"});
        
        // 功能特性
        info.put("features", new String[]{
            "文本生成", "智能对话", "流式响应", "模板管理",
            "配额管理", "成本分析", "性能监控", "智能限流"
        });
        
        info.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(info);
    }

    /**
     * 性能指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "性能指标", description = "获取服务性能指标")
    public ApiResponse<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 获取所有模型统计
        Map<String, Map<String, Object>> allStats = performanceMonitor.getAllStats();
        
        long totalRequests = 0;
        long totalSuccessRequests = 0;
        long totalFailedRequests = 0;
        
        for (Map<String, Object> stats : allStats.values()) {
            totalRequests += (Long) stats.get("requestCount");
            totalSuccessRequests += (Long) stats.get("successCount");
            totalFailedRequests += (Long) stats.get("failureCount");
        }
        
        metrics.put("totalRequests", totalRequests);
        metrics.put("totalSuccessRequests", totalSuccessRequests);
        metrics.put("totalFailedRequests", totalFailedRequests);
        metrics.put("modelsMonitored", allStats.size());
        metrics.put("modelStats", allStats);
        metrics.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.success(metrics);
    }

    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> status = new HashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            boolean isValid = connection.isValid(3);
            connection.close();
            
            status.put("status", isValid ? "UP" : "DOWN");
            status.put("type", "MySQL");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            log.error("数据库连接检查失败", e);
        }
        return status;
    }

    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> status = new HashMap<>();
        try {
            redisTemplate.opsForValue().get("health_check");
            status.put("status", "UP");
            status.put("type", "Redis");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            log.error("Redis连接检查失败", e);
        }
        return status;
    }
}
