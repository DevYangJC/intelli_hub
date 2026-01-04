package com.intellihub.api.dto.request;

import lombok.Data;

/**
 * 网关路由查询请求DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class GatewayRouteQueryRequest {

    private String keyword;

    private String status;

    private String method;

    private String authType;

    private String backendType;

    private String groupId;

    private Integer page = 1;

    private Integer size = 20;
}
