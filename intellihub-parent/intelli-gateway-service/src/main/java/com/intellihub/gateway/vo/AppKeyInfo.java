package com.intellihub.gateway.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * AppKey信息
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppKeyInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * AppSecret
     */
    private String appSecret;

    /**
     * 应用状态
     */
    private String status;

    /**
     * 过期时间（毫秒时间戳）
     */
    private Long expireTime;

    /**
     * IP白名单，多个用逗号分隔
     */
    private String ipWhitelist;

    /**
     * 每日调用配额限制
     */
    private Long quotaLimit;

    /**
     * 已使用配额
     */
    private Long quotaUsed;

    /**
     * 配额重置时间（毫秒时间戳）
     */
    private Long quotaResetTime;
}
