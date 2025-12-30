package com.intellihub.search.model.doc;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * API 索引文档
 *
 * @author IntelliHub
 */
@Data
@Accessors(chain = true)
public class ApiDoc {

    /**
     * API ID
     */
    private String id;

    /**
     * API 名称
     */
    private String name;

    /**
     * API 编码
     */
    private String code;

    /**
     * API 路径
     */
    private String path;

    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    private String method;

    /**
     * 协议（HTTP/HTTPS）
     */
    private String protocol;

    /**
     * API 描述
     */
    private String description;

    /**
     * 分组ID
     */
    private String groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 状态（draft/published/offline/deprecated）
     */
    private String status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 认证方式（none/token/signature）
     */
    private String authType;

    /**
     * 版本号
     */
    private String version;

    /**
     * 租户ID
     */
    private String tenantId;

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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
