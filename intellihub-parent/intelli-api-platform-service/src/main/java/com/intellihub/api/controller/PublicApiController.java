package com.intellihub.api.controller;

import com.intellihub.api.dto.request.ApiQueryRequest;
import com.intellihub.api.dto.response.ApiInfoResponse;
import com.intellihub.api.service.ApiInfoService;
import com.intellihub.ApiResponse;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开API控制器（API市场）
 * <p>
 * 提供跨租户的公开API查询，用于API市场展示
 * </p>
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
     * <p>
     * 只返回已发布状态的API，供API市场展示
     * </p>
     */
    @GetMapping("/list")
    public ApiResponse<PageData<ApiInfoResponse>> listPublicApis(ApiQueryRequest request) {
        PageData<ApiInfoResponse> pageData = apiInfoService.listPublicApis(request);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取公开API详情
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ApiInfoResponse> getPublicApiById(@PathVariable String id) {
        ApiInfoResponse api = apiInfoService.getApiById(id);
        // 只返回已发布的API
        if (api != null && "published".equals(api.getStatus())) {
            return ApiResponse.success(api);
        }
        return ApiResponse.error(404, "API不存在或未发布");
    }
}
