package com.intellihub.api.controller;

import com.intellihub.api.dto.request.CreateApiVersionRequest;
import com.intellihub.api.dto.response.ApiVersionResponse;
import com.intellihub.api.service.ApiVersionService;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API版本管理控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/apis/{apiId}/versions")
@RequiredArgsConstructor
public class ApiVersionController {

    private final ApiVersionService apiVersionService;

    /**
     * 获取API的版本历史列表
     */
    @GetMapping("/list")
    public ApiResponse<List<ApiVersionResponse>> listVersions(@PathVariable String apiId) {
        List<ApiVersionResponse> versions = apiVersionService.listVersions(apiId);
        return ApiResponse.success(versions);
    }

    /**
     * 获取指定版本详情
     */
    @GetMapping("/{versionId}/detail")
    public ApiResponse<ApiVersionResponse> getVersionDetail(
            @PathVariable String apiId,
            @PathVariable String versionId) {
        ApiVersionResponse version = apiVersionService.getVersion(versionId);
        return ApiResponse.success(version);
    }

    /**
     * 创建新版本（保存当前API快照）
     */
    @PostMapping("/create")
    public ApiResponse<ApiVersionResponse> createVersion(
            @PathVariable String apiId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateApiVersionRequest request) {
        ApiVersionResponse version = apiVersionService.createVersion(apiId, userId, request);
        return ApiResponse.success("版本创建成功", version);
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/{versionId}/rollback")
    public ApiResponse<Void> rollbackToVersion(
            @PathVariable String apiId,
            @PathVariable String versionId) {
        apiVersionService.rollbackToVersion(apiId, versionId);
        return ApiResponse.success("版本回滚成功", null);
    }

    /**
     * 删除版本
     */
    @PostMapping("/{versionId}/delete")
    public ApiResponse<Void> deleteVersion(
            @PathVariable String apiId,
            @PathVariable String versionId) {
        apiVersionService.deleteVersion(versionId);
        return ApiResponse.success("版本删除成功", null);
    }

    /**
     * 比较两个版本差异
     */
    @GetMapping("/compare")
    public ApiResponse<String> compareVersions(
            @PathVariable String apiId,
            @RequestParam String versionId1,
            @RequestParam String versionId2) {
        String diff = apiVersionService.compareVersions(versionId1, versionId2);
        return ApiResponse.success(diff);
    }
}
