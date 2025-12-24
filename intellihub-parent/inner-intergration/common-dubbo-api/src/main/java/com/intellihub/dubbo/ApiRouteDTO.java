package com.intellihub.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * API路由DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiRouteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apiId;
    private String tenantId;
    private String apiName;
    private String path;
    private String method;
    private String authType;
    private Integer timeout;
    private Boolean mockEnabled;
    private String mockResponse;
    private Boolean rateLimitEnabled;
    private Integer rateLimitQps;

    // 后端配置
    private String backendType;
    private String backendProtocol;
    private String backendHost;
    private String backendPath;
    private String backendMethod;

    // Dubbo配置
    private String dubboInterface;
    private String dubboMethod;
    private String dubboVersion;
    private String dubboGroup;
}
