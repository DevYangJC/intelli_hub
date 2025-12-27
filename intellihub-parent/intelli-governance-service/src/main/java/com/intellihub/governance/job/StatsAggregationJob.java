package com.intellihub.governance.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.governance.entity.ApiCallLog;
import com.intellihub.governance.entity.ApiCallStatsDaily;
import com.intellihub.governance.entity.ApiCallStatsHourly;
import com.intellihub.governance.mapper.ApiCallLogMapper;
import com.intellihub.governance.mapper.ApiCallStatsDailyMapper;
import com.intellihub.governance.mapper.ApiCallStatsHourlyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计聚合任务
 * <p>
 * 定期将 api_call_log 表的数据聚合到 api_call_stats_hourly 和 api_call_stats_daily 表
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatsAggregationJob {

    private final ApiCallLogMapper callLogMapper;
    private final ApiCallStatsHourlyMapper hourlyMapper;
    private final ApiCallStatsDailyMapper dailyMapper;

    /**
     * 每小时聚合一次（整点后5分钟执行，聚合上一小时的数据）
     */
    @Scheduled(cron = "0 5 * * * ?")
    @Transactional
    public void aggregateHourlyStats() {
        log.info("========== [统计聚合] 开始执行小时聚合任务 ==========");
        try {
            // 聚合上一小时的数据
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endTime = startTime.plusHours(1);
            
            aggregateHourlyData(startTime, endTime);
            
            log.info("========== [统计聚合] 小时聚合任务完成 ==========");
        } catch (Exception e) {
            log.error("[统计聚合] 小时聚合任务失败", e);
        }
    }

    /**
     * 每天凌晨聚合一次（聚合前一天的数据）
     */
    @Scheduled(cron = "0 10 0 * * ?")
    @Transactional
    public void aggregateDailyStats() {
        log.info("========== [统计聚合] 开始执行天聚合任务 ==========");
        try {
            // 聚合昨天的数据
            LocalDate yesterday = LocalDate.now().minusDays(1);
            aggregateDailyData(yesterday);
            
            log.info("========== [统计聚合] 天聚合任务完成 ==========");
        } catch (Exception e) {
            log.error("[统计聚合] 天聚合任务失败", e);
        }
    }

    /**
     * 启动时执行一次，补充今天的统计数据
     */
    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 10000)
    @Transactional
    public void aggregateOnStartup() {
        log.info("========== [统计聚合] 启动时执行补充聚合 ==========");
        try {
            // 聚合今天的小时数据
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime now = LocalDateTime.now();
            
            // 按小时聚合今天已过去的时间
            LocalDateTime hourStart = startOfDay;
            while (hourStart.isBefore(now)) {
                LocalDateTime hourEnd = hourStart.plusHours(1);
                if (hourEnd.isAfter(now)) {
                    hourEnd = now;
                }
                aggregateHourlyData(hourStart, hourEnd);
                hourStart = hourStart.plusHours(1);
            }
            
            // 聚合今天的天统计
            aggregateDailyData(today);
            
            // 如果昨天的数据也没有，补充昨天的
            LocalDate yesterday = today.minusDays(1);
            aggregateDailyData(yesterday);
            
            log.info("========== [统计聚合] 启动补充聚合完成 ==========");
        } catch (Exception e) {
            log.error("[统计聚合] 启动补充聚合失败", e);
        }
    }

    /**
     * 聚合指定时间段的小时统计
     */
    private void aggregateHourlyData(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[统计聚合] 聚合小时数据: {} - {}", startTime, endTime);
        
        // 查询该时间段的调用日志
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(ApiCallLog::getRequestTime, startTime)
               .lt(ApiCallLog::getRequestTime, endTime);
        List<ApiCallLog> logs = callLogMapper.selectList(wrapper);
        
        if (logs.isEmpty()) {
            log.info("[统计聚合] 该时间段无调用日志");
            return;
        }
        
        // 按 tenantId + apiPath + appId 分组聚合
        Map<String, List<ApiCallLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(log -> 
                    String.format("%s|%s|%s", 
                        log.getTenantId() != null ? log.getTenantId() : "default",
                        log.getApiPath() != null ? log.getApiPath() : "unknown",
                        log.getAppId() != null ? log.getAppId() : "")));
        
        LocalDateTime statTime = startTime;
        
        for (Map.Entry<String, List<ApiCallLog>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\\|", -1);
            String tenantId = parts[0];
            String apiPath = parts[1];
            String appId = parts.length > 2 ? parts[2] : null;
            List<ApiCallLog> groupLogs = entry.getValue();
            
            // 计算统计数据
            long totalCount = groupLogs.size();
            long successCount = groupLogs.stream().filter(l -> Boolean.TRUE.equals(l.getSuccess())).count();
            long failCount = totalCount - successCount;
            
            List<Integer> latencies = groupLogs.stream()
                    .map(ApiCallLog::getLatency)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            
            int avgLatency = latencies.isEmpty() ? 0 : 
                    (int) latencies.stream().mapToInt(Integer::intValue).average().orElse(0);
            int maxLatency = latencies.isEmpty() ? 0 : 
                    latencies.stream().mapToInt(Integer::intValue).max().orElse(0);
            int minLatency = latencies.isEmpty() ? 0 : 
                    latencies.stream().mapToInt(Integer::intValue).min().orElse(0);
            int p95Latency = latencies.isEmpty() ? 0 : 
                    latencies.get((int) (latencies.size() * 0.95));
            int p99Latency = latencies.isEmpty() ? 0 : 
                    latencies.get((int) (latencies.size() * 0.99));
            
            // 查找或创建记录
            LambdaQueryWrapper<ApiCallStatsHourly> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApiCallStatsHourly::getTenantId, tenantId)
                       .eq(ApiCallStatsHourly::getApiPath, apiPath)
                       .eq(ApiCallStatsHourly::getStatTime, statTime);
            if (appId != null && !appId.isEmpty()) {
                queryWrapper.eq(ApiCallStatsHourly::getAppId, appId);
            } else {
                queryWrapper.isNull(ApiCallStatsHourly::getAppId);
            }
            
            ApiCallStatsHourly existing = hourlyMapper.selectOne(queryWrapper);
            
            if (existing != null) {
                // 更新
                existing.setTotalCount(totalCount);
                existing.setSuccessCount(successCount);
                existing.setFailCount(failCount);
                existing.setAvgLatency(avgLatency);
                existing.setMaxLatency(maxLatency);
                existing.setMinLatency(minLatency);
                existing.setP95Latency(p95Latency);
                existing.setP99Latency(p99Latency);
                existing.setUpdatedAt(LocalDateTime.now());
                hourlyMapper.updateById(existing);
            } else {
                // 插入
                ApiCallStatsHourly stats = new ApiCallStatsHourly();
                stats.setTenantId(tenantId);
                stats.setApiPath(apiPath);
                stats.setAppId(appId != null && !appId.isEmpty() ? appId : null);
                stats.setStatTime(statTime);
                stats.setTotalCount(totalCount);
                stats.setSuccessCount(successCount);
                stats.setFailCount(failCount);
                stats.setAvgLatency(avgLatency);
                stats.setMaxLatency(maxLatency);
                stats.setMinLatency(minLatency);
                stats.setP95Latency(p95Latency);
                stats.setP99Latency(p99Latency);
                stats.setCreatedAt(LocalDateTime.now());
                stats.setUpdatedAt(LocalDateTime.now());
                hourlyMapper.insert(stats);
            }
        }
        
        log.info("[统计聚合] 小时聚合完成: {} 条日志 -> {} 组统计", logs.size(), grouped.size());
    }

    /**
     * 聚合指定日期的天统计
     */
    private void aggregateDailyData(LocalDate date) {
        log.info("[统计聚合] 聚合天数据: {}", date);
        
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        // 查询该日期的调用日志
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(ApiCallLog::getRequestTime, startTime)
               .lt(ApiCallLog::getRequestTime, endTime);
        List<ApiCallLog> logs = callLogMapper.selectList(wrapper);
        
        if (logs.isEmpty()) {
            log.info("[统计聚合] 该日期无调用日志");
            return;
        }
        
        // 按 tenantId + apiPath + appId 分组聚合
        Map<String, List<ApiCallLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(log -> 
                    String.format("%s|%s|%s", 
                        log.getTenantId() != null ? log.getTenantId() : "default",
                        log.getApiPath() != null ? log.getApiPath() : "unknown",
                        log.getAppId() != null ? log.getAppId() : "")));
        
        for (Map.Entry<String, List<ApiCallLog>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\\|", -1);
            String tenantId = parts[0];
            String apiPath = parts[1];
            String appId = parts.length > 2 ? parts[2] : null;
            List<ApiCallLog> groupLogs = entry.getValue();
            
            // 计算统计数据
            long totalCount = groupLogs.size();
            long successCount = groupLogs.stream().filter(l -> Boolean.TRUE.equals(l.getSuccess())).count();
            long failCount = totalCount - successCount;
            
            List<Integer> latencies = groupLogs.stream()
                    .map(ApiCallLog::getLatency)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            
            int avgLatency = latencies.isEmpty() ? 0 : 
                    (int) latencies.stream().mapToInt(Integer::intValue).average().orElse(0);
            int maxLatency = latencies.isEmpty() ? 0 : 
                    latencies.stream().mapToInt(Integer::intValue).max().orElse(0);
            int minLatency = latencies.isEmpty() ? 0 : 
                    latencies.stream().mapToInt(Integer::intValue).min().orElse(0);
            int p95Latency = latencies.isEmpty() ? 0 : 
                    latencies.get((int) (latencies.size() * 0.95));
            int p99Latency = latencies.isEmpty() ? 0 : 
                    latencies.get((int) (latencies.size() * 0.99));
            
            // 查找或创建记录
            LambdaQueryWrapper<ApiCallStatsDaily> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApiCallStatsDaily::getTenantId, tenantId)
                       .eq(ApiCallStatsDaily::getApiPath, apiPath)
                       .eq(ApiCallStatsDaily::getStatDate, date);
            if (appId != null && !appId.isEmpty()) {
                queryWrapper.eq(ApiCallStatsDaily::getAppId, appId);
            } else {
                queryWrapper.isNull(ApiCallStatsDaily::getAppId);
            }
            
            ApiCallStatsDaily existing = dailyMapper.selectOne(queryWrapper);
            
            if (existing != null) {
                // 更新
                existing.setTotalCount(totalCount);
                existing.setSuccessCount(successCount);
                existing.setFailCount(failCount);
                existing.setAvgLatency(avgLatency);
                existing.setMaxLatency(maxLatency);
                existing.setMinLatency(minLatency);
                existing.setP95Latency(p95Latency);
                existing.setP99Latency(p99Latency);
                existing.setUpdatedAt(LocalDateTime.now());
                dailyMapper.updateById(existing);
            } else {
                // 插入
                ApiCallStatsDaily stats = new ApiCallStatsDaily();
                stats.setTenantId(tenantId);
                stats.setApiPath(apiPath);
                stats.setAppId(appId != null && !appId.isEmpty() ? appId : null);
                stats.setStatDate(date);
                stats.setTotalCount(totalCount);
                stats.setSuccessCount(successCount);
                stats.setFailCount(failCount);
                stats.setAvgLatency(avgLatency);
                stats.setMaxLatency(maxLatency);
                stats.setMinLatency(minLatency);
                stats.setP95Latency(p95Latency);
                stats.setP99Latency(p99Latency);
                stats.setCreatedAt(LocalDateTime.now());
                stats.setUpdatedAt(LocalDateTime.now());
                dailyMapper.insert(stats);
            }
        }
        
        log.info("[统计聚合] 天聚合完成: {} 条日志 -> {} 组统计", logs.size(), grouped.size());
    }
}
