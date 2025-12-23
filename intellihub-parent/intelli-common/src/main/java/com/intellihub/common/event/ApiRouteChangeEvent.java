package com.intellihub.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * API路由变更事件
 * <p>
 * 当API发布、下线、更新时触发，通知网关刷新路由配置
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRouteChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Redis发布订阅频道名称
     */
    public static final String CHANNEL = "api:route:change";

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
     * 请求方法
     */
    private String method;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 事件时间戳
     */
    private Long timestamp;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * API发布
         */
        PUBLISH,
        
        /**
         * API下线
         */
        OFFLINE,
        
        /**
         * API更新
         */
        UPDATE,
        
        /**
         * API删除
         */
        DELETE,
        
        /**
         * 全量刷新
         */
        REFRESH_ALL
    }

    /**
     * 创建发布事件
     */
    public static ApiRouteChangeEvent publish(String apiId, String path, String method, String tenantId) {
        return ApiRouteChangeEvent.builder()
                .eventType(EventType.PUBLISH)
                .apiId(apiId)
                .path(path)
                .method(method)
                .tenantId(tenantId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建下线事件
     */
    public static ApiRouteChangeEvent offline(String apiId, String path, String method, String tenantId) {
        return ApiRouteChangeEvent.builder()
                .eventType(EventType.OFFLINE)
                .apiId(apiId)
                .path(path)
                .method(method)
                .tenantId(tenantId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建更新事件
     */
    public static ApiRouteChangeEvent update(String apiId, String path, String method, String tenantId) {
        return ApiRouteChangeEvent.builder()
                .eventType(EventType.UPDATE)
                .apiId(apiId)
                .path(path)
                .method(method)
                .tenantId(tenantId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建全量刷新事件
     */
    public static ApiRouteChangeEvent refreshAll() {
        return ApiRouteChangeEvent.builder()
                .eventType(EventType.REFRESH_ALL)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
