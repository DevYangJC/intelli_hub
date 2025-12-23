package com.intellihub.api.dto.request;

import lombok.Data;

/**
 * API后端配置请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiBackendRequest {

    /**
     * 后端类型：http/mock/function
     */
    private String type = "http";

    /**
     * 协议：HTTP/HTTPS
     */
    private String protocol = "HTTP";

    /**
     * 请求方法
     */
    private String method;

    /**
     * 后端地址
     */
    private String host;

    /**
     * 后端路径
     */
    private String path;

    /**
     * 超时时间(ms)
     */
    private Integer timeout = 30000;

    /**
     * 连接超时(ms)
     */
    private Integer connectTimeout = 5000;

    /**
     * Dubbo注册中心地址
     */
    private String registry;

    /**
     * Dubbo接口名称
     */
    private String interfaceName;

    /**
     * Dubbo方法名称
     */
    private String methodName;

    /**
     * Dubbo服务版本
     */
    private String dubboVersion;

    /**
     * Dubbo服务分组
     */
    private String dubboGroup;

    /**
     * 引用的内部API ID
     */
    private String refApiId;

    /**
     * Mock延迟时间(ms)
     */
    private Integer mockDelay = 0;

    /**
     * Mock响应数据
     */
    private String mockResponse;
}
