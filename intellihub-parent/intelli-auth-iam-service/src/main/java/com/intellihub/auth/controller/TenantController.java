package com.intellihub.auth.controller;

import com.intellihub.auth.dto.request.CreateTenantRequest;
import com.intellihub.auth.dto.request.TenantQuotaRequest;
import com.intellihub.auth.dto.request.TenantQueryRequest;
import com.intellihub.auth.dto.request.UpdateTenantRequest;
import com.intellihub.auth.dto.response.TenantResponse;
import com.intellihub.auth.service.TenantService;
import com.intellihub.common.ApiResponse;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 租户管理控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/iam/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 获取租户列表
     */
    @GetMapping
    public ApiResponse<PageData<TenantResponse>> listTenants(TenantQueryRequest request) {
        PageData<TenantResponse> pageData = tenantService.listTenants(request);
        return ApiResponse.success(pageData);
    }

    /**
     * 创建租户
     */
    @PostMapping
    public ApiResponse<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse tenant = tenantService.createTenant(request);
        return ApiResponse.success("创建成功", tenant);
    }

    /**
     * 获取租户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TenantResponse> getTenantById(@PathVariable String id) {
        TenantResponse tenant = tenantService.getTenantById(id);
        return ApiResponse.success(tenant);
    }

    /**
     * 更新租户
     */
    @PutMapping("/{id}")
    public ApiResponse<TenantResponse> updateTenant(
            @PathVariable String id,
            @Valid @RequestBody UpdateTenantRequest request) {
        TenantResponse tenant = tenantService.updateTenant(id, request);
        return ApiResponse.success("更新成功", tenant);
    }

    /**
     * 删除租户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTenant(@PathVariable String id) {
        tenantService.deleteTenant(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 启用租户
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enableTenant(@PathVariable String id) {
        tenantService.enableTenant(id);
        return ApiResponse.success("启用成功", null);
    }

    /**
     * 禁用租户
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disableTenant(@PathVariable String id) {
        tenantService.disableTenant(id);
        return ApiResponse.success("禁用成功", null);
    }

    /**
     * 获取租户配额
     */
    @GetMapping("/{id}/quota")
    public ApiResponse<TenantResponse.TenantQuotaInfo> getTenantQuota(@PathVariable String id) {
        TenantResponse.TenantQuotaInfo quota = tenantService.getTenantQuota(id);
        return ApiResponse.success(quota);
    }

    /**
     * 更新租户配额
     */
    @PutMapping("/{id}/quota")
    public ApiResponse<Void> updateTenantQuota(
            @PathVariable String id,
            @Valid @RequestBody TenantQuotaRequest request) {
        tenantService.updateTenantQuota(id, request);
        return ApiResponse.success("配额更新成功", null);
    }

    /**
     * 获取当前租户信息
     */
    @GetMapping("/current")
    public ApiResponse<TenantResponse> getCurrentTenant(@RequestHeader("X-Tenant-Id") String tenantId) {
        TenantResponse tenant = tenantService.getCurrentTenant(tenantId);
        return ApiResponse.success(tenant);
    }
}
