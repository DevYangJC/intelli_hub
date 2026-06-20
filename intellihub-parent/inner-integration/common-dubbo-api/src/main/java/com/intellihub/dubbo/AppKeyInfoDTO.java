package com.intellihub.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用Key信息DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppKeyInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String appId;
    private String tenantId;
    private String appName;
    private String appCode;
    private String appKey;
    private String appSecret;
    private String status;
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
