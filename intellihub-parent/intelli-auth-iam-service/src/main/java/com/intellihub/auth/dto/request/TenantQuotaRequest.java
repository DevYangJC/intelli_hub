package com.intellihub.auth.dto.request;

import lombok.Data;

/**
 * 租户配额请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class TenantQuotaRequest {

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大应用数
     */
    private Integer maxApps;

    /**
     * 最大API数
     */
    private Integer maxApis;

    /**
     * 每日最大调用次数
     */
    private Long maxCallsPerDay;

    /**
     * 每月最大调用次数
     */
    private Long maxCallsPerMonth;
}
