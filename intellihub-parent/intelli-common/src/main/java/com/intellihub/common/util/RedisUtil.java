package com.intellihub.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * <p>
 * 提供统一的Redis操作方法，支持字符串、对象、Hash、List、Set等数据类型
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== 通用操作 ====================

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败 - key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示key不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("判断key是否存在失败 - key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除key
     *
     * @param key 键
     * @return 是否成功
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除key失败 - key: {}", key, e);
            return false;
        }
    }

    /**
     * 批量删除key
     *
     * @param keys 键集合
     * @return 删除的数量
     */
    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("批量删除key失败", e);
            return 0L;
        }
    }

    /**
     * 按模式删除key
     *
     * @param pattern 模式（如 user:*）
     * @return 删除的数量
     */
    public Long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
            return 0L;
        } catch (Exception e) {
            log.error("按模式删除key失败 - pattern: {}", pattern, e);
            return 0L;
        }
    }

    // ==================== String操作 ====================

    /**
     * 设置字符串值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("设置字符串值失败 - key: {}", key, e);
        }
    }

    /**
     * 设置字符串值并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置字符串值失败 - key: {}", key, e);
        }
    }

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取字符串值失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 设置对象值（JSON序列化）
     *
     * @param key   键
     * @param value 对象
     */
    public void setObject(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            log.error("序列化对象失败 - key: {}", key, e);
        }
    }

    /**
     * 设置对象值并设置过期时间
     *
     * @param key     键
     * @param value   对象
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            log.error("序列化对象失败 - key: {}", key, e);
        }
    }

    /**
     * 获取对象值
     *
     * @param key   键
     * @param clazz 对象类型
     * @return 对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("反序列化对象失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 如果不存在则设置（原子操作）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("setIfAbsent失败 - key: {}", key, e);
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("递增失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 减量
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("递减失败 - key: {}", key, e);
            return null;
        }
    }

    // ==================== Hash操作 ====================

    /**
     * 设置Hash字段值
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    public void hSet(String key, String field, String value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("设置Hash字段失败 - key: {}, field: {}", key, field, e);
        }
    }

    /**
     * 获取Hash字段值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    public String hGet(String key, String field) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("获取Hash字段失败 - key: {}, field: {}", key, field, e);
            return null;
        }
    }

    /**
     * 批量设置Hash字段
     *
     * @param key 键
     * @param map 字段-值映射
     */
    public void hSetAll(String key, Map<String, String> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("批量设置Hash字段失败 - key: {}", key, e);
        }
    }

    /**
     * 获取所有Hash字段
     *
     * @param key 键
     * @return 字段-值映射
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取所有Hash字段失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 删除Hash字段
     *
     * @param key    键
     * @param fields 字段
     * @return 删除的数量
     */
    public Long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("删除Hash字段失败 - key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 判断Hash字段是否存在
     *
     * @param key   键
     * @param field 字段
     * @return 是否存在
     */
    public Boolean hHasKey(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("判断Hash字段是否存在失败 - key: {}, field: {}", key, field, e);
            return false;
        }
    }

    // ==================== List操作 ====================

    /**
     * 从右边添加元素
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long lRightPush(String key, String value) {
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("List右边添加元素失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 从左边弹出元素
     *
     * @param key 键
     * @return 元素值
     */
    public String lLeftPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("List左边弹出元素失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取列表范围
     *
     * @param key   键
     * @param start 开始索引
     * @param end   结束索引（-1表示到末尾）
     * @return 元素列表
     */
    public List<String> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取List范围失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 长度
     */
    public Long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取List长度失败 - key: {}", key, e);
            return null;
        }
    }

    // ==================== Set操作 ====================

    /**
     * 添加Set元素
     *
     * @param key    键
     * @param values 值
     * @return 添加的数量
     */
    public Long sAdd(String key, String... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("添加Set元素失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取Set所有元素
     *
     * @param key 键
     * @return 元素集合
     */
    public Set<String> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取Set元素失败 - key: {}", key, e);
            return null;
        }
    }

    /**
     * 判断是否是Set成员
     *
     * @param key   键
     * @param value 值
     * @return 是否是成员
     */
    public Boolean sIsMember(String key, String value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("判断Set成员失败 - key: {}", key, e);
            return false;
        }
    }

    /**
     * 移除Set元素
     *
     * @param key    键
     * @param values 值
     * @return 移除的数量
     */
    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("移除Set元素失败 - key: {}", key, e);
            return null;
        }
    }

    // ==================== 发布订阅 ====================

    /**
     * 发布消息到频道
     *
     * @param channel 频道
     * @param message 消息
     */
    public void publish(String channel, String message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("发布消息到频道 - channel: {}, message: {}", channel, message);
        } catch (Exception e) {
            log.error("发布消息失败 - channel: {}", channel, e);
        }
    }

    /**
     * 发布对象消息到频道（JSON序列化）
     *
     * @param channel 频道
     * @param message 消息对象
     */
    public void publishObject(String channel, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, json);
            log.debug("发布对象消息到频道 - channel: {}", channel);
        } catch (Exception e) {
            log.error("发布对象消息失败 - channel: {}", channel, e);
        }
    }
}
