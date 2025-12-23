package com.intellihub.gateway.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.common.event.ApiRouteChangeEvent;
import com.intellihub.gateway.service.OpenApiRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * API路由变更事件监听器
 * <p>
 * 监听Redis发布订阅频道，接收API发布/下线事件，刷新网关路由配置
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRouteChangeListener implements MessageListener {

    private final OpenApiRouteService routeService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            log.debug("收到路由变更事件 - message: {}", body);

            ApiRouteChangeEvent event = objectMapper.readValue(body, ApiRouteChangeEvent.class);
            handleEvent(event);

        } catch (Exception e) {
            log.error("处理路由变更事件失败", e);
        }
    }

    /**
     * 处理路由变更事件
     */
    private void handleEvent(ApiRouteChangeEvent event) {
        log.info("处理路由变更事件 - type: {}, apiId: {}, path: {}",
                event.getEventType(), event.getApiId(), event.getPath());

        switch (event.getEventType()) {
            case PUBLISH:
            case UPDATE:
                // 刷新单个API路由
                routeService.refreshRoute(event.getApiId())
                        .doOnSuccess(v -> log.info("路由刷新成功 - apiId: {}", event.getApiId()))
                        .doOnError(e -> log.error("路由刷新失败 - apiId: {}", event.getApiId(), e))
                        .subscribe();
                break;

            case OFFLINE:
            case DELETE:
                // 移除路由
                routeService.removeRoute(event.getPath(), event.getMethod());
                log.info("路由移除成功 - path: {}, method: {}", event.getPath(), event.getMethod());
                break;

            case REFRESH_ALL:
                // 全量刷新
                try {
                    routeService.refreshAllRoutes();
                    log.info("全量路由刷新成功");
                } catch (Exception e) {
                    log.error("全量路由刷新失败", e);
                }
                break;

            default:
                log.warn("未知的事件类型: {}", event.getEventType());
        }
    }
}
