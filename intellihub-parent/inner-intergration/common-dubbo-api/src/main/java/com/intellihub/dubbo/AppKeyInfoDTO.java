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
}
