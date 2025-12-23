package com.intellihub.common.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * API路由配置DTO（Dubbo传输对象）
 * <p>
 * 用于网关动态路由转发
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiRouteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * API ID
     */
    private String apiId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * API名称
     */
    private String apiName;

    /**
     * 请求路径（如 /open/user/info）
     */
    private String path;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    private String method;

    /**
     * 后端类型：http/dubbo/mock
     */
    private String backendType;

    /**
     * 后端协议：HTTP/HTTPS
     */
    private String backendProtocol;

    /**
     * 后端主机地址（如 user-service 或 192.168.1.100:8080）
     */
    private String backendHost;

    /**
     * 后端路径（如 /api/user/info）
     */
    private String backendPath;

    /**
     * 后端请求方法
     */
    private String backendMethod;

    /**
     * 超时时间(ms)
     */
    private Integer timeout;

    /**
     * 是否启用Mock
     */
    private Boolean mockEnabled;

    /**
     * Mock响应数据
     */
    private String mockResponse;

    /**
     * 认证方式：none/signature
     */
    private String authType;

    /**
     * 是否启用限流
     */
    private Boolean rateLimitEnabled;

    /**
     * 限流QPS
     */
    private Integer rateLimitQps;

    /**
     * Dubbo接口名称（后端类型为dubbo时使用）
     */
    private String dubboInterface;

    /**
     * Dubbo方法名称
     */
    private String dubboMethod;

    /**
     * Dubbo服务版本
     */
    private String dubboVersion;

    /**
     * Dubbo服务分组
     */
    private String dubboGroup;
}
