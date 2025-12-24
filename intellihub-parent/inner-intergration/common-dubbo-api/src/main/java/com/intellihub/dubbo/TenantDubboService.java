package com.intellihub.dubbo;


/**
 * 租户Dubbo服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface TenantDubboService {

    /**
     * 检查租户是否有效（String类型，网关使用）
     *
     * @param tenantId 租户ID字符串
     * @return 是否有效
     */
    boolean isValidTenant(String tenantId);

    /**
     * 根据租户ID获取租户信息
     *
     * @param tenantId 租户ID
     * @return 租户信息
     */
    TenantInfoDTO getTenantInfo(String tenantId);

    /**
     * 根据租户编码获取租户信息
     *
     * @param tenantCode 租户编码
     * @return 租户信息
     */
    TenantInfoDTO getTenantByCode(String tenantCode);
}
