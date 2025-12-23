package com.intellihub.app.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用订阅API响应DTO
 * <p>
 * 用于返回应用订阅的API信息
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppSubscriptionResponse {

    /**
     * 订阅ID
     */
    private String id;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * API ID
     */
    private String apiId;

    /**
     * API名称
     */
    private String apiName;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 订阅状态：active-生效，disabled-禁用，expired-过期
     */
    private String status;

    /**
     * 单独的调用限额
     */
    private Long quotaLimit;

    /**
     * 订阅生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 订阅过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
