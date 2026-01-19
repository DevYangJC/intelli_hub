package com.intellihub.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.kafka.constant.KafkaTopics;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 调用日志上报服务
 * <p>
 * 将API调用日志异步上报到Kafka，由Governance服务消费
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
    private final KafkaMessageProducer kafkaMessageProducer;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

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

                // 2. 发送到Kafka（供Governance服务消费）
                kafkaMessageProducer.send(KafkaTopics.CALL_LOG, logData);

                // 3. 更新实时统计（Redis）
                updateRealtimeStats(tenantId, apiPath, success, latency);

                log.debug("调用日志上报成功 - path: {}, latency: {}ms", apiPath, latency);
            } catch (Exception e) {
                log.error("调用日志上报失败 - path: {}", apiPath, e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    /**
     * 更新告警相关的 Redis 数据
     * <p>
     * 新数据结构:
     * 1. alert:requests:{tenantId}:{hour} - List，存储每个请求的JSON详情
     * 2. alert:stats:{tenantId}:{hour} - Hash，存储统计汇总
     * </p>
     */
    private void updateRealtimeStats(String tenantId, String apiPath, Boolean success, Integer latency) {
        String hour = LocalDateTime.now().format(HOUR_FORMATTER);
        String tenant = tenantId != null ? tenantId : "default";
        Duration ttl = Duration.ofSeconds(RedisKeyConstants.TTL_ALERT_DATA);
        
        log.debug("[Gateway Redis] tenantId={}, hour={}, apiPath={}, success={}, latency={}", 
                tenant, hour, apiPath, success, latency);

        // 1. 构建请求详情
        String requestsKey = RedisKeyConstants.buildAlertRequestsKey(tenant, hour);
        Map<String, Object> requestDetail = new HashMap<>();
        requestDetail.put("requestId", UUID.randomUUID().toString());
        requestDetail.put("apiPath", apiPath);
        requestDetail.put("statusCode", success != null && success ? 200 : 500);
        requestDetail.put("success", success);
        requestDetail.put("latency", latency);
        requestDetail.put("timestamp", LocalDateTime.now().toString());

        // 2. 统计 Key
        String statsKey = RedisKeyConstants.buildAlertStatsKey(tenant, hour);
        String minute = LocalDateTime.now().format(MINUTE_FORMATTER);
        String qpsKey = RedisKeyConstants.buildQpsKey(tenant, minute);
        Duration qpsTtl = Duration.ofSeconds(RedisKeyConstants.TTL_QPS_DATA);

        try {
            String requestJson = objectMapper.writeValueAsString(requestDetail);

            // 3. 构建所有 Redis 操作（原子化执行）
            Mono<Long> pushMono = redisTemplate.opsForList().rightPush(requestsKey, requestJson)
                    .flatMap(v -> redisTemplate.expire(requestsKey, ttl).thenReturn(v));

            Mono<Long> totalCountMono = redisTemplate.opsForHash().increment(statsKey, "totalCount", 1)
                    .flatMap(v -> redisTemplate.expire(statsKey, ttl).thenReturn(v));

            Mono<Long> successOrFailMono = Boolean.TRUE.equals(success)
                    ? redisTemplate.opsForHash().increment(statsKey, "successCount", 1)
                    : redisTemplate.opsForHash().increment(statsKey, "failCount", 1);

            Mono<Long> latencyMono = latency != null
                    ? redisTemplate.opsForHash().increment(statsKey, "latencySum", latency)
                    : Mono.just(0L);

            Mono<Long> qpsMono = redisTemplate.opsForValue().increment(qpsKey)
                    .flatMap(v -> redisTemplate.expire(qpsKey, qpsTtl).thenReturn(v));

            // 4. 原子化执行所有操作
            Mono.when(pushMono, totalCountMono, successOrFailMono, latencyMono, qpsMono)
                    .doOnSuccess(v -> log.debug("[Gateway Redis] 统计更新成功 - tenant={}, apiPath={}", tenant, apiPath))
                    .doOnError(e -> log.error("[Gateway Redis] 统计更新失败 - tenant={}, apiPath={}", tenant, apiPath, e))
                    .subscribe();

        } catch (JsonProcessingException e) {
            log.error("[Gateway Redis] JSON序列化失败", e);
        }
    }
}
