package com.intellihub.api.controller;

import com.intellihub.api.constant.ApiResponseMessage;
import com.intellihub.api.dto.request.ApiBackendRequest;
import com.intellihub.api.dto.response.ApiBackendResponse;
import com.intellihub.api.service.ApiBackendService;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * API后端配置管理控制器
 * 提供API后端服务的配置管理功能，包括后端地址、超时时间、重试策略等
 * 支持多种后端类型：HTTP、Dubbo、gRPC等
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/apis/{apiId}/backend")
@RequiredArgsConstructor
public class ApiBackendController {

    private final ApiBackendService apiBackendService;

    /**
     * 获取API的后端配置详情
     * 返回API关联的后端服务配置信息，包括服务地址、协议类型、超时设置等
     *
     * @param apiId API的唯一标识ID
     * @return API后端配置详细信息
     */
    @GetMapping("/detail")
    public ApiResponse<ApiBackendResponse> getBackendDetail(@PathVariable String apiId) {
        ApiBackendResponse backend = apiBackendService.getBackendByApiId(apiId);
        return ApiResponse.success(backend);
    }

    /**
     * 保存或更新API后端配置
     * 如果后端配置不存在则创建，存在则更新
     * 配置内容包括后端服务地址、协议类型、超时时间、重试策略等
     *
     * @param apiId API的唯一标识ID
     * @param request 后端配置请求，包含服务地址、协议、超时等配置项
     * @return 保存成功后返回后端配置详细信息
     */
    @PostMapping("/save")
    public ApiResponse<ApiBackendResponse> saveBackend(
            @PathVariable String apiId,
            @Valid @RequestBody ApiBackendRequest request) {
        ApiBackendResponse backend = apiBackendService.saveBackend(apiId, request);
        return ApiResponse.success(ApiResponseMessage.SAVE_SUCCESS, backend);
    }

    /**
     * 删除API后端配置
     * 删除后API将无法调用后端服务，需要重新配置后端才能发布
     *
     * @param apiId API的唯一标识ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    public ApiResponse<Void> deleteBackend(@PathVariable String apiId) {
        apiBackendService.deleteBackend(apiId);
        return ApiResponse.success(ApiResponseMessage.DELETE_SUCCESS, null);
    }

    /**
     * 测试后端服务连接
     * 在保存配置前测试后端服务是否可达，验证配置的正确性
     * 支持HTTP、Dubbo等多种协议的连通性测试
     *
     * @param apiId API的唯一标识ID
     * @param request 后端配置请求，包含需要测试的服务地址和协议信息
     * @return 测试结果，true-连接成功，false-连接失败
     */
    @PostMapping("/test-connection")
    public ApiResponse<Boolean> testConnection(
            @PathVariable String apiId,
            @Valid @RequestBody ApiBackendRequest request) {
        boolean result = apiBackendService.testConnection(request);
        if (result) {
            return ApiResponse.success(ApiResponseMessage.CONNECTION_SUCCESS, true);
        } else {
            return ApiResponse.success(ApiResponseMessage.CONNECTION_FAILED, false);
        }
    }
}
