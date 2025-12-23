package com.intellihub.api.controller;

import com.intellihub.api.dto.response.ApiRouteResponse;
import com.intellihub.common.context.UserContextHolder;
import com.intellihub.api.dto.request.ApiQueryRequest;
import com.intellihub.api.dto.request.CreateApiRequest;
import com.intellihub.api.dto.request.UpdateApiRequest;
import com.intellihub.api.dto.response.ApiInfoResponse;
import com.intellihub.api.service.ApiInfoService;
import com.intellihub.common.ApiResponse;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * API管理控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/apis")
@RequiredArgsConstructor
public class ApiInfoController {

    private final ApiInfoService apiInfoService;

    /**
     * 获取API列表
     */
    @GetMapping("/list")
    public ApiResponse<PageData<ApiInfoResponse>> listApis(ApiQueryRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        PageData<ApiInfoResponse> pageData = apiInfoService.listApis(tenantId, request);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取API详情
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ApiInfoResponse> getApiById(@PathVariable String id) {
        ApiInfoResponse api = apiInfoService.getApiById(id);
        return ApiResponse.success(api);
    }

    /**
     * 创建API
     */
    @PostMapping("/create")
    public ApiResponse<ApiInfoResponse> createApi(@Valid @RequestBody CreateApiRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        ApiInfoResponse api = apiInfoService.createApi(tenantId, userId, username, request);
        return ApiResponse.success("创建成功", api);
    }

    /**
     * 更新API
     */
    @PostMapping("/{id}/update")
    public ApiResponse<ApiInfoResponse> updateApi(
            @PathVariable String id,
            @Valid @RequestBody UpdateApiRequest request) {
        ApiInfoResponse api = apiInfoService.updateApi(id, request);
        return ApiResponse.success("更新成功", api);
    }

    /**
     * 删除API
     */
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> deleteApi(@PathVariable String id) {
        apiInfoService.deleteApi(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 发布API
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishApi(@PathVariable String id) {
        apiInfoService.publishApi(id);
        return ApiResponse.success("发布成功", null);
    }

    /**
     * 下线API
     */
    @PostMapping("/{id}/offline")
    public ApiResponse<Void> offlineApi(@PathVariable String id) {
        apiInfoService.offlineApi(id);
        return ApiResponse.success("下线成功", null);
    }

    /**
     * 废弃API
     */
    @PostMapping("/{id}/deprecate")
    public ApiResponse<Void> deprecateApi(@PathVariable String id) {
        apiInfoService.deprecateApi(id);
        return ApiResponse.success("废弃成功", null);
    }

    /**
     * 复制API
     */
    @PostMapping("/{id}/copy")
    public ApiResponse<ApiInfoResponse> copyApi(@PathVariable String id) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        ApiInfoResponse api = apiInfoService.copyApi(id, tenantId, userId, username);
        return ApiResponse.success("复制成功", api);
    }

    /**
     * 获取所有已发布API的路由配置（内部接口，供网关调用）
     */
    @GetMapping("/routes")
    public ApiResponse<java.util.List<ApiRouteResponse>> getApiRoutes() {
        java.util.List<ApiRouteResponse> routes = apiInfoService.getPublishedApiRoutes();
        return ApiResponse.success(routes);
    }
}
