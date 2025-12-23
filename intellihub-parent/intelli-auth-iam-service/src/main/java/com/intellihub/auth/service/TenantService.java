package com.intellihub.auth.service;

import com.intellihub.auth.dto.request.CreateTenantRequest;
import com.intellihub.auth.dto.request.TenantQuotaRequest;
import com.intellihub.auth.dto.request.TenantQueryRequest;
import com.intellihub.auth.dto.request.UpdateTenantRequest;
import com.intellihub.auth.dto.response.TenantResponse;
import com.intellihub.page.PageData;

/**
 * 租户服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface TenantService {

    /**
     * 分页查询租户
     */
    PageData<TenantResponse> listTenants(TenantQueryRequest request);

    /**
     * 创建租户
     */
    TenantResponse createTenant(CreateTenantRequest request);

    /**
     * 获取租户详情
     */
    TenantResponse getTenantById(String id);

    /**
     * 更新租户
     */
    TenantResponse updateTenant(String id, UpdateTenantRequest request);

    /**
     * 删除租户
     */
    void deleteTenant(String id);

    /**
     * 启用租户
     */
    void enableTenant(String id);

    /**
     * 禁用租户
     */
    void disableTenant(String id);

    /**
     * 获取租户配额
     */
    TenantResponse.TenantQuotaInfo getTenantQuota(String id);

    /**
     * 更新租户配额
     */
    void updateTenantQuota(String id, TenantQuotaRequest request);

    /**
     * 获取当前租户信息
     */
    TenantResponse getCurrentTenant(String tenantId);
}
