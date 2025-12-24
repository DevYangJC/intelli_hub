package com.intellihub.api.controller;

import com.intellihub.api.dto.request.ApiParamRequest;
import com.intellihub.api.dto.response.ApiParamResponse;
import com.intellihub.api.service.ApiParamService;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API参数管理控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/apis/{apiId}/params")
@RequiredArgsConstructor
public class ApiParamController {

    private final ApiParamService apiParamService;

    // ==================== 请求参数管理 ====================

    /**
     * 获取API的请求参数列表
     */
    @GetMapping("/request/list")
    public ApiResponse<List<ApiParamResponse>> listRequestParams(@PathVariable String apiId) {
        List<ApiParamResponse> params = apiParamService.listRequestParams(apiId);
        return ApiResponse.success(params);
    }

    /**
     * 批量保存请求参数
     */
    @PostMapping("/request/batch-save")
    public ApiResponse<Void> saveRequestParams(
            @PathVariable String apiId,
            @Valid @RequestBody List<ApiParamRequest> params) {
        apiParamService.saveRequestParams(apiId, params);
        return ApiResponse.success("保存成功", null);
    }

    /**
     * 添加请求参数
     */
    @PostMapping("/request/add")
    public ApiResponse<ApiParamResponse> addRequestParam(
            @PathVariable String apiId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.addRequestParam(apiId, request);
        return ApiResponse.success("添加成功", response);
    }

    /**
     * 更新请求参数
     */
    @PostMapping("/request/{paramId}/update")
    public ApiResponse<ApiParamResponse> updateRequestParam(
            @PathVariable String apiId,
            @PathVariable String paramId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.updateRequestParam(paramId, request);
        return ApiResponse.success("更新成功", response);
    }

    /**
     * 删除请求参数
     */
    @PostMapping("/request/{paramId}/delete")
    public ApiResponse<Void> deleteRequestParam(
            @PathVariable String apiId,
            @PathVariable String paramId) {
        apiParamService.deleteRequestParam(paramId);
        return ApiResponse.success("删除成功", null);
    }

    // ==================== 响应参数管理 ====================

    /**
     * 获取API的响应参数列表
     */
    @GetMapping("/response/list")
    public ApiResponse<List<ApiParamResponse>> listResponseParams(@PathVariable String apiId) {
        List<ApiParamResponse> params = apiParamService.listResponseParams(apiId);
        return ApiResponse.success(params);
    }

    /**
     * 批量保存响应参数
     */
    @PostMapping("/response/batch-save")
    public ApiResponse<Void> saveResponseParams(
            @PathVariable String apiId,
            @Valid @RequestBody List<ApiParamRequest> params) {
        apiParamService.saveResponseParams(apiId, params);
        return ApiResponse.success("保存成功", null);
    }

    /**
     * 添加响应参数
     */
    @PostMapping("/response/add")
    public ApiResponse<ApiParamResponse> addResponseParam(
            @PathVariable String apiId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.addResponseParam(apiId, request);
        return ApiResponse.success("添加成功", response);
    }

    /**
     * 更新响应参数
     */
    @PostMapping("/response/{paramId}/update")
    public ApiResponse<ApiParamResponse> updateResponseParam(
            @PathVariable String apiId,
            @PathVariable String paramId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.updateResponseParam(paramId, request);
        return ApiResponse.success("更新成功", response);
    }

    /**
     * 删除响应参数
     */
    @PostMapping("/response/{paramId}/delete")
    public ApiResponse<Void> deleteResponseParam(
            @PathVariable String apiId,
            @PathVariable String paramId) {
        apiParamService.deleteResponseParam(paramId);
        return ApiResponse.success("删除成功", null);
    }
}
