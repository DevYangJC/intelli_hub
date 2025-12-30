package com.intellihub.search.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "search:lock:";
    private static final long DEFAULT_EXPIRE_SECONDS = 300;

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的 key
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long expireSeconds) {
        String key = LOCK_PREFIX + lockKey;
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(key, String.valueOf(System.currentTimeMillis()), expireSeconds, TimeUnit.SECONDS);
            boolean locked = Boolean.TRUE.equals(result);
            if (locked) {
                log.debug("获取分布式锁成功: {}", key);
            } else {
                log.debug("获取分布式锁失败（已被其他实例持有）: {}", key);
            }
            return locked;
        } catch (Exception e) {
            log.error("获取分布式锁异常: {}", key, e);
            return false;
        }
    }

    /**
     * 尝试获取锁（使用默认过期时间）
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 释放锁
     */
    public void unlock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        try {
            redisTemplate.delete(key);
            log.debug("释放分布式锁: {}", key);
        } catch (Exception e) {
            log.error("释放分布式锁异常: {}", key, e);
        }
    }

    /**
     * 检查锁是否存在
     */
    public boolean isLocked(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
