package com.intellihub.api.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 网关路由创建请求DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class GatewayRouteCreateRequest {

    @NotBlank(message = "API名称不能为空")
    private String name;

    @NotBlank(message = "API编码不能为空")
    private String code;

    private String groupId;

    private String version = "v1";

    private String description;

    @NotBlank(message = "请求方法不能为空")
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH", message = "请求方法必须为GET/POST/PUT/DELETE/PATCH之一")
    private String method;

    @NotBlank(message = "请求路径不能为空")
    @Pattern(regexp = "^/.*", message = "请求路径必须以/开头")
    private String path;

    private String protocol = "HTTP";

    private String contentType = "application/json";

    @NotBlank(message = "认证方式不能为空")
    @Pattern(regexp = "none|token|signature", message = "认证方式必须为none/token/signature之一")
    private String authType = "token";

    private Integer timeout = 30000;

    private Integer retryCount = 0;

    private Boolean cacheEnabled = false;

    private Integer cacheTtl = 0;

    private Boolean rateLimitEnabled = false;

    private Integer rateLimitQps = 100;

    private Boolean mockEnabled = false;

    private String mockResponse;

    private String successResponse;

    private String errorResponse;

    private Boolean ipWhitelistEnabled = false;

    private String ipWhitelist;

    @NotNull(message = "后端配置不能为空")
    @Valid
    private BackendConfig backend;

    @Data
    public static class BackendConfig {

        @NotBlank(message = "后端类型不能为空")
        @Pattern(regexp = "http|mock|dubbo", message = "后端类型必须为http/mock/dubbo之一")
        private String type;

        private String protocol = "HTTP";

        private String method;

        private String host;

        private String path;

        private Integer timeout = 30000;

        private Integer connectTimeout = 5000;

        private String interfaceName;

        private String methodName;

        private String dubboVersion;

        private String dubboGroup;

        private String refApiId;

        private Integer mockDelay = 0;
    }
}
