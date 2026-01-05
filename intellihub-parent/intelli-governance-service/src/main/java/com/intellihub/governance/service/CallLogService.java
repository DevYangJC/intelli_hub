package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.page.PageData;
import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.governance.dto.CallLogDTO;
import com.intellihub.governance.entity.ApiCallLog;
import com.intellihub.governance.mapper.ApiCallLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.intellihub.context.UserContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 调用日志服务
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallLogService {

    private final ApiCallLogMapper callLogMapper;
    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 保存调用日志（异步）
     * <p>
     * 注意：Redis 实时统计由 Gateway 负责写入，这里只保存到数据库
     * 避免 Gateway 和 Governance 双重写入导致统计数据翻倍
     * </p>
     */
    @Async
    public void saveCallLog(CallLogDTO dto) {
        try {
            // 保存到数据库
            ApiCallLog apiCallLog = new ApiCallLog();
            BeanUtils.copyProperties(dto, apiCallLog);
            apiCallLog.setCreatedAt(LocalDateTime.now());
            callLogMapper.insert(apiCallLog);

            log.info("调用日志保存成功 - path: {}, latency: {}ms", dto.getApiPath(), dto.getLatency());
        } catch (Exception e) {
            log.error("保存调用日志失败", e);
        }
    }

    /**
     * 更新实时统计（Redis）
     */
    private void updateRealtimeStats(CallLogDTO dto) {
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String tenantId = dto.getTenantId() != null ? dto.getTenantId() : "default";
        String apiPath = dto.getApiPath();
        long ttlSeconds = RedisKeyConstants.TTL_STATS;

        // 总调用数
        String totalKey = RedisKeyConstants.buildStatsTotalKey(tenantId, apiPath, hour);
        redisTemplate.opsForValue().increment(totalKey);
        redisTemplate.expire(totalKey, ttlSeconds, TimeUnit.SECONDS);

        // 成功/失败数
        if (Boolean.TRUE.equals(dto.getSuccess())) {
            String successKey = RedisKeyConstants.buildStatsSuccessKey(tenantId, apiPath, hour);
            redisTemplate.opsForValue().increment(successKey);
            redisTemplate.expire(successKey, ttlSeconds, TimeUnit.SECONDS);
        } else {
            String failKey = RedisKeyConstants.buildStatsFailKey(tenantId, apiPath, hour);
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, ttlSeconds, TimeUnit.SECONDS);
        }

        // 延迟统计（使用List存储，后续计算P95/P99）
        if (dto.getLatency() != null) {
            String latencyKey = RedisKeyConstants.buildStatsKeyPrefix(tenantId, apiPath, hour) + ":latency";
            redisTemplate.opsForList().rightPush(latencyKey, String.valueOf(dto.getLatency()));
            redisTemplate.expire(latencyKey, ttlSeconds, TimeUnit.SECONDS);
        }

        // 全局统计 - 总数
        String globalTotalKey = RedisKeyConstants.buildStatsTotalKey(tenantId, "global", hour);
        redisTemplate.opsForValue().increment(globalTotalKey);
        redisTemplate.expire(globalTotalKey, ttlSeconds, TimeUnit.SECONDS);

        // 全局统计 - 成功/失败数（供告警检测使用）
        if (Boolean.TRUE.equals(dto.getSuccess())) {
            String globalSuccessKey = RedisKeyConstants.buildStatsSuccessKey(tenantId, "global", hour);
            redisTemplate.opsForValue().increment(globalSuccessKey);
            redisTemplate.expire(globalSuccessKey, ttlSeconds, TimeUnit.SECONDS);
        } else {
            String globalFailKey = RedisKeyConstants.buildStatsFailKey(tenantId, "global", hour);
            redisTemplate.opsForValue().increment(globalFailKey);
            redisTemplate.expire(globalFailKey, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 分页查询调用日志
     * <p>租户ID由多租户拦截器自动处理</p>
     */
    public PageData<ApiCallLog> pageCallLogs(String apiId, String apiPath, String appId,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          Boolean success, int page, int size) {
        // 租户条件由拦截器自动添加
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(apiId)) {
            wrapper.eq(ApiCallLog::getApiId, apiId);
        }
        if (StringUtils.hasText(apiPath)) {
            wrapper.like(ApiCallLog::getApiPath, apiPath);
        }
        if (StringUtils.hasText(appId)) {
            wrapper.eq(ApiCallLog::getAppId, appId);
        }
        if (startTime != null) {
            wrapper.ge(ApiCallLog::getRequestTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(ApiCallLog::getRequestTime, endTime);
        }
        if (success != null) {
            wrapper.eq(ApiCallLog::getSuccess, success);
        }
        
        wrapper.orderByDesc(ApiCallLog::getRequestTime);
        Page<ApiCallLog> apiCallLogPage = callLogMapper.selectPage(new Page<>(page, size), wrapper);
        return PageData.of(apiCallLogPage.getRecords(), apiCallLogPage.getTotal(), apiCallLogPage.getSize(), apiCallLogPage.getCurrent());
    }

    /**
     * 获取实时调用数
     */
    public Long getRealtimeCount(String apiPath) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String key = RedisKeyConstants.buildStatsTotalKey(tenantId, apiPath, hour);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 获取全局实时调用数
     */
    public Long getGlobalRealtimeCount() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String key = RedisKeyConstants.buildStatsTotalKey(tenantId, "global", hour);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }
}
