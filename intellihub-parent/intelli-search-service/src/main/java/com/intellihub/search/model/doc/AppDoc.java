package com.intellihub.search.model.doc;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 应用索引文档
 *
 * @author IntelliHub
 */
@Data
@Accessors(chain = true)
public class AppDoc {

    /**
     * 应用ID
     */
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用编码
     */
    private String code;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用类型（internal/external）
     */
    private String appType;

    /**
     * 应用类型名称
     */
    private String appTypeName;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * 应用状态（active/disabled/expired）
     */
    private String status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
