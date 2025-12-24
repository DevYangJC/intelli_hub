package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.governance.dto.StatsOverviewDTO;
import com.intellihub.governance.dto.StatsTrendDTO;
import com.intellihub.governance.entity.ApiCallStatsDaily;
import com.intellihub.governance.entity.ApiCallStatsHourly;
import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.governance.mapper.ApiCallStatsDailyMapper;
import com.intellihub.governance.mapper.ApiCallStatsHourlyMapper;
import com.intellihub.redis.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计查询服务
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ApiCallStatsHourlyMapper hourlyMapper;
    private final ApiCallStatsDailyMapper dailyMapper;
    private final RedisUtil redisUtil;

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 获取统计概览
     */
    public StatsOverviewDTO getOverview(String tenantId) {
        StatsOverviewDTO overview = new StatsOverviewDTO();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 查询今日统计
        LambdaQueryWrapper<ApiCallStatsDaily> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(ApiCallStatsDaily::getTenantId, tenantId)
                .eq(ApiCallStatsDaily::getStatDate, today);
        List<ApiCallStatsDaily> todayStats = dailyMapper.selectList(todayWrapper);

        long todayTotal = 0, todaySuccess = 0, todayFail = 0;
        long latencySum = 0;
        int latencyCount = 0;

        for (ApiCallStatsDaily stat : todayStats) {
            todayTotal += stat.getTotalCount() != null ? stat.getTotalCount() : 0;
            todaySuccess += stat.getSuccessCount() != null ? stat.getSuccessCount() : 0;
            todayFail += stat.getFailCount() != null ? stat.getFailCount() : 0;
            if (stat.getAvgLatency() != null && stat.getTotalCount() != null) {
                latencySum += stat.getAvgLatency() * stat.getTotalCount();
                latencyCount += stat.getTotalCount();
            }
        }

        overview.setTodayTotalCount(todayTotal);
        overview.setTodaySuccessCount(todaySuccess);
        overview.setTodayFailCount(todayFail);
        overview.setTodaySuccessRate(todayTotal > 0 ? (todaySuccess * 100.0 / todayTotal) : 100.0);
        overview.setTodayAvgLatency(latencyCount > 0 ? (int) (latencySum / latencyCount) : 0);

        // 查询昨日统计
        LambdaQueryWrapper<ApiCallStatsDaily> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.eq(ApiCallStatsDaily::getTenantId, tenantId)
                .eq(ApiCallStatsDaily::getStatDate, yesterday);
        List<ApiCallStatsDaily> yesterdayStats = dailyMapper.selectList(yesterdayWrapper);

        long yesterdayTotal = 0;
        for (ApiCallStatsDaily stat : yesterdayStats) {
            yesterdayTotal += stat.getTotalCount() != null ? stat.getTotalCount() : 0;
        }
        overview.setYesterdayTotalCount(yesterdayTotal);

        // 日环比
        if (yesterdayTotal > 0) {
            overview.setDayOverDayRate((todayTotal - yesterdayTotal) * 100.0 / yesterdayTotal);
        } else {
            overview.setDayOverDayRate(todayTotal > 0 ? 100.0 : 0.0);
        }

        // 当前小时QPS（从Redis获取实时数据）
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String globalKey = RedisKeyConstants.buildStatsTotalKey(tenantId, "global", hour);
        Object value = redisUtil.get(globalKey);
        long currentHourCount = value != null ? Long.parseLong(value.toString()) : 0;
        // 假设已过去的分钟数
        int minutesPassed = LocalDateTime.now().getMinute();
        if (minutesPassed > 0) {
            overview.setCurrentQps(currentHourCount * 1.0 / (minutesPassed * 60));
        } else {
            overview.setCurrentQps(0.0);
        }

        return overview;
    }

    /**
     * 获取调用趋势（小时维度）
     */
    public StatsTrendDTO getHourlyTrend(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ApiCallStatsHourly> stats = hourlyMapper.selectTrend(tenantId, startTime, endTime);
        return buildTrendDTO(stats);
    }

    /**
     * 获取调用趋势（天维度）
     */
    public StatsTrendDTO getDailyTrend(String tenantId, LocalDate startDate, LocalDate endDate) {
        List<ApiCallStatsDaily> stats = dailyMapper.selectTrend(tenantId, startDate, endDate);
        return buildDailyTrendDTO(stats);
    }

    /**
     * 获取单个API的统计趋势（小时维度）
     */
    public StatsTrendDTO getApiHourlyTrend(String tenantId, String apiPath, 
                                           LocalDateTime startTime, LocalDateTime endTime) {
        List<ApiCallStatsHourly> stats = hourlyMapper.selectApiTrend(tenantId, apiPath, startTime, endTime);
        return buildTrendDTO(stats);
    }

    /**
     * 获取Top N API
     */
    public List<ApiCallStatsDaily> getTopApis(String tenantId, int limit) {
        return dailyMapper.selectTopApis(tenantId, LocalDate.now(), limit);
    }

    /**
     * 构建小时趋势DTO
     */
    private StatsTrendDTO buildTrendDTO(List<ApiCallStatsHourly> stats) {
        StatsTrendDTO dto = new StatsTrendDTO();
        List<String> timePoints = new ArrayList<>();
        List<Long> totalCounts = new ArrayList<>();
        List<Long> successCounts = new ArrayList<>();
        List<Long> failCounts = new ArrayList<>();
        List<Integer> avgLatencies = new ArrayList<>();
        List<Double> successRates = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:00");

        for (ApiCallStatsHourly stat : stats) {
            timePoints.add(stat.getStatTime().format(formatter));
            totalCounts.add(stat.getTotalCount() != null ? stat.getTotalCount() : 0L);
            successCounts.add(stat.getSuccessCount() != null ? stat.getSuccessCount() : 0L);
            failCounts.add(stat.getFailCount() != null ? stat.getFailCount() : 0L);
            avgLatencies.add(stat.getAvgLatency() != null ? stat.getAvgLatency() : 0);
            
            long total = stat.getTotalCount() != null ? stat.getTotalCount() : 0;
            long success = stat.getSuccessCount() != null ? stat.getSuccessCount() : 0;
            successRates.add(total > 0 ? (success * 100.0 / total) : 100.0);
        }

        dto.setTimePoints(timePoints);
        dto.setTotalCounts(totalCounts);
        dto.setSuccessCounts(successCounts);
        dto.setFailCounts(failCounts);
        dto.setAvgLatencies(avgLatencies);
        dto.setSuccessRates(successRates);

        return dto;
    }

    /**
     * 获取实时统计数据（供告警检测使用）
     */
    public Map<String, Object> getRealtimeStats(String tenantId, String apiId) {
        Map<String, Object> stats = new HashMap<>();
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        
        String apiPath = apiId != null ? apiId : "global";
        
        // 获取实时统计 (Key格式参见 RedisKeyConstants.STATS_REALTIME_PREFIX)
        Object totalObj = redisUtil.get(RedisKeyConstants.buildStatsTotalKey(tenantId, apiPath, hour));
        Object successObj = redisUtil.get(RedisKeyConstants.buildStatsSuccessKey(tenantId, apiPath, hour));
        Object failObj = redisUtil.get(RedisKeyConstants.buildStatsFailKey(tenantId, apiPath, hour));
        
        long totalCount = totalObj != null ? Long.parseLong(totalObj.toString()) : 0;
        long successCount = successObj != null ? Long.parseLong(successObj.toString()) : 0;
        long failCount = failObj != null ? Long.parseLong(failObj.toString()) : 0;
        
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("errorRate", totalCount > 0 ? (failCount * 100.0 / totalCount) : 0.0);
        
        // 从数据库获取平均延迟
        LocalDateTime hourStart = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LambdaQueryWrapper<ApiCallStatsHourly> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCallStatsHourly::getTenantId, tenantId)
               .eq(ApiCallStatsHourly::getStatTime, hourStart);
        if (apiId != null) {
            wrapper.eq(ApiCallStatsHourly::getApiId, apiId);
        }
        
        List<ApiCallStatsHourly> hourlyStats = hourlyMapper.selectList(wrapper);
        if (!hourlyStats.isEmpty()) {
            int avgLatency = hourlyStats.stream()
                    .mapToInt(s -> s.getAvgLatency() != null ? s.getAvgLatency() : 0)
                    .sum() / hourlyStats.size();
            stats.put("avgLatency", avgLatency);
        } else {
            stats.put("avgLatency", 0);
        }
        
        return stats;
    }

    /**
     * 构建天趋势DTO
     */
    private StatsTrendDTO buildDailyTrendDTO(List<ApiCallStatsDaily> stats) {
        StatsTrendDTO dto = new StatsTrendDTO();
        List<String> timePoints = new ArrayList<>();
        List<Long> totalCounts = new ArrayList<>();
        List<Long> successCounts = new ArrayList<>();
        List<Long> failCounts = new ArrayList<>();
        List<Integer> avgLatencies = new ArrayList<>();
        List<Double> successRates = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (ApiCallStatsDaily stat : stats) {
            timePoints.add(stat.getStatDate().format(formatter));
            totalCounts.add(stat.getTotalCount() != null ? stat.getTotalCount() : 0L);
            successCounts.add(stat.getSuccessCount() != null ? stat.getSuccessCount() : 0L);
            failCounts.add(stat.getFailCount() != null ? stat.getFailCount() : 0L);
            avgLatencies.add(stat.getAvgLatency() != null ? stat.getAvgLatency() : 0);
            
            long total = stat.getTotalCount() != null ? stat.getTotalCount() : 0;
            long success = stat.getSuccessCount() != null ? stat.getSuccessCount() : 0;
            successRates.add(total > 0 ? (success * 100.0 / total) : 100.0);
        }

        dto.setTimePoints(timePoints);
        dto.setTotalCounts(totalCounts);
        dto.setSuccessCounts(successCounts);
        dto.setFailCounts(failCounts);
        dto.setAvgLatencies(avgLatencies);
        dto.setSuccessRates(successRates);

        return dto;
    }
}
