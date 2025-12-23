package com.intellihub.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API信息响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiInfoResponse {

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
    private String createdBy;
    private String creatorName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 请求参数列表
     */
    private List<ApiParamResponse> requestParams;

    /**
     * 后端配置
     */
    private ApiBackendResponse backend;

    /**
     * 标签列表
     */
    private List<String> tags;
}
