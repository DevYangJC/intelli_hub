package com.intellihub.gateway.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.event.ApiRouteChangeEvent;
import com.intellihub.gateway.service.AppKeyService;
import com.intellihub.gateway.service.OpenApiRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 路由刷新监听器
 * <p>
 * 处理Redis Pub/Sub消息，刷新本地缓存
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component("apiRouteChangeListener")
@RequiredArgsConstructor
public class ApiRouteChangeListener {

    private static final String MSG_REFRESH_ALL = "ALL";

    private final OpenApiRouteService openApiRouteService;
    private final AppKeyService appKeyService;
    private final ObjectMapper objectMapper;

    /**
     * 处理路由变更消息
     * <p>
     * 消息格式：ApiRouteChangeEvent的JSON序列化
     * </p>
     */
    public void onRouteChange(String message) {
        if (message == null || message.isEmpty()) {
            log.warn("收到空的路由变更消息");
            return;
        }

        try {
            // 解析JSON消息
            ApiRouteChangeEvent event = objectMapper.readValue(message, ApiRouteChangeEvent.class);
            
            switch (event.getEventType()) {
                case REFRESH_ALL:
                    log.info("执行全量路由刷新");
                    openApiRouteService.refreshAllRoutes();
                    break;
                    
                case PUBLISH:
                case UPDATE:
                    log.info("API发布/更新，刷新路由 - apiId: {}, path: {}", 
                            event.getApiId(), event.getPath());
                    openApiRouteService.refreshRoute(event.getApiId()).subscribe();
                    break;
                    
                case OFFLINE:
                case DELETE:
                    log.info("API下线/删除，移除路由 - apiId: {}, path: {}", 
                            event.getApiId(), event.getPath());
                    openApiRouteService.removeRouteByApiId(event.getApiId());
                    break;
                    
                default:
                    log.warn("未知的事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理路由变更消息失败 - message: {}", message, e);
        }
    }

    /**
     * 处理应用状态变更消息
     * <p>
     * 消息格式：
     * - "ALL" - 清除所有AppKey缓存
     * - "{appKey}" - 清除指定AppKey缓存
     * </p>
     */
    public void onAppStatusChange(String message) {
        if (message == null || message.isEmpty()) {
            log.warn("收到空的应用状态变更消息");
            return;
        }

        try {
            if (MSG_REFRESH_ALL.equalsIgnoreCase(message)) {
                log.info("清除所有AppKey缓存");
                appKeyService.clearAllCache().subscribe();
            } else {
                log.info("清除AppKey缓存 - appKey: {}", message);
                appKeyService.clearCache(message).subscribe();
            }
        } catch (Exception e) {
            log.error("处理应用状态变更消息失败 - message: {}", message, e);
        }
    }
}
