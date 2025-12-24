package com.intellihub.api.controller;

import com.intellihub.api.dto.request.ApiBackendRequest;
import com.intellihub.api.dto.response.ApiBackendResponse;
import com.intellihub.api.service.ApiBackendService;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * API后端配置管理控制器
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
     * 获取API的后端配置
     */
    @GetMapping("/detail")
    public ApiResponse<ApiBackendResponse> getBackendDetail(@PathVariable String apiId) {
        ApiBackendResponse backend = apiBackendService.getBackendByApiId(apiId);
        return ApiResponse.success(backend);
    }

    /**
     * 保存或更新后端配置
     */
    @PostMapping("/save")
    public ApiResponse<ApiBackendResponse> saveBackend(
            @PathVariable String apiId,
            @Valid @RequestBody ApiBackendRequest request) {
        ApiBackendResponse backend = apiBackendService.saveBackend(apiId, request);
        return ApiResponse.success("保存成功", backend);
    }

    /**
     * 删除后端配置
     */
    @PostMapping("/delete")
    public ApiResponse<Void> deleteBackend(@PathVariable String apiId) {
        apiBackendService.deleteBackend(apiId);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 测试后端连接
     */
    @PostMapping("/test-connection")
    public ApiResponse<Boolean> testConnection(
            @PathVariable String apiId,
            @Valid @RequestBody ApiBackendRequest request) {
        boolean result = apiBackendService.testConnection(request);
        if (result) {
            return ApiResponse.success("连接成功", true);
        } else {
            return ApiResponse.success("连接失败", false);
        }
    }
}
