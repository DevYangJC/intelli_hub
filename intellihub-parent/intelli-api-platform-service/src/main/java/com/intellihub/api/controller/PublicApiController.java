package com.intellihub.api.controller;

import com.intellihub.api.constant.ApiConstants;
import com.intellihub.api.constant.ApiResponseMessage;
import com.intellihub.api.dto.request.ApiQueryRequest;
import com.intellihub.api.dto.response.ApiInfoResponse;
import com.intellihub.api.service.ApiInfoService;
import com.intellihub.ApiResponse;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开API控制器（API市场）
 * 提供跨租户的公开API查询功能，用于API市场展示和发现
 * 只展示已发布状态的公开API，不涉及租户隔离
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/public/apis")
@RequiredArgsConstructor
public class PublicApiController {

    private final ApiInfoService apiInfoService;

    /**
     * 获取公开API列表（跨租户）
     * 查询所有租户的公开API，只返回已发布状态的API
     * 用于API市场展示，支持按分类、关键词等条件筛选
     *
     * @param request 查询请求，包含分页参数、筛选条件等
     * @return 分页后的公开API列表
     */
    @GetMapping("/list")
    public ApiResponse<PageData<ApiInfoResponse>> listPublicApis(ApiQueryRequest request) {
        PageData<ApiInfoResponse> pageData = apiInfoService.listPublicApis(request);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取公开API详情
     * 查询指定API的详细信息，只能查看已发布状态的公开API
     * 未发布或私有API无法通过此接口访问
     *
     * @param id API的唯一标识ID
     * @return API详细信息，如果API不存在或未发布则返回404错误
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ApiInfoResponse> getPublicApiById(@PathVariable String id) {
        ApiInfoResponse api = apiInfoService.getApiById(id);
        if (api != null && ApiConstants.ApiStatus.PUBLISHED.equals(api.getStatus())) {
            return ApiResponse.success(api);
        }
        return ApiResponse.error(ApiConstants.HttpStatus.NOT_FOUND, ApiResponseMessage.NOT_FOUND);
    }
}
