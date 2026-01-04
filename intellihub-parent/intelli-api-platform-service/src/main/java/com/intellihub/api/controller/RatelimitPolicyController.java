package com.intellihub.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.api.constant.ApiConstants;
import com.intellihub.api.constant.ApiResponseMessage;
import com.intellihub.api.dto.request.RatelimitPolicyApplyRequest;
import com.intellihub.api.dto.request.RatelimitPolicyCreateRequest;
import com.intellihub.api.dto.response.RatelimitPolicyResponse;
import com.intellihub.api.service.RatelimitPolicyService;
import com.intellihub.ApiResponse;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 限流策略管理控制器
 * 提供限流策略的增删改查、应用到路由、移除等功能
 * 限流策略用于控制API的访问频率，防止恶意攻击和过载
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/gateway/ratelimit")
@RequiredArgsConstructor
public class RatelimitPolicyController {

    private final RatelimitPolicyService policyService;

    /**
     * 分页查询限流策略列表
     * 支持按关键词和状态筛选
     *
     * @param page 页码，默认第1页
     * @param size 每页大小，默认20条
     * @param keyword 搜索关键词（可选），支持策略名称模糊搜索
     * @param status 策略状态（可选），如：ENABLED、DISABLED
     * @return 分页后的限流策略列表
     */
    @GetMapping("/policies")
    public ApiResponse<Page<RatelimitPolicyResponse>> listPolicies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        String tenantId = UserContextHolder.getCurrentTenantId();
        Page<RatelimitPolicyResponse> result = policyService.listPolicies(tenantId, page, size, keyword, status);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询限流策略详情
     * 返回策略的完整配置信息，包括限流规则、应用的路由等
     *
     * @param id 策略ID
     * @return 限流策略详细信息
     */
    @GetMapping("/policies/{id}")
    public ApiResponse<RatelimitPolicyResponse> getPolicyDetail(@PathVariable String id) {
        RatelimitPolicyResponse response = policyService.getPolicyById(id);
        return ApiResponse.success(response);
    }

    /**
     * 创建限流策略
     * 创建新的限流策略配置，包括限流类型、阈值、时间窗口等
     *
     * @param request 限流策略创建请求，包含策略名称、类型、限流规则等
     * @return 创建成功后返回策略ID
     */
    @PostMapping("/policies")
    public ApiResponse<String> createPolicy(@Valid @RequestBody RatelimitPolicyCreateRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String policyId = policyService.createPolicy(tenantId, request);
        return ApiResponse.success(policyId, ApiResponseMessage.POLICY_CREATE_SUCCESS);
    }

    /**
     * 更新限流策略
     * 修改已存在的限流策略配置，更新后立即生效
     *
     * @param id 策略ID
     * @param request 限流策略更新请求，包含需要修改的配置项
     * @return 操作结果
     */
    @PutMapping("/policies/{id}")
    public ApiResponse<String> updatePolicy(
            @PathVariable String id,
            @Valid @RequestBody RatelimitPolicyCreateRequest request) {
        
        policyService.updatePolicy(id, request);
        return ApiResponse.success(ApiResponseMessage.POLICY_UPDATE_SUCCESS);
    }

    /**
     * 删除限流策略
     * 删除策略前会检查是否有路由正在使用，如有则不允许删除
     *
     * @param id 策略ID
     * @return 操作结果
     */
    @DeleteMapping("/policies/{id}")
    public ApiResponse<String> deletePolicy(@PathVariable String id) {
        policyService.deletePolicy(id);
        return ApiResponse.success(ApiResponseMessage.POLICY_DELETE_SUCCESS);
    }

    /**
     * 应用限流策略到路由
     * 将限流策略绑定到一个或多个API路由，绑定后路由将受该策略限制
     *
     * @param policyId 策略ID
     * @param request 应用请求，包含需要绑定的路由ID列表
     * @return 操作结果
     */
    @PostMapping("/policies/{policyId}/apply")
    public ApiResponse<String> applyPolicyToRoutes(
            @PathVariable String policyId,
            @Valid @RequestBody RatelimitPolicyApplyRequest request) {
        
        policyService.applyPolicyToRoutes(policyId, request);
        return ApiResponse.success(ApiResponseMessage.POLICY_APPLY_SUCCESS);
    }

    /**
     * 移除路由的限流策略
     * 解除限流策略与路由的绑定关系，移除后路由不再受该策略限制
     *
     * @param policyId 策略ID
     * @param routeId 路由ID
     * @return 操作结果
     */
    @DeleteMapping("/policies/{policyId}/routes/{routeId}")
    public ApiResponse<String> removePolicyFromRoute(
            @PathVariable String policyId,
            @PathVariable String routeId) {
        
        policyService.removePolicyFromRoute(policyId, routeId);
        return ApiResponse.success(ApiResponseMessage.POLICY_REMOVE_SUCCESS);
    }
}
