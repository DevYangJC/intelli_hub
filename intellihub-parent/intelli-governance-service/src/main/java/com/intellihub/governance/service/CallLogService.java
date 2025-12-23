package com.intellihub.governance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    private static final String STATS_KEY_PREFIX = "stats:realtime:";
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 保存调用日志（异步）
     */
    @Async
    public void saveCallLog(CallLogDTO dto) {
        try {
            // 1. 保存到数据库
            ApiCallLog apiCallLog = new ApiCallLog();
            BeanUtils.copyProperties(dto, apiCallLog);
            apiCallLog.setCreatedAt(LocalDateTime.now());
            callLogMapper.insert(apiCallLog);

            // 2. 更新实时统计
            updateRealtimeStats(dto);

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

        // 总调用数
        String totalKey = STATS_KEY_PREFIX + tenantId + ":" + apiPath + ":" + hour + ":total";
        redisTemplate.opsForValue().increment(totalKey);
        redisTemplate.expire(totalKey, 3, TimeUnit.HOURS);

        // 成功/失败数
        if (Boolean.TRUE.equals(dto.getSuccess())) {
            String successKey = STATS_KEY_PREFIX + tenantId + ":" + apiPath + ":" + hour + ":success";
            redisTemplate.opsForValue().increment(successKey);
            redisTemplate.expire(successKey, 3, TimeUnit.HOURS);
        } else {
            String failKey = STATS_KEY_PREFIX + tenantId + ":" + apiPath + ":" + hour + ":fail";
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, 3, TimeUnit.HOURS);
        }

        // 延迟统计（使用List存储，后续计算P95/P99）
        if (dto.getLatency() != null) {
            String latencyKey = STATS_KEY_PREFIX + tenantId + ":" + apiPath + ":" + hour + ":latency";
            redisTemplate.opsForList().rightPush(latencyKey, String.valueOf(dto.getLatency()));
            redisTemplate.expire(latencyKey, 3, TimeUnit.HOURS);
        }

        // 全局统计
        String globalTotalKey = STATS_KEY_PREFIX + tenantId + ":global:" + hour + ":total";
        redisTemplate.opsForValue().increment(globalTotalKey);
        redisTemplate.expire(globalTotalKey, 3, TimeUnit.HOURS);
    }

    /**
     * 分页查询调用日志
     */
    public IPage<ApiCallLog> pageCallLogs(String tenantId, String apiId, String appId,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          Boolean success, int page, int size) {
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(tenantId)) {
            wrapper.eq(ApiCallLog::getTenantId, tenantId);
        }
        if (StringUtils.hasText(apiId)) {
            wrapper.eq(ApiCallLog::getApiId, apiId);
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
        
        return callLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取实时调用数
     */
    public Long getRealtimeCount(String tenantId, String apiPath) {
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String key = STATS_KEY_PREFIX + tenantId + ":" + apiPath + ":" + hour + ":total";
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 获取全局实时调用数
     */
    public Long getGlobalRealtimeCount(String tenantId) {
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String key = STATS_KEY_PREFIX + tenantId + ":global:" + hour + ":total";
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }
}
