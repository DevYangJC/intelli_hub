package com.intellihub.app.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息响应DTO
 * <p>
 * 用于返回应用的详细信息
 * 注意：AppSecret不在列表接口返回，只在创建时返回一次
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppInfoResponse {

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
     * 应用类型：internal-内部应用，external-外部应用
     */
    private String appType;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * 应用状态：active-正常，disabled-禁用，expired-过期
     */
    private String status;

    /**
     * 调用配额限制（每日）
     */
    private Long quotaLimit;

    /**
     * 已使用配额
     */
    private Long quotaUsed;

    /**
     * 配额使用百分比
     */
    private Double quotaUsagePercent;

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * IP白名单
     */
    private String ipWhitelist;

    /**
     * AppKey过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 订阅的API数量
     */
    private Integer subscribedApiCount;

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
