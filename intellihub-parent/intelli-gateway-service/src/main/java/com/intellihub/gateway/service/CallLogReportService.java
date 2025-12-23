package com.intellihub.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 调用日志上报服务
 * <p>
 * 将API调用日志异步上报到Redis，由Governance服务消费
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallLogReportService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CALL_LOG_QUEUE = "call_log:queue";
    private static final String STATS_KEY_PREFIX = "stats:realtime:";
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 上报调用日志（异步）
     */
    public void reportCallLog(String tenantId, String apiId, String apiPath, String apiMethod,
                              String appId, String appKey, String clientIp,
                              Integer statusCode, Boolean success, Integer latency,
                              String errorMessage, String userAgent) {
        
        Mono.fromRunnable(() -> {
            try {
                // 1. 构建日志对象
                Map<String, Object> logData = new HashMap<>();
                logData.put("tenantId", tenantId != null ? tenantId : "default");
                logData.put("apiId", apiId);
                logData.put("apiPath", apiPath);
                logData.put("apiMethod", apiMethod);
                logData.put("appId", appId);
                logData.put("appKey", appKey);
                logData.put("clientIp", clientIp);
                logData.put("statusCode", statusCode);
                logData.put("success", success);
                logData.put("latency", latency);
                logData.put("errorMessage", errorMessage);
                logData.put("userAgent", userAgent);
                logData.put("requestTime", LocalDateTime.now().toString());

                String logJson = objectMapper.writeValueAsString(logData);

                // 2. 推送到Redis队列（供Governance服务消费）
                redisTemplate.opsForList().rightPush(CALL_LOG_QUEUE, logJson)
                        .subscribe();

                // 3. 更新实时统计
                updateRealtimeStats(tenantId, apiPath, success, latency);

                log.debug("调用日志上报成功 - path: {}, latency: {}ms", apiPath, latency);
            } catch (Exception e) {
                log.error("调用日志上报失败 - path: {}", apiPath, e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    /**
     * 更新实时统计
     */
    private void updateRealtimeStats(String tenantId, String apiPath, Boolean success, Integer latency) {
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String tenant = tenantId != null ? tenantId : "default";

        // 总调用数
        String totalKey = STATS_KEY_PREFIX + tenant + ":" + apiPath + ":" + hour + ":total";
        redisTemplate.opsForValue().increment(totalKey)
                .flatMap(v -> redisTemplate.expire(totalKey, java.time.Duration.ofHours(3)))
                .subscribe();

        // 成功/失败数
        if (Boolean.TRUE.equals(success)) {
            String successKey = STATS_KEY_PREFIX + tenant + ":" + apiPath + ":" + hour + ":success";
            redisTemplate.opsForValue().increment(successKey)
                    .flatMap(v -> redisTemplate.expire(successKey, java.time.Duration.ofHours(3)))
                    .subscribe();
        } else {
            String failKey = STATS_KEY_PREFIX + tenant + ":" + apiPath + ":" + hour + ":fail";
            redisTemplate.opsForValue().increment(failKey)
                    .flatMap(v -> redisTemplate.expire(failKey, java.time.Duration.ofHours(3)))
                    .subscribe();
        }

        // 全局统计
        String globalKey = STATS_KEY_PREFIX + tenant + ":global:" + hour + ":total";
        redisTemplate.opsForValue().increment(globalKey)
                .flatMap(v -> redisTemplate.expire(globalKey, java.time.Duration.ofHours(3)))
                .subscribe();
    }
}
