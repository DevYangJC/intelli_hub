package com.intellihub.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 创建API请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class CreateApiRequest {

    /**
     * 分组ID
     */
    private String groupId;

    /**
     * API名称
     */
    @NotBlank(message = "API名称不能为空")
    private String name;

    /**
     * API编码
     */
    @NotBlank(message = "API编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9-]*$", message = "编码只能包含小写字母、数字和连字符，且以字母开头")
    private String code;

    /**
     * 版本号
     */
    private String version = "v1";

    /**
     * API描述
     */
    private String description;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    @NotBlank(message = "请求方法不能为空")
    private String method;

    /**
     * 请求路径
     */
    @NotBlank(message = "请求路径不能为空")
    private String path;

    /**
     * 协议：HTTP/HTTPS
     */
    private String protocol = "HTTP";

    /**
     * 内容类型
     */
    private String contentType = "application/json";

    /**
     * 认证方式：none/token/signature
     */
    private String authType = "token";

    /**
     * 超时时间(ms)
     */
    private Integer timeout = 30000;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 是否启用缓存
     */
    private Boolean cacheEnabled = false;

    /**
     * 缓存时间(秒)
     */
    private Integer cacheTtl = 0;

    /**
     * 是否启用限流
     */
    private Boolean rateLimitEnabled = false;

    /**
     * 限流QPS
     */
    private Integer rateLimitQps = 100;

    /**
     * 是否启用Mock
     */
    private Boolean mockEnabled = false;

    /**
     * Mock响应数据
     */
    private String mockResponse;

    /**
     * 成功响应示例
     */
    private String successResponse;

    /**
     * 错误响应示例
     */
    private String errorResponse;

    /**
     * 是否启用IP白名单
     */
    private Boolean ipWhitelistEnabled = false;

    /**
     * IP白名单列表
     */
    private String ipWhitelist;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 请求参数列表
     */
    private List<ApiParamRequest> requestParams;

    /**
     * 后端配置
     */
    private ApiBackendRequest backend;
}
