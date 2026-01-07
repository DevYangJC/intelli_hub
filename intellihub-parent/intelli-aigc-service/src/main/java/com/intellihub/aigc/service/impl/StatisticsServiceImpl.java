package com.intellihub.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.aigc.entity.AigcRequestLog;
import com.intellihub.aigc.mapper.AigcRequestLogMapper;
import com.intellihub.aigc.service.QuotaService;
import com.intellihub.aigc.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 统计分析服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final AigcRequestLogMapper requestLogMapper;
    private final QuotaService quotaService;

    @Override
    public Map<String, Object> getTenantStatistics(String tenantId, int days) {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(days), LocalTime.MIN);

        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, startTime);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        // 统计数据
        long totalRequests = logs.size();
        long successRequests = logs.stream()
                .filter(log -> "success".equals(log.getStatus()))
                .count();
        long failedRequests = totalRequests - successRequests;

        int totalTokens = logs.stream()
                .mapToInt(AigcRequestLog::getTokensUsed)
                .sum();

        BigDecimal totalCost = logs.stream()
                .map(AigcRequestLog::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalDuration = logs.stream()
                .mapToLong(AigcRequestLog::getDuration)
                .sum();

        double avgDuration = totalRequests > 0 ? (double) totalDuration / totalRequests : 0;
        double successRate = totalRequests > 0 ? (double) successRequests / totalRequests * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalRequests", totalRequests);
        result.put("successRequests", successRequests);
        result.put("failedRequests", failedRequests);
        result.put("totalTokens", totalTokens);
        result.put("totalCost", totalCost);
        result.put("avgDuration", String.format("%.2f", avgDuration));
        result.put("successRate", String.format("%.2f", successRate));
        result.put("statisticsDays", days);

        log.info("租户统计 - tenantId: {}, days: {}, requests: {}", tenantId, days, totalRequests);
        return result;
    }

    @Override
    public Map<String, Object> getModelRanking(String tenantId, int limit) {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);

        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, startTime);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        // 按模型统计
        Map<String, ModelStats> modelStatsMap = new HashMap<>();
        for (AigcRequestLog log : logs) {
            String model = log.getModelName();
            ModelStats stats = modelStatsMap.getOrDefault(model, new ModelStats());
            stats.requestCount++;
            stats.tokensUsed += log.getTokensUsed();
            stats.totalCost = stats.totalCost.add(log.getCost());
            modelStatsMap.put(model, stats);
        }

        // 转换为列表并排序
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Map.Entry<String, ModelStats> entry : modelStatsMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("model", entry.getKey());
            item.put("requestCount", entry.getValue().requestCount);
            item.put("tokensUsed", entry.getValue().tokensUsed);
            item.put("totalCost", entry.getValue().totalCost);
            ranking.add(item);
        }

        // 按请求数降序排序
        ranking.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Long) o2.get("requestCount")).compareTo((Long) o1.get("requestCount"));
            }
        });

        // 限制返回数量
        if (ranking.size() > limit) {
            ranking = ranking.subList(0, limit);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        result.put("totalModels", modelStatsMap.size());

        return result;
    }

    @Override
    public Map<String, Object> getDailyTrend(String tenantId, int days) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                    .ge(AigcRequestLog::getCreatedAt, startTime)
                    .le(AigcRequestLog::getCreatedAt, endTime);

            List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

            long requestCount = logs.size();
            int tokensUsed = logs.stream().mapToInt(AigcRequestLog::getTokensUsed).sum();
            BigDecimal cost = logs.stream()
                    .map(AigcRequestLog::getCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("requestCount", requestCount);
            dayData.put("tokensUsed", tokensUsed);
            dayData.put("cost", cost);

            trendData.add(dayData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("trend", trendData);
        result.put("days", days);

        return result;
    }

    @Override
    public Map<String, Object> getRealTimeOverview(String tenantId) {
        // 今日统计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<AigcRequestLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, todayStart);

        List<AigcRequestLog> todayLogs = requestLogMapper.selectList(todayWrapper);

        long todayRequests = todayLogs.size();
        int todayTokens = todayLogs.stream().mapToInt(AigcRequestLog::getTokensUsed).sum();
        BigDecimal todayCost = todayLogs.stream()
                .map(AigcRequestLog::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 最近1小时统计
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        LambdaQueryWrapper<AigcRequestLog> hourWrapper = new LambdaQueryWrapper<>();
        hourWrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, hourAgo);

        long hourRequests = requestLogMapper.selectCount(hourWrapper);

        // 配额使用情况
        Map<String, Object> quotaUsage = quotaService.getQuotaUsage(tenantId);

        Map<String, Object> result = new HashMap<>();
        result.put("todayRequests", todayRequests);
        result.put("todayTokens", todayTokens);
        result.put("todayCost", todayCost);
        result.put("hourRequests", hourRequests);
        result.put("quotaUsage", quotaUsage);

        return result;
    }

    @Override
    public Map<String, Object> getUserRanking(String tenantId, int limit) {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);

        LambdaQueryWrapper<AigcRequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcRequestLog::getTenantId, tenantId)
                .ge(AigcRequestLog::getCreatedAt, startTime)
                .isNotNull(AigcRequestLog::getUserId);

        List<AigcRequestLog> logs = requestLogMapper.selectList(wrapper);

        // 按用户统计
        Map<String, UserStats> userStatsMap = new HashMap<>();
        for (AigcRequestLog log : logs) {
            String userId = log.getUserId();
            if (userId == null || userId.isEmpty()) {
                continue;
            }
            UserStats stats = userStatsMap.getOrDefault(userId, new UserStats());
            stats.requestCount++;
            stats.tokensUsed += log.getTokensUsed();
            stats.totalCost = stats.totalCost.add(log.getCost());
            userStatsMap.put(userId, stats);
        }

        // 转换为列表并排序
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Map.Entry<String, UserStats> entry : userStatsMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", entry.getKey());
            item.put("requestCount", entry.getValue().requestCount);
            item.put("tokensUsed", entry.getValue().tokensUsed);
            item.put("totalCost", entry.getValue().totalCost);
            ranking.add(item);
        }

        // 按请求数降序排序
        ranking.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Long) o2.get("requestCount")).compareTo((Long) o1.get("requestCount"));
            }
        });

        // 限制返回数量
        if (ranking.size() > limit) {
            ranking = ranking.subList(0, limit);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        result.put("totalUsers", userStatsMap.size());

        return result;
    }

    /**
     * 模型统计数据
     */
    private static class ModelStats {
        long requestCount = 0;
        int tokensUsed = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
    }

    /**
     * 用户统计数据
     */
    private static class UserStats {
        long requestCount = 0;
        int tokensUsed = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
    }
}
