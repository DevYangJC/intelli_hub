package com.intellihub.dubbo;

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

    private Long id;
    private String tenantId;
    private String tenantCode;
    private String tenantName;
    private String name;
    private String code;
    private String status;
    private Integer maxUsers;
    private Integer maxApps;
    private Long expireTime;
}
