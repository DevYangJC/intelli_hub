package com.intellihub.aigc.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class PerformanceMonitor {

    /**
     * 请求计数
     */
    private final Map<String, AtomicLong> requestCountMap = new ConcurrentHashMap<String, AtomicLong>();

    /**
     * 成功计数
     */
    private final Map<String, AtomicLong> successCountMap = new ConcurrentHashMap<String, AtomicLong>();

    /**
     * 失败计数
     */
    private final Map<String, AtomicLong> failureCountMap = new ConcurrentHashMap<String, AtomicLong>();

    /**
     * 总耗时
     */
    private final Map<String, AtomicLong> totalTimeMap = new ConcurrentHashMap<String, AtomicLong>();

    /**
     * 总Token数
     */
    private final Map<String, AtomicLong> totalTokensMap = new ConcurrentHashMap<String, AtomicLong>();

    /**
     * 记录请求
     *
     * @param model 模型名称
     * @param success 是否成功
     * @param costMs 耗时（毫秒）
     * @param tokens Token数
     */
    public void recordRequest(String model, boolean success, long costMs, int tokens) {
        // 请求计数
        requestCountMap.computeIfAbsent(model, k -> new AtomicLong(0)).incrementAndGet();

        // 成功/失败计数
        if (success) {
            successCountMap.computeIfAbsent(model, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            failureCountMap.computeIfAbsent(model, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 累加耗时
        totalTimeMap.computeIfAbsent(model, k -> new AtomicLong(0)).addAndGet(costMs);

        // 累加Token数
        totalTokensMap.computeIfAbsent(model, k -> new AtomicLong(0)).addAndGet(tokens);

        log.debug("性能监控 - model: {}, success: {}, costMs: {}, tokens: {}", 
                model, success, costMs, tokens);
    }

    /**
     * 获取模型统计信息
     *
     * @param model 模型名称
     * @return 统计信息
     */
    public Map<String, Object> getModelStats(String model) {
        long requestCount = getCount(requestCountMap, model);
        long successCount = getCount(successCountMap, model);
        long failureCount = getCount(failureCountMap, model);
        long totalTime = getCount(totalTimeMap, model);
        long totalTokens = getCount(totalTokensMap, model);

        double avgTime = requestCount > 0 ? (double) totalTime / requestCount : 0;
        double avgTokens = requestCount > 0 ? (double) totalTokens / requestCount : 0;
        double successRate = requestCount > 0 ? (double) successCount / requestCount * 100 : 0;

        Map<String, Object> stats = new ConcurrentHashMap<String, Object>();
        stats.put("model", model);
        stats.put("requestCount", requestCount);
        stats.put("successCount", successCount);
        stats.put("failureCount", failureCount);
        stats.put("totalTime", totalTime);
        stats.put("totalTokens", totalTokens);
        stats.put("avgTime", String.format("%.2f", avgTime));
        stats.put("avgTokens", String.format("%.2f", avgTokens));
        stats.put("successRate", String.format("%.2f", successRate));

        return stats;
    }

    /**
     * 获取所有模型统计
     *
     * @return 统计信息Map
     */
    public Map<String, Map<String, Object>> getAllStats() {
        Map<String, Map<String, Object>> allStats = new ConcurrentHashMap<String, Map<String, Object>>();
        
        for (String model : requestCountMap.keySet()) {
            allStats.put(model, getModelStats(model));
        }
        
        return allStats;
    }

    /**
     * 重置统计数据
     */
    public void reset() {
        requestCountMap.clear();
        successCountMap.clear();
        failureCountMap.clear();
        totalTimeMap.clear();
        totalTokensMap.clear();
        log.info("性能监控数据已重置");
    }

    /**
     * 重置指定模型统计数据
     *
     * @param model 模型名称
     */
    public void resetModel(String model) {
        requestCountMap.remove(model);
        successCountMap.remove(model);
        failureCountMap.remove(model);
        totalTimeMap.remove(model);
        totalTokensMap.remove(model);
        log.info("重置模型监控数据 - model: {}", model);
    }

    /**
     * 获取计数
     */
    private long getCount(Map<String, AtomicLong> map, String key) {
        AtomicLong counter = map.get(key);
        return counter != null ? counter.get() : 0;
    }
}
