package com.intellihub.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.common.event.ApiRouteChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * API路由事件发布器
 * <p>
 * 在API发布、下线、更新时发布事件，通知网关刷新路由配置
 * 使用Redis发布订阅机制
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRouteEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发布API发布事件
     *
     * @param apiId    API ID
     * @param path     API路径
     * @param method   请求方法
     * @param tenantId 租户ID
     */
    public void publishApiPublished(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = ApiRouteChangeEvent.publish(apiId, path, method, tenantId);
        publishEvent(event);
        log.info("发布API发布事件 - apiId: {}, path: {}", apiId, path);
    }

    /**
     * 发布API下线事件
     *
     * @param apiId    API ID
     * @param path     API路径
     * @param method   请求方法
     * @param tenantId 租户ID
     */
    public void publishApiOffline(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = ApiRouteChangeEvent.offline(apiId, path, method, tenantId);
        publishEvent(event);
        log.info("发布API下线事件 - apiId: {}, path: {}", apiId, path);
    }

    /**
     * 发布API更新事件
     *
     * @param apiId    API ID
     * @param path     API路径
     * @param method   请求方法
     * @param tenantId 租户ID
     */
    public void publishApiUpdated(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = ApiRouteChangeEvent.update(apiId, path, method, tenantId);
        publishEvent(event);
        log.info("发布API更新事件 - apiId: {}, path: {}", apiId, path);
    }

    /**
     * 发布全量刷新事件
     */
    public void publishRefreshAll() {
        ApiRouteChangeEvent event = ApiRouteChangeEvent.refreshAll();
        publishEvent(event);
        log.info("发布全量刷新事件");
    }

    /**
     * 发布事件到Redis
     */
    private void publishEvent(ApiRouteChangeEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(ApiRouteChangeEvent.CHANNEL, message);
            log.debug("事件发布成功 - channel: {}, event: {}", ApiRouteChangeEvent.CHANNEL, event.getEventType());
        } catch (Exception e) {
            log.error("事件发布失败 - event: {}", event, e);
        }
    }
}
