package com.intellihub.gateway.service;

import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.gateway.util.ReactiveRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 限流服务类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ReactiveRedisUtil redisUtil;

    /**
     * 检查是否允许请求（基于Redis的简单限流）
     * <p>
     * 使用固定窗口算法：在时间窗口内限制请求次数。
     * 修复：每次 increment 后检查 TTL，如果没有过期时间则重新设置，
     * 避免 Redis 重启或 key 意外丢失 TTL 导致计数无限累加。
     * </p>
     *
     * @param key        限流Key
     * @param requests   允许的请求数
     * @param window     时间窗口（秒）
     * @return 是否允许
     */
    public Mono<Boolean> isAllowed(String key, int requests, int window) {
        String countKey = key + ":count";

        ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();

        return ops.increment(countKey)
                .flatMap(count -> {
                    // 检查 key 是否有过期时间
                    return redisTemplate.getExpire(countKey)
                            .flatMap(ttl -> {
                                // ttl < 0 表示没有设置过期时间（-1 表示永不过期，-2 表示 key 不存在）
                                if (ttl == null || ttl.getSeconds() < 0) {
                                    // 重新设置过期时间
                                    log.debug("限流Key无过期时间，重新设置 - Key: {}, Window: {}s", countKey, window);
                                    return redisTemplate.expire(countKey, Duration.ofSeconds(window))
                                            .thenReturn(count);
                                }
                                return Mono.just(count);
                            })
                            .defaultIfEmpty(count);
                })
                .map(count -> {
                    boolean allowed = count <= requests;
                    log.debug("限流检查 - Key: {}, 当前计数: {}, 限制: {}, 允许: {}", countKey, count, requests, allowed);
                    return allowed;
                })
                .defaultIfEmpty(true);
    }

    /**
     * 获取当前限流计数
     *
     * @param key 限流Key
     * @return 当前计数
     */
    public Mono<Integer> getCurrentCount(String key) {
        return redisTemplate.opsForValue().get(key + ":count")
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .defaultIfEmpty(0);
    }

    /**
     * 重置限流计数
     *
     * @param key 限流Key
     */
    public Mono<Void> reset(String key) {
        return redisTemplate.delete(key + ":count")
                .then();
    }

    /**
     * 获取剩余请求次数
     *
     * @param key      限流Key
     * @param requests 限制的请求数
     * @return 剩余次数
     */
    public Mono<Integer> getRemainingRequests(String key, int requests) {
        return getCurrentCount(key)
                .map(count -> Math.max(0, requests - count));
    }

    /**
     * 获取限流Key
     *
     * @param type     限流类型（ip、user、path等）
     * @param value    值
     * @return 限流Key
     */
    public String buildKey(String type, String value) {
        return RedisKeyConstants.GATEWAY_RATE_LIMIT_PREFIX + type + ":" + value;
    }

    /**
     * 基于滑动窗口的限流（使用Redis List实现简化版）
     *
     * @param key      限流Key
     * @param limit    限制次数
     * @param window   时间窗口（秒）
     * @return 是否允许
     */
    public Mono<Boolean> isAllowedSlidingWindow(String key, int limit, int window) {
        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000L;
        String listKey = key + ":window";

        // 清理过期记录
        return redisTemplate.opsForList().remove(listKey, 0, 1)
                .then(redisTemplate.opsForList().size(listKey))
                .flatMap(size -> {
                    // 添加当前请求时间戳
                    return redisTemplate.opsForList().rightPush(listKey, String.valueOf(now))
                            .then(redisTemplate.expire(listKey, Duration.ofSeconds(window)))
                            .thenReturn(size + 1);
                })
                .map(size -> {
                    boolean allowed = size <= limit;
                    log.debug("滑动窗口限流 - Key: {}, 当前计数: {}, 限制: {}, 允许: {}", key, size, limit, allowed);
                    return allowed;
                })
                .defaultIfEmpty(true);
    }
}