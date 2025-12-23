package com.intellihub.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    /**
     * 租户ID
     */
    private String id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 租户编码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * Logo
     */
    private String logo;

    /**
     * 状态
     */
    private String status;

    /**
     * 管理员用户信息
     */
    private AdminUserInfo adminUser;

    /**
     * 配额信息
     */
    private TenantQuotaInfo quota;

    /**
     * 使用量信息
     */
    private TenantUsageInfo usage;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminUserInfo {
        private String id;
        private String username;
        private String nickname;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantQuotaInfo {
        private Integer maxUsers;
        private Integer maxApps;
        private Integer maxApis;
        private Long maxCallsPerDay;
        private Long maxCallsPerMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantUsageInfo {
        private Integer userCount;
        private Integer appCount;
        private Integer apiCount;
        private Long todayCalls;
    }
}
