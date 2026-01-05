package com.intellihub.api.controller;

import com.intellihub.api.constant.ApiResponseMessage;
import com.intellihub.api.dto.response.ApiRouteResponse;
import com.intellihub.context.UserContextHolder;
import com.intellihub.api.dto.request.ApiQueryRequest;
import com.intellihub.api.dto.request.CreateApiRequest;
import com.intellihub.api.dto.request.UpdateApiRequest;
import com.intellihub.api.dto.response.ApiInfoResponse;
import com.intellihub.api.service.ApiInfoService;
import com.intellihub.ApiResponse;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * API管理控制器
 * 提供API的全生命周期管理功能，包括创建、编辑、发布、下线、废弃、复制等
 * 支持API的版本管理和路由配置
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
     * 分页查询API列表
     * 支持按分组、状态、关键词等条件筛选
     *
     * @param request 查询请求，包含分页参数、筛选条件等
     * @return 分页后的API列表
     */
    @GetMapping("/list")
    public ApiResponse<PageData<ApiInfoResponse>> listApis(ApiQueryRequest request) {
        // 租户ID由多租户拦截器自动处理
        PageData<ApiInfoResponse> pageData = apiInfoService.listApis(request);
        return ApiResponse.success(pageData);
    }

    /**
     * 根据ID获取API详情
     * 返回API的完整信息，包括基本信息、后端配置、参数定义等
     *
     * @param id API的唯一标识ID
     * @return API详细信息
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ApiInfoResponse> getApiById(@PathVariable String id) {
        ApiInfoResponse api = apiInfoService.getApiById(id);
        return ApiResponse.success(api);
    }

    /**
     * 创建新API
     * 创建API草稿，需要填写API的基本信息（名称、路径、方法等）
     * 创建后API处于草稿状态，需要配置完整后才能发布
     *
     * @param request 创建请求，包含API名称、路径、请求方法、分组等信息
     * @return 创建成功后返回API详细信息
     */
    @PostMapping("/create")
    public ApiResponse<ApiInfoResponse> createApi(@Valid @RequestBody CreateApiRequest request) {
        // 租户ID由多租户拦截器自动处理
        String userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        ApiInfoResponse api = apiInfoService.createApi(userId, username, request);
        return ApiResponse.success(ApiResponseMessage.CREATE_SUCCESS, api);
    }

    /**
     * 更新API信息
     * 只能更新草稿或已下线状态的API，已发布的API需要先下线
     * 更新内容包括API名称、描述、路径、请求方法等基本信息
     *
     * @param id API的唯一标识ID
     * @param request 更新请求，包含需要修改的API信息
     * @return 更新成功后返回API详细信息
     */
    @PostMapping("/{id}/update")
    public ApiResponse<ApiInfoResponse> updateApi(
            @PathVariable String id,
            @Valid @RequestBody UpdateApiRequest request) {
        ApiInfoResponse api = apiInfoService.updateApi(id, request);
        return ApiResponse.success(ApiResponseMessage.UPDATE_SUCCESS, api);
    }

    /**
     * 删除API
     * 只能删除草稿状态的API，已发布或已下线的API需要先废弃
     * 删除操作不可恢复，请谨慎操作
     *
     * @param id API的唯一标识ID
     * @return 操作结果
     */
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> deleteApi(@PathVariable String id) {
        apiInfoService.deleteApi(id);
        return ApiResponse.success(ApiResponseMessage.DELETE_SUCCESS, null);
    }

    /**
     * 发布API
     * 将草稿状态的API发布到网关，发布后API可供外部调用
     * 发布前会校验API配置的完整性（后端配置、参数定义等）
     *
     * @param id API的唯一标识ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishApi(@PathVariable String id) {
        apiInfoService.publishApi(id);
        return ApiResponse.success(ApiResponseMessage.PUBLISH_SUCCESS, null);
    }

    /**
     * 下线API
     * 将已发布的API从网关下线，下线后API不可调用
     * 下线后可以重新编辑API配置，然后再次发布
     *
     * @param id API的唯一标识ID
     * @return 操作结果
     */
    @PostMapping("/{id}/offline")
    public ApiResponse<Void> offlineApi(@PathVariable String id) {
        apiInfoService.offlineApi(id);
        return ApiResponse.success(ApiResponseMessage.OFFLINE_SUCCESS, null);
    }

    /**
     * 废弃API
     * 将API标记为废弃状态，废弃后API仍可调用但会返回废弃警告
     * 通常用于API版本迁移，建议调用方切换到新版本API
     *
     * @param id API的唯一标识ID
     * @return 操作结果
     */
    @PostMapping("/{id}/deprecate")
    public ApiResponse<Void> deprecateApi(@PathVariable String id) {
        apiInfoService.deprecateApi(id);
        return ApiResponse.success(ApiResponseMessage.DEPRECATE_SUCCESS, null);
    }

    /**
     * 复制API
     * 基于现有API创建一个副本，包括API的所有配置信息
     * 复制后的API处于草稿状态，可以进行修改后发布
     *
     * @param id 源API的唯一标识ID
     * @return 复制成功后返回新API的详细信息
     */
    @PostMapping("/{id}/copy")
    public ApiResponse<ApiInfoResponse> copyApi(@PathVariable String id) {
        // 租户ID由多租户拦截器自动处理
        String userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        ApiInfoResponse api = apiInfoService.copyApi(id, userId, username);
        return ApiResponse.success(ApiResponseMessage.COPY_SUCCESS, api);
    }

    /**
     * 获取所有已发布API的路由配置
     * 内部接口，供网关服务调用，用于动态加载路由配置
     * 只返回已发布状态的API路由信息
     *
     * @return 所有已发布API的路由配置列表
     */
    @GetMapping("/routes")
    public ApiResponse<java.util.List<ApiRouteResponse>> getApiRoutes() {
        java.util.List<ApiRouteResponse> routes = apiInfoService.getPublishedApiRoutes();
        return ApiResponse.success(routes);
    }
}
