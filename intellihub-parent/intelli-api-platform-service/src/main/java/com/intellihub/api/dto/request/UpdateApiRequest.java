package com.intellihub.api.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 更新API请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UpdateApiRequest {

    /**
     * 分组ID
     */
    private String groupId;

    /**
     * API名称
     */
    private String name;

    /**
     * 版本号
     */
    private String version;

    /**
     * API描述
     */
    private String description;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    private String method;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 协议：HTTP/HTTPS
     */
    private String protocol;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 认证方式：none/token/signature
     */
    private String authType;

    /**
     * 超时时间(ms)
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否启用缓存
     */
    private Boolean cacheEnabled;

    /**
     * 缓存时间(秒)
     */
    private Integer cacheTtl;

    /**
     * 是否启用限流
     */
    private Boolean rateLimitEnabled;

    /**
     * 限流QPS
     */
    private Integer rateLimitQps;

    /**
     * 是否启用Mock
     */
    private Boolean mockEnabled;

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
    private Boolean ipWhitelistEnabled;

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
     * 响应参数列表
     */
    private List<ApiParamRequest> responseParams;

    /**
     * 后端配置
     */
    private ApiBackendRequest backend;
}
