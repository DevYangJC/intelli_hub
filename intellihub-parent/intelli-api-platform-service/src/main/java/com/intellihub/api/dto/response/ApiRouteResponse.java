package com.intellihub.api.dto.response;

import lombok.Data;

/**
 * API路由响应DTO
 * <p>
 * 供网关动态路由使用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiRouteResponse {

    /**
     * API ID
     */
    private String apiId;

    /**
     * API名称
     */
    private String apiName;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求路径
     */
    private String path;

    /**
     * API状态
     */
    private String status;

    /**
     * 认证方式：none/token/signature
     */
    private String authType;

    /**
     * 后端类型：http/mock/dubbo
     */
    private String backendType;

    /**
     * 后端地址
     */
    private String backendHost;

    /**
     * 后端路径
     */
    private String backendPath;

    /**
     * 后端协议
     */
    private String backendProtocol;

    /**
     * 超时时间
     */
    private Integer timeout;

    /**
     * 是否启用Mock
     */
    private Boolean mockEnabled;

    /**
     * Mock响应
     */
    private String mockResponse;

    /**
     * 是否启用限流
     */
    private Boolean rateLimitEnabled;

    /**
     * 限流QPS
     */
    private Integer rateLimitQps;
}
