package com.intellihub.aigc.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis限流器
 * 基于令牌桶算法实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_KEY_PREFIX = "aigc:ratelimit:";

    /**
     * Lua脚本实现原子性限流
     */
    private static final String LUA_SCRIPT = 
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = tonumber(redis.call('get', key) or '0') " +
            "if current + 1 > limit then " +
            "    return 0 " +
            "else " +
            "    redis.call('incr', key) " +
            "    redis.call('expire', key, window) " +
            "    return 1 " +
            "end";

    /**
     * 检查是否允许访问（固定窗口）
     *
     * @param key 限流键
     * @param limit 限流次数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        try {
            String redisKey = RATE_LIMIT_KEY_PREFIX + key;
            
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LUA_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(
                    script, 
                    Collections.singletonList(redisKey),
                    String.valueOf(limit),
                    String.valueOf(windowSeconds)
            );
            
            boolean allowed = result != null && result == 1;
            
            if (!allowed) {
                log.warn("限流触发 - key: {}, limit: {}, window: {}s", key, limit, windowSeconds);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("限流检查失败", e);
            // 降级：限流失败时允许通过
            return true;
        }
    }

    /**
     * 按租户限流
     *
     * @param tenantId 租户ID
     * @param limit 限流次数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean tryAcquireByTenant(String tenantId, int limit, int windowSeconds) {
        String key = "tenant:" + tenantId;
        return tryAcquire(key, limit, windowSeconds);
    }

    /**
     * 按用户限流
     *
     * @param userId 用户ID
     * @param limit 限流次数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean tryAcquireByUser(String userId, int limit, int windowSeconds) {
        String key = "user:" + userId;
        return tryAcquire(key, limit, windowSeconds);
    }

    /**
     * 按IP限流
     *
     * @param ip IP地址
     * @param limit 限流次数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许访问
     */
    public boolean tryAcquireByIp(String ip, int limit, int windowSeconds) {
        String key = "ip:" + ip;
        return tryAcquire(key, limit, windowSeconds);
    }

    /**
     * 获取剩余次数
     *
     * @param key 限流键
     * @param limit 限流次数
     * @return 剩余次数
     */
    public int getRemaining(String key, int limit) {
        try {
            String redisKey = RATE_LIMIT_KEY_PREFIX + key;
            Object value = redisTemplate.opsForValue().get(redisKey);
            
            if (value == null) {
                return limit;
            }
            
            int current = Integer.parseInt(String.valueOf(value));
            return Math.max(0, limit - current);
            
        } catch (Exception e) {
            log.error("获取剩余次数失败", e);
            return limit;
        }
    }

    /**
     * 重置限流
     *
     * @param key 限流键
     */
    public void reset(String key) {
        try {
            String redisKey = RATE_LIMIT_KEY_PREFIX + key;
            redisTemplate.delete(redisKey);
            log.info("重置限流 - key: {}", key);
        } catch (Exception e) {
            log.error("重置限流失败", e);
        }
    }
}
