package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.governance.dto.StatsOverviewDTO;
import com.intellihub.governance.dto.StatsTrendDTO;
import com.intellihub.governance.entity.ApiCallStatsDaily;
import com.intellihub.governance.entity.ApiCallStatsHourly;
import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.governance.mapper.ApiCallStatsDailyMapper;
import com.intellihub.governance.mapper.ApiCallStatsHourlyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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
 * <p>
 * 注意：实时统计数据由 Gateway 使用 StringRedisTemplate 写入，
 * 因此这里也必须使用 StringRedisTemplate 读取，避免序列化不匹配。
 * </p>
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
    private final StringRedisTemplate stringRedisTemplate;

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

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

        // 当前 QPS（从分钟级 Redis Key 获取）
        double currentQps = getQps(tenantId);
        overview.setCurrentQps(currentQps);

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
     * <p>
     * 从 Redis Hash 获取当前小时的实时统计数据
     * </p>
     * <p>
     * Redis Key 格式: alert:stats:{tenantId}:{hour} (Hash类型)
     * 字段: totalCount, successCount, failCount, latencySum
     * </p>
     */
    public Map<String, Object> getRealtimeStats(String tenantId, String apiId) {
        Map<String, Object> stats = new HashMap<>();
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        
        // 构建 Redis Key（新结构使用 Hash）
        String statsKey = RedisKeyConstants.buildAlertStatsKey(tenantId, hour);
        
        log.info("[实时统计] 查询Redis Hash Key: {}", statsKey);
        
        // 从 Hash 获取所有字段
        Map<Object, Object> hashData = stringRedisTemplate.opsForHash().entries(statsKey);
        log.info("[实时统计] HGETALL {} => {}", statsKey, hashData);
        
        if (hashData.isEmpty()) {
            log.info("[实时统计] Hash为空，返回零值");
            stats.put("totalCount", 0L);
            stats.put("successCount", 0L);
            stats.put("failCount", 0L);
            stats.put("errorRate", 0.0);
            stats.put("avgLatency", 0);
            return stats;
        }
        
        long totalCount = parseLong(hashData.get("totalCount"));
        long successCount = parseLong(hashData.get("successCount"));
        long failCount = parseLong(hashData.get("failCount"));
        long latencySum = parseLong(hashData.get("latencySum"));
        
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("errorRate", totalCount > 0 ? (failCount * 100.0 / totalCount) : 0.0);
        
        // 计算平均延迟
        int avgLatency = totalCount > 0 ? (int) (latencySum / totalCount) : 0;
        stats.put("avgLatency", avgLatency);
        
        log.info("[实时统计] 返回数据: totalCount={}, successCount={}, failCount={}, avgLatency={}ms", 
                totalCount, successCount, failCount, avgLatency);
        return stats;
    }
    
    /**
     * 获取告警相关的请求详情列表
     */
    public List<String> getAlertRequests(String tenantId, String hour) {
        String requestsKey = RedisKeyConstants.buildAlertRequestsKey(tenantId, hour);
        log.info("[实时统计] 获取请求列表 Key: {}", requestsKey);
        
        List<String> requests = stringRedisTemplate.opsForList().range(requestsKey, 0, -1);
        log.info("[实时统计] LRANGE {} => 获取到{}条记录", requestsKey, requests != null ? requests.size() : 0);
        return requests != null ? requests : new java.util.ArrayList<>();
    }
    
    /**
     * 删除告警相关的Redis数据（告警触发后调用，避免重复告警）
     */
    public void deleteAlertData(String tenantId, String hour) {
        String statsKey = RedisKeyConstants.buildAlertStatsKey(tenantId, hour);
        String requestsKey = RedisKeyConstants.buildAlertRequestsKey(tenantId, hour);
        
        log.info("[实时统计] 删除告警数据: statsKey={}, requestsKey={}", statsKey, requestsKey);
        stringRedisTemplate.delete(statsKey);
        stringRedisTemplate.delete(requestsKey);
    }
    
    private long parseLong(Object value) {
        if (value == null) return 0;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 获取上一分钟的 QPS（固定窗口）
     * <p>
     * QPS = 上一分钟的请求总数 / 60
     * </p>
     * <p>
     * Redis Key: alert:qps:{tenantId}:{minute}
     * minute 格式: yyyyMMddHHmm
     * </p>
     */
    public double getQps(String tenantId) {
        // 获取上一分钟的时间标识
        LocalDateTime lastMinute = LocalDateTime.now().minusMinutes(1);
        String minute = lastMinute.format(MINUTE_FORMATTER);
        String qpsKey = RedisKeyConstants.buildQpsKey(tenantId, minute);
        
        log.info("[QPS统计] 查询 Key: {}", qpsKey);
        
        String countStr = stringRedisTemplate.opsForValue().get(qpsKey);
        long count = countStr != null ? Long.parseLong(countStr) : 0;
        
        double qps = count / 60.0;
        log.info("[QPS统计] minute={}, count={}, QPS={}/s", minute, count, qps);
        
        return qps;
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
