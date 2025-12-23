package com.intellihub.app.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用内部响应DTO
 * <p>
 * 用于服务间内部调用，包含AppSecret
 * 注意：此接口不应暴露给外部
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppInternalResponse {

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
     * AppKey
     */
    private String appKey;

    /**
     * AppSecret（内部使用）
     */
    private String appSecret;

    /**
     * 应用状态
     */
    private String status;

    /**
     * AppKey过期时间
     */
    private LocalDateTime expireTime;
}
