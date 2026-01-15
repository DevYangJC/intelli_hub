package com.intellihub.gateway.service;

import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.gateway.config.RateLimitConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 限流服务类（重构版）
 * <p>
 * 修复问题：
 * 1. 分离检查和增加计数逻辑，避免重复计数
 * 2. 实现正确的滑动窗口算法（基于Redis Sorted Set）
 * 3. 实现令牌桶算法（基于Redis + Lua脚本）
 * 4. 支持根据配置选择限流算法
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    /**
     * 检查是否允许请求（只检查不增加计数）
     *
     * @param key      限流Key
     * @param requests 允许的请求数
     * @param window   时间窗口（秒）
     * @return RateLimitResult 包含是否允许、剩余次数等信息
     */
    public Mono<RateLimitResult> checkLimit(String key, int requests, int window) {
        RateLimitConfig.Algorithm algorithm = rateLimitConfig.getAlgorithm();
        
        switch (algorithm) {
            case SLIDING_WINDOW:
                return checkSlidingWindow(key, requests, window);
            case TOKEN_BUCKET:
                return checkTokenBucket(key, requests, window);
            case FIXED_WINDOW:
            default:
                return checkFixedWindow(key, requests, window);
        }
    }

    /**
     * 增加计数（在所有检查通过后调用）
     *
     * @param key    限流Key
     * @param window 时间窗口（秒）
     * @return void
     */
    public Mono<Void> incrementCounter(String key, int window) {
        RateLimitConfig.Algorithm algorithm = rateLimitConfig.getAlgorithm();
        
        switch (algorithm) {
            case SLIDING_WINDOW:
                return incrementSlidingWindow(key, window);
            case TOKEN_BUCKET:
                return Mono.empty(); // 令牌桶在检查时已经消费令牌
            case FIXED_WINDOW:
            default:
                return incrementFixedWindow(key, window);
        }
    }

    // ==================== 固定窗口算法 ====================

    /**
     * 固定窗口算法 - 检查
     */
    private Mono<RateLimitResult> checkFixedWindow(String key, int requests, int window) {
        String countKey = key + ":count";

        return getCurrentCount(key)
                .map(count -> {
                    boolean allowed = count < requests;
                    int remaining = Math.max(0, requests - count);
                    long resetTime = System.currentTimeMillis() / 1000 + window;
                    
                    log.debug("固定窗口限流检查 - Key: {}, 当前: {}, 限制: {}, 允许: {}", 
                            countKey, count, requests, allowed);
                    
                    return new RateLimitResult(allowed, requests, remaining, resetTime);
                });
    }

    /**
     * 固定窗口算法 - 增加计数
     */
    private Mono<Void> incrementFixedWindow(String key, int window) {
        String countKey = key + ":count";
        ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();

        return ops.increment(countKey)
                .flatMap(count -> {
                    if (count == 1) {
                        // 第一次请求，设置过期时间
                                    return redisTemplate.expire(countKey, Duration.ofSeconds(window))
                                .then();
                    }
                    return Mono.empty();
                })
                .then();
    }

    // ==================== 滑动窗口算法（基于Redis Sorted Set）====================

    /**
     * 滑动窗口算法 - 检查（使用Lua脚本保证原子性）
     */
    private Mono<RateLimitResult> checkSlidingWindow(String key, int limit, int window) {
        String zsetKey = key + ":sliding";
        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000L;

        // Lua脚本：移除过期数据 + 统计当前窗口内的请求数
        String luaScript = 
            "local key = KEYS[1]\n" +
            "local windowStart = tonumber(ARGV[1])\n" +
            "local now = tonumber(ARGV[2])\n" +
            "local limit = tonumber(ARGV[3])\n" +
            "\n" +
            "-- 移除窗口外的过期数据\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)\n" +
            "\n" +
            "-- 统计当前窗口内的请求数\n" +
            "local count = redis.call('ZCARD', key)\n" +
            "\n" +
            "-- 返回：当前计数\n" +
            "return count";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        List<String> keys = Arrays.asList(zsetKey);
        List<String> args = Arrays.asList(
            String.valueOf(windowStart),
            String.valueOf(now),
            String.valueOf(limit)
        );

        return redisTemplate.execute(script, keys, args)
                .next()
                .map(count -> {
                    boolean allowed = count < limit;
                    int remaining = Math.max(0, limit - count.intValue());
                    long resetTime = System.currentTimeMillis() / 1000 + window;
                    
                    log.debug("滑动窗口限流检查 - Key: {}, 当前: {}, 限制: {}, 允许: {}", 
                            zsetKey, count, limit, allowed);
                    
                    return new RateLimitResult(allowed, limit, remaining, resetTime);
                })
                .defaultIfEmpty(new RateLimitResult(true, limit, limit, System.currentTimeMillis() / 1000 + window));
    }

    /**
     * 滑动窗口算法 - 增加计数
     */
    private Mono<Void> incrementSlidingWindow(String key, int window) {
        String zsetKey = key + ":sliding";
        long now = System.currentTimeMillis();
        
        // 添加当前请求时间戳（使用纳秒避免重复）
        String member = now + ":" + System.nanoTime();
        
        return redisTemplate.opsForZSet()
                .add(zsetKey, member, now)
                .then(redisTemplate.expire(zsetKey, Duration.ofSeconds(window + 1)))
                .then();
    }

    // ==================== 令牌桶算法（基于Redis + Lua脚本）====================

    /**
     * 令牌桶算法 - 检查并消费令牌（原子操作）
     * <p>
     * capacity: 桶容量（最大令牌数）
     * refillRate: 每秒补充的令牌数 = capacity / window
     * </p>
     */
    private Mono<RateLimitResult> checkTokenBucket(String key, int capacity, int window) {
        String bucketKey = key + ":bucket";
        long now = System.currentTimeMillis();
        double refillRate = (double) capacity / window;

        // Lua脚本保证原子性
        String luaScript = 
            "local key = KEYS[1]\n" +
            "local capacity = tonumber(ARGV[1])\n" +
            "local refillRate = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "\n" +
            "local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')\n" +
            "local tokens = tonumber(bucket[1]) or capacity\n" +
            "local lastRefill = tonumber(bucket[2]) or now\n" +
            "\n" +
            "-- 计算应该添加的令牌数\n" +
            "local elapsedSeconds = (now - lastRefill) / 1000\n" +
            "local tokensToAdd = math.floor(elapsedSeconds * refillRate)\n" +
            "tokens = math.min(capacity, tokens + tokensToAdd)\n" +
            "\n" +
            "-- 尝试消费1个令牌\n" +
            "local allowed = 0\n" +
            "if tokens >= 1 then\n" +
            "    tokens = tokens - 1\n" +
            "    redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', now)\n" +
            "    redis.call('EXPIRE', key, 3600)\n" +
            "    allowed = 1\n" +
            "end\n" +
            "\n" +
            "-- 返回：是否允许,剩余令牌数\n" +
            "return {allowed, tokens}";

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(List.class);

        List<String> keys = Arrays.asList(bucketKey);
        List<String> args = Arrays.asList(
            String.valueOf(capacity),
            String.valueOf(refillRate),
            String.valueOf(now)
        );

        return redisTemplate.execute(script, keys, args)
                .collectList()
                .flatMap(resultList -> {
                    if (resultList.isEmpty()) {
                        return Mono.just(new RateLimitResult(true, capacity, capacity, 
                                System.currentTimeMillis() / 1000 + window));
    }
                    
                    List<?> results = (List<?>) resultList.get(0);
                    long allowed = results.size() > 0 ? ((Number) results.get(0)).longValue() : 0L;
                    long tokens = results.size() > 1 ? ((Number) results.get(1)).longValue() : 0L;
                    
                    boolean isAllowed = allowed == 1;
                    int remaining = (int) tokens;
                    long resetTime = System.currentTimeMillis() / 1000 + 
                            (refillRate > 0 ? (int) (remaining / refillRate) : window);
                    
                    log.debug("令牌桶限流检查 - Key: {}, 允许: {}, 剩余令牌: {}", 
                            bucketKey, isAllowed, remaining);
                    
                    return Mono.just(new RateLimitResult(isAllowed, capacity, remaining, resetTime));
                });
    }

    // ==================== 辅助方法 ====================

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
     * @param type  限流类型（ip、user、path等）
     * @param value 值
     * @return 限流Key
     */
    public String buildKey(String type, String value) {
        return RedisKeyConstants.GATEWAY_RATE_LIMIT_PREFIX + type + ":" + value;
    }

    // ==================== 兼容旧接口（标记为废弃）====================

    /**
     * @deprecated 使用 checkLimit() 和 incrementCounter() 替代
     */
    @Deprecated
    public Mono<Boolean> isAllowed(String key, int requests, int window) {
        return checkLimit(key, requests, window)
                .flatMap(result -> {
                    if (result.isAllowed()) {
                        return incrementCounter(key, window)
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }

    /**
     * 限流结果封装类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitResult {
        /**
         * 是否允许请求
         */
        private boolean allowed;

        /**
         * 限流配额
         */
        private int limit;

        /**
         * 剩余请求次数
         */
        private int remaining;

        /**
         * 重置时间（Unix时间戳，秒）
         */
        private long resetTime;
    }
}