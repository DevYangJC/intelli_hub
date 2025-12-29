package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.governance.dto.ApiStatsDetailDTO;
import com.intellihub.governance.dto.StatsOverviewDTO;
import com.intellihub.governance.dto.StatsTrendDTO;
import com.intellihub.governance.entity.ApiCallStatsDaily;
import com.intellihub.governance.entity.ApiCallStatsHourly;
import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.governance.mapper.ApiCallLogMapper;
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

    private final ApiCallLogMapper callLogMapper;
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
    /**
     * 获取单个API的统计详情
     * <p>
     * 优化策略：
     * 1. 基础统计数据优先从预聚合表（api_call_stats_daily）获取
     * 2. 今日实时数据从日志表获取（数据量小）
     * 3. 分布数据限制查询最近3天以控制数据量
     * </p>
     *
     * @param tenantId 租户ID
     * @param apiId    API ID
     * @return API统计详情
     */
    public ApiStatsDetailDTO getApiStatsDetail(String tenantId, String apiId) {
        ApiStatsDetailDTO dto = new ApiStatsDetailDTO();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);
        
        // 今日开始时间和结束时间
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = now;
        
        // 昨日时间范围
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(23, 59, 59);
        
        // 1. 获取今日统计（从日志表，数据量小）
        Map<String, Object> todayStats = callLogMapper.selectApiStatsSummary(tenantId, apiId, todayStart, todayEnd);
        long todayCalls = parseLong(todayStats.get("total_count"));
        long todaySuccess = parseLong(todayStats.get("success_count"));
        int todayAvgLatency = parseInt(todayStats.get("avg_latency"));
        
        // 2. 获取昨日统计（优先从预聚合表获取）
        long yesterdayCalls = 0;
        long yesterdaySuccess = 0;
        int yesterdayAvgLatency = 0;
        
        // 尝试从预聚合表获取昨日数据
        LambdaQueryWrapper<ApiCallStatsDaily> dailyWrapper = new LambdaQueryWrapper<>();
        dailyWrapper.eq(ApiCallStatsDaily::getTenantId, tenantId)
                   .eq(ApiCallStatsDaily::getStatDate, yesterday);
        List<ApiCallStatsDaily> yesterdayDailyStats = dailyMapper.selectList(dailyWrapper);
        
        if (!yesterdayDailyStats.isEmpty()) {
            // 从预聚合表聚合
            for (ApiCallStatsDaily stat : yesterdayDailyStats) {
                yesterdayCalls += stat.getTotalCount() != null ? stat.getTotalCount() : 0;
                yesterdaySuccess += stat.getSuccessCount() != null ? stat.getSuccessCount() : 0;
            }
            yesterdayAvgLatency = (int) yesterdayDailyStats.stream()
                    .mapToInt(s -> s.getAvgLatency() != null ? s.getAvgLatency() : 0)
                    .average().orElse(0);
        } else {
            // 回退到日志表查询
            Map<String, Object> yesterdayStats = callLogMapper.selectApiStatsSummary(tenantId, apiId, yesterdayStart, yesterdayEnd);
            yesterdayCalls = parseLong(yesterdayStats.get("total_count"));
            yesterdaySuccess = parseLong(yesterdayStats.get("success_count"));
            yesterdayAvgLatency = parseInt(yesterdayStats.get("avg_latency"));
        }
        
        // 3. 获取历史总调用次数（从预聚合表汇总）
        Long totalCalls = getTotalCallsFromDailyStats(tenantId, apiId);
        totalCalls = totalCalls + todayCalls; // 加上今日实时数据
        
        // 设置基本统计
        dto.setTodayCalls(todayCalls);
        dto.setTotalCalls(totalCalls);
        dto.setAvgLatency(todayAvgLatency);
        dto.setSuccessRate(todayCalls > 0 ? (todaySuccess * 100.0 / todayCalls) : 100.0);
        
        // 计算环比
        dto.setTodayTrend(calculateTrend(todayCalls, yesterdayCalls));
        dto.setLatencyTrend(calculateTrend(todayAvgLatency, yesterdayAvgLatency));
        
        double todaySuccessRate = todayCalls > 0 ? (todaySuccess * 100.0 / todayCalls) : 100.0;
        double yesterdaySuccessRate = yesterdayCalls > 0 ? (yesterdaySuccess * 100.0 / yesterdayCalls) : 100.0;
        dto.setSuccessRateTrend(todaySuccessRate - yesterdaySuccessRate);
        
        // 4. 获取最近3天的状态码分布（限制时间范围以优化性能）
        LocalDateTime recentStart = today.minusDays(3).atStartOfDay();
        List<Map<String, Object>> statusDist = callLogMapper.selectStatusDistribution(tenantId, apiId, recentStart, todayEnd);
        dto.setStatusDistribution(buildStatusDistribution(statusDist));
        
        // 5. 获取最近3天的响应时间分布
        List<Map<String, Object>> latencyDist = callLogMapper.selectLatencyDistribution(tenantId, apiId, recentStart, todayEnd);
        dto.setLatencyDistribution(buildLatencyDistribution(latencyDist));
        
        return dto;
    }
    
    /**
     * 从预聚合表获取历史总调用次数
     */
    private Long getTotalCallsFromDailyStats(String tenantId, String apiId) {
        LambdaQueryWrapper<ApiCallStatsDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCallStatsDaily::getTenantId, tenantId);
        // 注意：apiId可能需要通过apiPath关联，这里简化处理
        List<ApiCallStatsDaily> stats = dailyMapper.selectList(wrapper);
        
        return stats.stream()
                .mapToLong(s -> s.getTotalCount() != null ? s.getTotalCount() : 0)
                .sum();
    }
    
    /**
     * 计算环比增长率
     */
    private Double calculateTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return (current - previous) * 100.0 / previous;
    }
    
    /**
     * 构建状态码分布
     */
    private List<ApiStatsDetailDTO.StatusDistribution> buildStatusDistribution(List<Map<String, Object>> data) {
        List<ApiStatsDetailDTO.StatusDistribution> result = new ArrayList<>();
        
        // 初始化三个类别
        Map<String, Long> countMap = new HashMap<>();
        countMap.put("2xx", 0L);
        countMap.put("4xx", 0L);
        countMap.put("5xx", 0L);
        
        long total = 0;
        for (Map<String, Object> item : data) {
            String category = (String) item.get("status_category");
            long count = parseLong(item.get("count"));
            if (countMap.containsKey(category)) {
                countMap.put(category, count);
                total += count;
            }
        }
        
        // 构建分布项
        result.add(createStatusDistribution("2xx", countMap.get("2xx"), total, "success", "#52c41a"));
        result.add(createStatusDistribution("4xx", countMap.get("4xx"), total, "warning", "#faad14"));
        result.add(createStatusDistribution("5xx", countMap.get("5xx"), total, "danger", "#ff4d4f"));
        
        return result;
    }
    
    private ApiStatsDetailDTO.StatusDistribution createStatusDistribution(
            String code, long count, long total, String type, String color) {
        ApiStatsDetailDTO.StatusDistribution dist = new ApiStatsDetailDTO.StatusDistribution();
        dist.setCode(code);
        dist.setCount(count);
        dist.setPercent(total > 0 ? (count * 100.0 / total) : 0.0);
        dist.setType(type);
        dist.setColor(color);
        return dist;
    }
    
    /**
     * 构建响应时间分布
     */
    private List<ApiStatsDetailDTO.LatencyDistribution> buildLatencyDistribution(List<Map<String, Object>> data) {
        List<ApiStatsDetailDTO.LatencyDistribution> result = new ArrayList<>();
        
        // 初始化四个区间
        Map<String, Long> countMap = new HashMap<>();
        countMap.put("< 50ms", 0L);
        countMap.put("50-100ms", 0L);
        countMap.put("100-500ms", 0L);
        countMap.put("> 500ms", 0L);
        
        long total = 0;
        for (Map<String, Object> item : data) {
            String range = (String) item.get("latency_range");
            long count = parseLong(item.get("count"));
            if (countMap.containsKey(range)) {
                countMap.put(range, count);
                total += count;
            }
        }
        
        // 构建分布项
        result.add(createLatencyDistribution("< 50ms", countMap.get("< 50ms"), total, "#52c41a"));
        result.add(createLatencyDistribution("50-100ms", countMap.get("50-100ms"), total, "#1890ff"));
        result.add(createLatencyDistribution("100-500ms", countMap.get("100-500ms"), total, "#faad14"));
        result.add(createLatencyDistribution("> 500ms", countMap.get("> 500ms"), total, "#ff4d4f"));
        
        return result;
    }
    
    private ApiStatsDetailDTO.LatencyDistribution createLatencyDistribution(
            String range, long count, long total, String color) {
        ApiStatsDetailDTO.LatencyDistribution dist = new ApiStatsDetailDTO.LatencyDistribution();
        dist.setRange(range);
        dist.setPercent(total > 0 ? (count * 100.0 / total) : 0.0);
        dist.setColor(color);
        return dist;
    }
    
    private int parseInt(Object value) {
        if (value == null) return 0;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

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
