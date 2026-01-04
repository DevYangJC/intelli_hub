package com.intellihub.api.controller;

import com.intellihub.api.constant.ApiResponseMessage;
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
 * 提供API请求参数和响应参数的管理功能
 * 支持参数的增删改查、批量保存、参数验证等
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
     * 返回API的所有请求参数定义，包括参数名、类型、是否必填、默认值等
     *
     * @param apiId API的唯一标识ID
     * @return 请求参数列表
     */
    @GetMapping("/request/list")
    public ApiResponse<List<ApiParamResponse>> listRequestParams(@PathVariable String apiId) {
        List<ApiParamResponse> params = apiParamService.listRequestParams(apiId);
        return ApiResponse.success(params);
    }

    /**
     * 批量保存请求参数
     * 一次性保存多个请求参数，会先删除原有参数再新增
     * 适用于参数表单的批量保存场景
     *
     * @param apiId API的唯一标识ID
     * @param params 请求参数列表
     * @return 操作结果
     */
    @PostMapping("/request/batch-save")
    public ApiResponse<Void> saveRequestParams(
            @PathVariable String apiId,
            @Valid @RequestBody List<ApiParamRequest> params) {
        apiParamService.saveRequestParams(apiId, params);
        return ApiResponse.success(ApiResponseMessage.SAVE_SUCCESS, null);
    }

    /**
     * 添加单个请求参数
     * 为API添加一个新的请求参数定义
     *
     * @param apiId API的唯一标识ID
     * @param request 参数定义请求，包含参数名、类型、是否必填等
     * @return 添加成功后返回参数详细信息
     */
    @PostMapping("/request/add")
    public ApiResponse<ApiParamResponse> addRequestParam(
            @PathVariable String apiId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.addRequestParam(apiId, request);
        return ApiResponse.success(ApiResponseMessage.ADD_SUCCESS, response);
    }

    /**
     * 更新请求参数
     * 修改已存在的请求参数定义
     *
     * @param apiId API的唯一标识ID
     * @param paramId 参数ID
     * @param request 参数更新请求
     * @return 更新成功后返回参数详细信息
     */
    @PostMapping("/request/{paramId}/update")
    public ApiResponse<ApiParamResponse> updateRequestParam(
            @PathVariable String apiId,
            @PathVariable String paramId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.updateRequestParam(paramId, request);
        return ApiResponse.success(ApiResponseMessage.UPDATE_SUCCESS, response);
    }

    /**
     * 删除请求参数
     * 删除指定的请求参数定义
     *
     * @param apiId API的唯一标识ID
     * @param paramId 参数ID
     * @return 操作结果
     */
    @PostMapping("/request/{paramId}/delete")
    public ApiResponse<Void> deleteRequestParam(
            @PathVariable String apiId,
            @PathVariable String paramId) {
        apiParamService.deleteRequestParam(paramId);
        return ApiResponse.success(ApiResponseMessage.DELETE_SUCCESS, null);
    }

    // ==================== 响应参数管理 ====================

    /**
     * 获取API的响应参数列表
     * 返回API的所有响应参数定义，包括参数名、类型、示例值等
     *
     * @param apiId API的唯一标识ID
     * @return 响应参数列表
     */
    @GetMapping("/response/list")
    public ApiResponse<List<ApiParamResponse>> listResponseParams(@PathVariable String apiId) {
        List<ApiParamResponse> params = apiParamService.listResponseParams(apiId);
        return ApiResponse.success(params);
    }

    /**
     * 批量保存响应参数
     * 一次性保存多个响应参数，会先删除原有参数再新增
     * 适用于参数表单的批量保存场景
     *
     * @param apiId API的唯一标识ID
     * @param params 响应参数列表
     * @return 操作结果
     */
    @PostMapping("/response/batch-save")
    public ApiResponse<Void> saveResponseParams(
            @PathVariable String apiId,
            @Valid @RequestBody List<ApiParamRequest> params) {
        apiParamService.saveResponseParams(apiId, params);
        return ApiResponse.success(ApiResponseMessage.SAVE_SUCCESS, null);
    }

    /**
     * 添加单个响应参数
     * 为API添加一个新的响应参数定义
     *
     * @param apiId API的唯一标识ID
     * @param request 参数定义请求，包含参数名、类型、示例值等
     * @return 添加成功后返回参数详细信息
     */
    @PostMapping("/response/add")
    public ApiResponse<ApiParamResponse> addResponseParam(
            @PathVariable String apiId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.addResponseParam(apiId, request);
        return ApiResponse.success(ApiResponseMessage.ADD_SUCCESS, response);
    }

    /**
     * 更新响应参数
     * 修改已存在的响应参数定义
     *
     * @param apiId API的唯一标识ID
     * @param paramId 参数ID
     * @param request 参数更新请求
     * @return 更新成功后返回参数详细信息
     */
    @PostMapping("/response/{paramId}/update")
    public ApiResponse<ApiParamResponse> updateResponseParam(
            @PathVariable String apiId,
            @PathVariable String paramId,
            @Valid @RequestBody ApiParamRequest request) {
        ApiParamResponse response = apiParamService.updateResponseParam(paramId, request);
        return ApiResponse.success(ApiResponseMessage.UPDATE_SUCCESS, response);
    }

    /**
     * 删除响应参数
     * 删除指定的响应参数定义
     *
     * @param apiId API的唯一标识ID
     * @param paramId 参数ID
     * @return 操作结果
     */
    @PostMapping("/response/{paramId}/delete")
    public ApiResponse<Void> deleteResponseParam(
            @PathVariable String apiId,
            @PathVariable String paramId) {
        apiParamService.deleteResponseParam(paramId);
        return ApiResponse.success(ApiResponseMessage.DELETE_SUCCESS, null);
    }
}
