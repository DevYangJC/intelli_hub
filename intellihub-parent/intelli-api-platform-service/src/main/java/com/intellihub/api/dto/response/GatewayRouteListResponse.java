package com.intellihub.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关路由列表响应DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class GatewayRouteListResponse {

    private String id;

    private String name;

    private String code;

    private String groupName;

    private String method;

    private String path;

    private String status;

    private String authType;

    private String backendType;

    private String backendHost;

    private String backendPath;

    private Integer timeout;

    private Boolean rateLimitEnabled;

    private Integer rateLimitQps;

    private Boolean mockEnabled;

    private Long todayCalls;

    private Long totalCalls;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
