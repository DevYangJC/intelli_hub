package com.intellihub.common.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * 租户信息DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class TenantInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租户状态：active/inactive/suspended
     */
    private String status;

    /**
     * 租户类型
     */
    private String type;

    /**
     * 过期时间（毫秒时间戳）
     */
    private Long expireTime;
}
