package com.intellihub.api.dto.response;

import lombok.Data;

/**
 * API后端配置响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiBackendResponse {

    private String id;
    private String apiId;
    private String type;
    private String protocol;
    private String method;
    private String host;
    private String path;
    private Integer timeout;
    private Integer connectTimeout;
    private String registry;
    private String interfaceName;
    private String methodName;
    private String dubboVersion;
    private String dubboGroup;
    private String refApiId;
    private Integer mockDelay;
}
