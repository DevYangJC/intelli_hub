package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API信息实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_info")
public class ApiInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 分组ID
     */
    private String groupId;

    /**
     * API名称
     */
    private String name;

    /**
     * API编码
     */
    private String code;

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
     * 状态：draft/published/offline/deprecated
     */
    private String status;

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
     * 今日调用次数
     */
    private Long todayCalls;

    /**
     * 总调用次数
     */
    private Long totalCalls;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;
}
