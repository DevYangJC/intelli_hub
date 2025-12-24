package com.intellihub.event;

import lombok.Data;

import java.io.Serializable;

/**
 * API路由变更事件
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiRouteChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Redis发布订阅频道
     */
    public static final String CHANNEL = "intellihub:channel:api:route:change";

    /**
     * 事件类型枚举
     */
    public enum EventType {
        REFRESH_ALL,
        PUBLISH,
        UPDATE,
        OFFLINE,
        DELETE
    }

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * API ID
     */
    private String apiId;

    /**
     * API路径
     */
    private String path;

    /**
     * API方法
     */
    private String method;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 时间戳
     */
    private long timestamp;

    public ApiRouteChangeEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiRouteChangeEvent(EventType eventType) {
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiRouteChangeEvent(EventType eventType, String apiId) {
        this.eventType = eventType;
        this.apiId = apiId;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiRouteChangeEvent(EventType eventType, String apiId, String path) {
        this.eventType = eventType;
        this.apiId = apiId;
        this.path = path;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 静态工厂方法 ====================

    public static ApiRouteChangeEvent refreshAll() {
        return new ApiRouteChangeEvent(EventType.REFRESH_ALL);
    }

    public static ApiRouteChangeEvent publish(String apiId, String path) {
        return new ApiRouteChangeEvent(EventType.PUBLISH, apiId, path);
    }

    public static ApiRouteChangeEvent publish(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = new ApiRouteChangeEvent(EventType.PUBLISH, apiId, path);
        event.setMethod(method);
        event.setTenantId(tenantId);
        return event;
    }

    public static ApiRouteChangeEvent update(String apiId, String path) {
        return new ApiRouteChangeEvent(EventType.UPDATE, apiId, path);
    }

    public static ApiRouteChangeEvent update(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = new ApiRouteChangeEvent(EventType.UPDATE, apiId, path);
        event.setMethod(method);
        event.setTenantId(tenantId);
        return event;
    }

    public static ApiRouteChangeEvent offline(String apiId, String path) {
        return new ApiRouteChangeEvent(EventType.OFFLINE, apiId, path);
    }

    public static ApiRouteChangeEvent offline(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = new ApiRouteChangeEvent(EventType.OFFLINE, apiId, path);
        event.setMethod(method);
        event.setTenantId(tenantId);
        return event;
    }

    public static ApiRouteChangeEvent delete(String apiId, String path) {
        return new ApiRouteChangeEvent(EventType.DELETE, apiId, path);
    }

    public static ApiRouteChangeEvent delete(String apiId, String path, String method, String tenantId) {
        ApiRouteChangeEvent event = new ApiRouteChangeEvent(EventType.DELETE, apiId, path);
        event.setMethod(method);
        event.setTenantId(tenantId);
        return event;
    }
}
