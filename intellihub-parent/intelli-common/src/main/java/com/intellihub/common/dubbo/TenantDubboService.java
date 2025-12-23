package com.intellihub.common.dubbo;

/**
 * 租户Dubbo服务接口
 * <p>
 * 提供租户验证和信息查询功能
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface TenantDubboService {

    /**
     * 验证租户是否有效
     *
     * @param tenantId 租户ID
     * @return 是否有效
     */
    boolean isValidTenant(String tenantId);

    /**
     * 获取租户信息
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
