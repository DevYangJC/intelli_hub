package com.intellihub.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.api.dto.request.RatelimitPolicyApplyRequest;
import com.intellihub.api.dto.request.RatelimitPolicyCreateRequest;
import com.intellihub.api.dto.response.RatelimitPolicyResponse;

/**
 * 限流策略服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface RatelimitPolicyService {

    /**
     * 分页查询限流策略列表
     */
    Page<RatelimitPolicyResponse> listPolicies(String tenantId, int page, int size, String keyword, String status);

    /**
     * 查询限流策略详情
     */
    RatelimitPolicyResponse getPolicyById(String id);

    /**
     * 创建限流策略
     */
    String createPolicy(String tenantId, RatelimitPolicyCreateRequest request);

    /**
     * 更新限流策略
     */
    void updatePolicy(String id, RatelimitPolicyCreateRequest request);

    /**
     * 删除限流策略
     */
    void deletePolicy(String id);

    /**
     * 应用限流策略到路由
     */
    void applyPolicyToRoutes(String policyId, RatelimitPolicyApplyRequest request);

    /**
     * 移除路由的限流策略
     */
    void removePolicyFromRoute(String policyId, String routeId);
}
