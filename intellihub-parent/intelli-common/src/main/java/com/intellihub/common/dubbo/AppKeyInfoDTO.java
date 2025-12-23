package com.intellihub.common.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * AppKey信息DTO（Dubbo传输对象）
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppKeyInfoDTO implements Serializable {

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
     * 应用编码
     */
    private String appCode;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * AppSecret
     */
    private String appSecret;

    /**
     * 应用状态：active/disabled/expired
     */
    private String status;

    /**
     * 过期时间（毫秒时间戳）
     */
    private Long expireTime;
}
