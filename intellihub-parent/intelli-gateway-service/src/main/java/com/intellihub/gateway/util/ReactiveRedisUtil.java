package com.intellihub.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 响应式Redis工具类
 * <p>
 * 用于网关等响应式服务的Redis操作，基于ReactiveStringRedisTemplate
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveRedisUtil {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== String操作 ====================

    /**
     * 设置字符串值
     */
    public Mono<Boolean> set(String key, String value) {
        return redisTemplate.opsForValue().set(key, value)
                .doOnError(e -> log.error("设置字符串值失败 - key: {}", key, e))
                .onErrorReturn(false);
    }

    /**
     * 设置字符串值并设置过期时间
     */
    public Mono<Boolean> set(String key, String value, Duration timeout) {
        return redisTemplate.opsForValue().set(key, value, timeout)
                .doOnError(e -> log.error("设置字符串值失败 - key: {}", key, e))
                .onErrorReturn(false);
    }

    /**
     * 设置字符串值并设置过期时间（秒）
     */
    public Mono<Boolean> set(String key, String value, long timeoutSeconds) {
        return set(key, value, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 获取字符串值
     */
    public Mono<String> get(String key) {
        return redisTemplate.opsForValue().get(key)
                .doOnError(e -> log.error("获取字符串值失败 - key: {}", key, e))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * 如果不存在则设置（原子操作，用于分布式锁、防重放等）
     */
    public Mono<Boolean> setIfAbsent(String key, String value, Duration timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout)
                .doOnError(e -> log.error("setIfAbsent失败 - key: {}", key, e))
                .onErrorReturn(false);
    }

    /**
     * 如果不存在则设置（秒）
     */
    public Mono<Boolean> setIfAbsent(String key, String value, long timeoutSeconds) {
        return setIfAbsent(key, value, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 递增
     */
    public Mono<Long> increment(String key) {
        return redisTemplate.opsForValue().increment(key)
                .doOnError(e -> log.error("递增失败 - key: {}", key, e))
                .onErrorReturn(0L);
    }

    /**
     * 递增指定值
     */
    public Mono<Long> increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta)
                .doOnError(e -> log.error("递增失败 - key: {}", key, e))
                .onErrorReturn(0L);
    }

    // ==================== 对象操作 ====================

    /**
     * 设置对象值（JSON序列化）
     */
    public Mono<Boolean> setObject(String key, Object value, Duration timeout) {
        try {
            String json = objectMapper.writeValueAsString(value);
            return set(key, json, timeout);
        } catch (JsonProcessingException e) {
            log.error("序列化对象失败 - key: {}", key, e);
            return Mono.just(false);
        }
    }

    /**
     * 设置对象值（秒）
     */
    public Mono<Boolean> setObject(String key, Object value, long timeoutSeconds) {
        return setObject(key, value, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 获取对象值
     */
    public <T> Mono<T> getObject(String key, Class<T> clazz) {
        return get(key)
                .flatMap(json -> {
                    if (json == null || json.isEmpty()) {
                        return Mono.empty();
                    }
                    try {
                        return Mono.just(objectMapper.readValue(json, clazz));
                    } catch (JsonProcessingException e) {
                        log.error("反序列化对象失败 - key: {}", key, e);
                        return Mono.empty();
                    }
                });
    }

    // ==================== 通用操作 ====================

    /**
     * 删除key
     */
    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnError(e -> log.error("删除key失败 - key: {}", key, e))
                .onErrorReturn(false);
    }

    /**
     * 设置过期时间
     */
    public Mono<Boolean> expire(String key, Duration timeout) {
        return redisTemplate.expire(key, timeout)
                .doOnError(e -> log.error("设置过期时间失败 - key: {}", key, e))
                .onErrorReturn(false);
    }

    /**
     * 设置过期时间（秒）
     */
    public Mono<Boolean> expire(String key, long timeoutSeconds) {
        return expire(key, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 判断key是否存在
     */
    public Mono<Boolean> hasKey(String key) {
        return redisTemplate.hasKey(key)
                .doOnError(e -> log.error("判断key是否存在失败 - key: {}", key, e))
                .onErrorReturn(false);
    }

    /**
     * 获取过期时间（秒）
     */
    public Mono<Long> getExpire(String key) {
        return redisTemplate.getExpire(key)
                .map(Duration::getSeconds)
                .doOnError(e -> log.error("获取过期时间失败 - key: {}", key, e))
                .onErrorReturn(-1L);
    }

    // ==================== 发布订阅 ====================

    /**
     * 发布消息到频道
     */
    public Mono<Long> publish(String channel, String message) {
        return redisTemplate.convertAndSend(channel, message)
                .doOnSuccess(count -> log.debug("发布消息到频道 - channel: {}, receivers: {}", channel, count))
                .doOnError(e -> log.error("发布消息失败 - channel: {}", channel, e))
                .onErrorReturn(0L);
    }

    /**
     * 发布对象消息到频道（JSON序列化）
     */
    public Mono<Long> publishObject(String channel, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            return publish(channel, json);
        } catch (JsonProcessingException e) {
            log.error("序列化消息失败 - channel: {}", channel, e);
            return Mono.just(0L);
        }
    }
}
