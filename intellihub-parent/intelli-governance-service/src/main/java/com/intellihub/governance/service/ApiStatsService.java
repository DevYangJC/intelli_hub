package com.intellihub.governance.service;

import com.intellihub.dubbo.ApiStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * API统计服务
 * <p>
 * 负责API调用统计的Redis操作：记录调用次数、成功率、平均响应时间
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiStatsService {

    private final StringRedisTemplate redisTemplate;

    // Redis Key 前缀
    private static final String KEY_PREFIX_TODAY = "api:stats:today:";
    private static final String KEY_PREFIX_TOTAL = "api:stats:total:";
    private static final String KEY_PREFIX_SUCCESS = "api:stats:success:";
    private static final String KEY_PREFIX_LATENCY_SUM = "api:stats:latency:sum:";
    private static final String KEY_PREFIX_LATENCY_COUNT = "api:stats:latency:count:";

    // Key 过期时间（7天）
    private static final long TTL_DAYS = 7;

    /**
     * 记录单次API调用
     *
     * @param apiId        API ID
     * @param success      是否成功
     * @param responseTime 响应时间（毫秒）
     */
    public void recordApiCall(String apiId, boolean success, long responseTime) {
        if (apiId == null || apiId.isEmpty()) {
            log.warn("API ID为空，跳过统计");
            return;
        }

        try {
            // 1. 今日调用次数
            String todayKey = KEY_PREFIX_TODAY + apiId;
            redisTemplate.opsForValue().increment(todayKey);
            redisTemplate.expire(todayKey, TTL_DAYS, TimeUnit.DAYS);

            // 2. 总调用次数
            String totalKey = KEY_PREFIX_TOTAL + apiId;
            redisTemplate.opsForValue().increment(totalKey);

            // 3. 成功次数
            if (success) {
                String successKey = KEY_PREFIX_SUCCESS + apiId;
                redisTemplate.opsForValue().increment(successKey);
                redisTemplate.expire(successKey, TTL_DAYS, TimeUnit.DAYS);
            }

            // 4. 响应时间统计
            if (responseTime > 0) {
                // 响应时间总和
                String latencySumKey = KEY_PREFIX_LATENCY_SUM + apiId;
                redisTemplate.opsForValue().increment(latencySumKey, responseTime);
                redisTemplate.expire(latencySumKey, TTL_DAYS, TimeUnit.DAYS);

                // 响应时间记录数
                String latencyCountKey = KEY_PREFIX_LATENCY_COUNT + apiId;
                redisTemplate.opsForValue().increment(latencyCountKey);
                redisTemplate.expire(latencyCountKey, TTL_DAYS, TimeUnit.DAYS);
            }

            log.debug("API统计记录成功 - apiId: {}, success: {}, responseTime: {}ms", apiId, success, responseTime);
        } catch (Exception e) {
            log.error("记录API统计失败 - apiId: {}", apiId, e);
        }
    }

    /**
     * 获取API实时统计
     *
     * @param apiId API ID
     * @return 统计数据
     */
    public ApiStatsDTO getApiStats(String apiId) {
        if (apiId == null || apiId.isEmpty()) {
            return null;
        }

        try {
            // 今日调用次数
            String todayValue = redisTemplate.opsForValue().get(KEY_PREFIX_TODAY + apiId);
            Long todayCalls = todayValue != null ? Long.parseLong(todayValue) : 0L;

            // 总调用次数
            String totalValue = redisTemplate.opsForValue().get(KEY_PREFIX_TOTAL + apiId);
            Long totalCalls = totalValue != null ? Long.parseLong(totalValue) : 0L;

            // 成功调用次数
            String successValue = redisTemplate.opsForValue().get(KEY_PREFIX_SUCCESS + apiId);
            Long successCalls = successValue != null ? Long.parseLong(successValue) : 0L;

            // 计算成功率
            Double successRate = 0.0;
            if (totalCalls > 0) {
                successRate = (successCalls * 100.0) / totalCalls;
                successRate = Math.round(successRate * 100.0) / 100.0; // 保留两位小数
            }

            // 计算平均响应时间
            Double avgResponseTime = 0.0;
            String latencySumValue = redisTemplate.opsForValue().get(KEY_PREFIX_LATENCY_SUM + apiId);
            String latencyCountValue = redisTemplate.opsForValue().get(KEY_PREFIX_LATENCY_COUNT + apiId);

            if (latencySumValue != null && latencyCountValue != null) {
                long latencySum = Long.parseLong(latencySumValue);
                long latencyCount = Long.parseLong(latencyCountValue);
                if (latencyCount > 0) {
                    avgResponseTime = (double) latencySum / latencyCount;
                    avgResponseTime = Math.round(avgResponseTime * 100.0) / 100.0; // 保留两位小数
                }
            }

            // 使用Builder创建DTO
            return ApiStatsDTO.builder()
                    .apiId(apiId)
                    .todayCalls(todayCalls)
                    .totalCalls(totalCalls)
                    .successCalls(successCalls)
                    .successRate(successRate)
                    .avgResponseTime(avgResponseTime)
                    .build();
        } catch (Exception e) {
            log.error("获取API统计失败 - apiId: {}", apiId, e);
            return null;
        }
    }

    /**
     * 批量获取多个API的统计
     *
     * @param apiIds API ID列表
     * @return API ID -> 统计数据的映射
     */
    public Map<String, ApiStatsDTO> batchGetApiStats(List<String> apiIds) {
        Map<String, ApiStatsDTO> result = new HashMap<>();

        if (apiIds == null || apiIds.isEmpty()) {
            return result;
        }

        for (String apiId : apiIds) {
            ApiStatsDTO stats = getApiStats(apiId);
            if (stats != null) {
                result.put(apiId, stats);
            }
        }

        return result;
    }

    /**
     * 重置今日统计
     * <p>
     * 每天0点调用，清除所有今日计数器
     * </p>
     */
    public void resetDailyStats() {
        try {
            Set<String> todayKeys = redisTemplate.keys(KEY_PREFIX_TODAY + "*");
            if (todayKeys != null && !todayKeys.isEmpty()) {
                Long deleted = redisTemplate.delete(todayKeys);
                log.info("今日统计重置完成 - 删除 {} 个key", deleted);
            }

            // 同时清除成功次数统计（也是今日的）
            Set<String> successKeys = redisTemplate.keys(KEY_PREFIX_SUCCESS + "*");
            if (successKeys != null && !successKeys.isEmpty()) {
                Long deleted = redisTemplate.delete(successKeys);
                log.info("成功次数统计重置完成 - 删除 {} 个key", deleted);
            }

            // 清除响应时间统计
            Set<String> latencySumKeys = redisTemplate.keys(KEY_PREFIX_LATENCY_SUM + "*");
            if (latencySumKeys != null && !latencySumKeys.isEmpty()) {
                redisTemplate.delete(latencySumKeys);
            }

            Set<String> latencyCountKeys = redisTemplate.keys(KEY_PREFIX_LATENCY_COUNT + "*");
            if (latencyCountKeys != null && !latencyCountKeys.isEmpty()) {
                redisTemplate.delete(latencyCountKeys);
            }

        } catch (Exception e) {
            log.error("重置今日统计失败", e);
        }
    }

    /**
     * 获取所有API ID列表（从Redis）
     *
     * @return API ID列表
     */
    public Set<String> getAllApiIds() {
        try {
            Set<String> totalKeys = redisTemplate.keys(KEY_PREFIX_TOTAL + "*");
            if (totalKeys == null || totalKeys.isEmpty()) {
                return Collections.emptySet();
            }

            // 提取API ID（去除前缀）
            return totalKeys.stream()
                    .map(key -> key.substring(KEY_PREFIX_TOTAL.length()))
                    .collect(java.util.stream.Collectors.toSet());
        } catch (Exception e) {
            log.error("获取所有API ID失败", e);
            return Collections.emptySet();
        }
    }

}

