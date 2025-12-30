package com.intellihub.search.controller;

import com.intellihub.ApiResponse;
import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import com.intellihub.search.constant.IndexConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * ES 健康检查控制器
 *
 * @author IntelliHub
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search/health")
@RequiredArgsConstructor
public class HealthController {

    private final ElasticsearchTemplate elasticsearchTemplate;

    /**
     * ES 健康检查
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", System.currentTimeMillis());

        try {
            // 检查 ES 连接
            boolean connected = elasticsearchTemplate.ping();
            health.put("elasticsearch", connected ? "UP" : "DOWN");

            if (connected) {
                // 检查索引状态
                Map<String, Object> indices = new HashMap<>();
                indices.put(IndexConstants.INDEX_API, checkIndex(IndexConstants.INDEX_API));
                indices.put(IndexConstants.INDEX_APP, checkIndex(IndexConstants.INDEX_APP));
                indices.put(IndexConstants.INDEX_USER, checkIndex(IndexConstants.INDEX_USER));
                health.put("indices", indices);
            }

            health.put("status", connected ? "UP" : "DOWN");
            return ApiResponse.success(health);
        } catch (Exception e) {
            log.error("ES 健康检查失败: {}", e.getMessage());
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ApiResponse.success(health);
        }
    }

    /**
     * 获取索引统计信息
     */
    @GetMapping("/indices")
    public ApiResponse<Map<String, Object>> getIndicesStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put(IndexConstants.INDEX_API, getIndexStats(IndexConstants.INDEX_API));
            stats.put(IndexConstants.INDEX_APP, getIndexStats(IndexConstants.INDEX_APP));
            stats.put(IndexConstants.INDEX_USER, getIndexStats(IndexConstants.INDEX_USER));
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取索引统计失败: {}", e.getMessage());
            return ApiResponse.failed(null, "获取索引统计失败: " + e.getMessage());
        }
    }

    private Map<String, Object> checkIndex(String indexName) {
        Map<String, Object> info = new HashMap<>();
        try {
            boolean exists = elasticsearchTemplate.indexExists(indexName);
            info.put("exists", exists);
            info.put("status", exists ? "UP" : "NOT_FOUND");
        } catch (Exception e) {
            info.put("exists", false);
            info.put("status", "ERROR");
            info.put("error", e.getMessage());
        }
        return info;
    }

    private Map<String, Object> getIndexStats(String indexName) {
        Map<String, Object> stats = new HashMap<>();
        try {
            boolean exists = elasticsearchTemplate.indexExists(indexName);
            stats.put("exists", exists);
            if (exists) {
                long docCount = elasticsearchTemplate.count(indexName);
                stats.put("docCount", docCount);
            }
        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }
        return stats;
    }
}
