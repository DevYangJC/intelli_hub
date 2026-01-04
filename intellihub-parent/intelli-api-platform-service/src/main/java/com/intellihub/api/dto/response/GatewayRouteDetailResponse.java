package com.intellihub.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关路由详情响应DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class GatewayRouteDetailResponse {

    private String id;

    private String tenantId;

    private String groupId;

    private String groupName;

    private String name;

    private String code;

    private String version;

    private String description;

    private String method;

    private String path;

    private String protocol;

    private String contentType;

    private String status;

    private String authType;

    private Integer timeout;

    private Integer retryCount;

    private Boolean cacheEnabled;

    private Integer cacheTtl;

    private Boolean rateLimitEnabled;

    private Integer rateLimitQps;

    private Boolean mockEnabled;

    private String mockResponse;

    private String successResponse;

    private String errorResponse;

    private Boolean ipWhitelistEnabled;

    private String ipWhitelist;

    private Long todayCalls;

    private Long totalCalls;

    private BackendDetail backend;

    private String createdBy;

    private String creatorName;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    public static class BackendDetail {

        private String type;

        private String protocol;

        private String method;

        private String host;

        private String path;

        private Integer timeout;

        private Integer connectTimeout;

        private String interfaceName;

        private String methodName;

        private String dubboVersion;

        private String dubboGroup;

        private String refApiId;

        private Integer mockDelay;
    }
}
