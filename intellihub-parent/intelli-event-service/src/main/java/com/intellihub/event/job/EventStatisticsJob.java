package com.intellihub.event.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.intellihub.event.constant.ConsumeStatus;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventPublishRecord;
import com.intellihub.event.entity.EventStatistics;
import com.intellihub.event.mapper.EventConsumeRecordMapper;
import com.intellihub.event.mapper.EventPublishRecordMapper;
import com.intellihub.event.mapper.EventStatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件统计定时任务
 * 每天凌晨统计前一天的事件发布/消费数据
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatisticsJob {

    private final EventPublishRecordMapper publishRecordMapper;
    private final EventConsumeRecordMapper consumeRecordMapper;
    private final EventStatisticsMapper statisticsMapper;

    /**
     * 每天凌晨1点执行统计
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        doStatistics(yesterday);
    }

    /**
     * 每小时执行一次当天的增量统计（可选，提供近实时统计）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyStatistics() {
        LocalDate today = LocalDate.now();
        doStatistics(today);
    }

    /**
     * 执行统计
     */
    public void doStatistics(LocalDate statDate) {
        log.info("开始执行事件统计: statDate={}", statDate);
        long startTime = System.currentTimeMillis();
        
        try {
            LocalDateTime startOfDay = statDate.atStartOfDay();
            LocalDateTime endOfDay = statDate.atTime(LocalTime.MAX);
            
            // 统计发布记录
            Map<String, PublishStats> publishStatsMap = getPublishStats(startOfDay, endOfDay);
            
            // 统计消费记录
            Map<String, ConsumeStats> consumeStatsMap = getConsumeStats(startOfDay, endOfDay);
            
            // 合并所有事件编码
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(publishStatsMap.keySet());
            allKeys.addAll(consumeStatsMap.keySet());
            
            int updatedCount = 0;
            for (String key : allKeys) {
                String[] parts = key.split(":");
                String tenantId = parts[0];
                String eventCode = parts[1];
                
                PublishStats publishStats = publishStatsMap.getOrDefault(key, new PublishStats());
                ConsumeStats consumeStats = consumeStatsMap.getOrDefault(key, new ConsumeStats());
                
                saveOrUpdateStatistics(tenantId, eventCode, statDate, publishStats, consumeStats);
                updatedCount++;
            }
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("事件统计完成: statDate={}, updatedCount={}, costTime={}ms", 
                    statDate, updatedCount, costTime);
        } catch (Exception e) {
            log.error("事件统计失败: statDate={}", statDate, e);
        }
    }

    /**
     * 获取发布统计
     */
    private Map<String, PublishStats> getPublishStats(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<EventPublishRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(EventPublishRecord::getPublishTime, startTime)
                .lt(EventPublishRecord::getPublishTime, endTime);
        
        List<EventPublishRecord> records = publishRecordMapper.selectList(queryWrapper);
        
        return records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTenantId() + ":" + r.getEventCode(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    PublishStats stats = new PublishStats();
                                    stats.publishCount = list.size();
                                    return stats;
                                }
                        )
                ));
    }

    /**
     * 获取消费统计
     */
    private Map<String, ConsumeStats> getConsumeStats(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<EventConsumeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(EventConsumeRecord::getConsumeTime, startTime)
                .lt(EventConsumeRecord::getConsumeTime, endTime);
        
        List<EventConsumeRecord> records = consumeRecordMapper.selectList(queryWrapper);
        
        return records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTenantId() + ":" + r.getEventCode(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    ConsumeStats stats = new ConsumeStats();
                                    stats.consumeCount = list.size();
                                    stats.successCount = (int) list.stream()
                                            .filter(r -> ConsumeStatus.SUCCESS.getCode().equals(r.getStatus()))
                                            .count();
                                    stats.failedCount = (int) list.stream()
                                            .filter(r -> ConsumeStatus.FAILED.getCode().equals(r.getStatus()))
                                            .count();
                                    
                                    // 计算平均耗时和最大耗时
                                    List<Integer> costTimes = list.stream()
                                            .filter(r -> r.getCostTime() != null && r.getCostTime() > 0)
                                            .map(EventConsumeRecord::getCostTime)
                                            .collect(Collectors.toList());
                                    
                                    if (!costTimes.isEmpty()) {
                                        stats.avgCostTime = (int) costTimes.stream()
                                                .mapToInt(Integer::intValue)
                                                .average()
                                                .orElse(0);
                                        stats.maxCostTime = costTimes.stream()
                                                .mapToInt(Integer::intValue)
                                                .max()
                                                .orElse(0);
                                    }
                                    return stats;
                                }
                        )
                ));
    }

    /**
     * 保存或更新统计数据
     */
    private void saveOrUpdateStatistics(String tenantId, String eventCode, LocalDate statDate,
                                         PublishStats publishStats, ConsumeStats consumeStats) {
        // 查询是否已存在
        LambdaQueryWrapper<EventStatistics> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EventStatistics::getTenantId, tenantId)
                .eq(EventStatistics::getEventCode, eventCode)
                .eq(EventStatistics::getStatDate, statDate);
        
        EventStatistics existing = statisticsMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            // 更新
            LambdaUpdateWrapper<EventStatistics> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(EventStatistics::getId, existing.getId())
                    .set(EventStatistics::getPublishCount, publishStats.publishCount)
                    .set(EventStatistics::getConsumeCount, consumeStats.consumeCount)
                    .set(EventStatistics::getSuccessCount, consumeStats.successCount)
                    .set(EventStatistics::getFailedCount, consumeStats.failedCount)
                    .set(EventStatistics::getAvgCostTime, consumeStats.avgCostTime)
                    .set(EventStatistics::getMaxCostTime, consumeStats.maxCostTime)
                    .set(EventStatistics::getUpdatedAt, LocalDateTime.now());
            statisticsMapper.update(null, updateWrapper);
        } else {
            // 插入
            EventStatistics statistics = new EventStatistics();
            statistics.setTenantId(tenantId);
            statistics.setEventCode(eventCode);
            statistics.setStatDate(statDate);
            statistics.setPublishCount((long) publishStats.publishCount);
            statistics.setConsumeCount((long) consumeStats.consumeCount);
            statistics.setSuccessCount((long) consumeStats.successCount);
            statistics.setFailedCount((long) consumeStats.failedCount);
            statistics.setAvgCostTime(consumeStats.avgCostTime);
            statistics.setMaxCostTime(consumeStats.maxCostTime);
            statistics.setCreatedAt(LocalDateTime.now());
            statistics.setUpdatedAt(LocalDateTime.now());
            statisticsMapper.insert(statistics);
        }
    }

    /**
     * 发布统计内部类
     */
    private static class PublishStats {
        int publishCount = 0;
    }

    /**
     * 消费统计内部类
     */
    private static class ConsumeStats {
        int consumeCount = 0;
        int successCount = 0;
        int failedCount = 0;
        int avgCostTime = 0;
        int maxCostTime = 0;
    }
}
