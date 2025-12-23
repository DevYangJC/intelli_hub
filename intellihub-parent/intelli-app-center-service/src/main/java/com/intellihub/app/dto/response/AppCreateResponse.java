package com.intellihub.app.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用创建响应DTO
 * <p>
 * 用于返回创建应用后的信息，包含AppSecret
 * 注意：AppSecret只在创建时返回一次，请妥善保管
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AppCreateResponse {

    /**
     * 应用ID
     */
    private String id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用编码
     */
    private String code;

    /**
     * AppKey，用于API调用认证
     */
    private String appKey;

    /**
     * AppSecret，用于签名验证
     * 注意：此字段只在创建时返回，请妥善保管
     */
    private String appSecret;

    /**
     * 应用状态
     */
    private String status;

    /**
     * 调用配额限制（每日）
     */
    private Long quotaLimit;

    /**
     * AppKey过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
