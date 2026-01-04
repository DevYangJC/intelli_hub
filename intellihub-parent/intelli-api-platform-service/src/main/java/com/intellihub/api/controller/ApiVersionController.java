package com.intellihub.api.controller;

import com.intellihub.api.constant.ApiConstants;
import com.intellihub.api.constant.ApiResponseMessage;
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
 * 提供API版本的管理功能，包括版本创建、查询、回滚、删除、比较等
 * 支持API的版本快照和版本历史管理
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
     * 返回按时间倒序排列的版本列表，包括版本号、创建时间、创建人等
     *
     * @param apiId API的唯一标识ID
     * @return API的所有版本列表
     */
    @GetMapping("/list")
    public ApiResponse<List<ApiVersionResponse>> listVersions(@PathVariable String apiId) {
        List<ApiVersionResponse> versions = apiVersionService.listVersions(apiId);
        return ApiResponse.success(versions);
    }

    /**
     * 获取指定版本的详细信息
     * 返回版本快照的完整配置，包括API定义、后端配置、参数定义等
     *
     * @param apiId API的唯一标识ID
     * @param versionId 版本ID
     * @return 版本详细信息
     */
    @GetMapping("/{versionId}/detail")
    public ApiResponse<ApiVersionResponse> getVersionDetail(
            @PathVariable String apiId,
            @PathVariable String versionId) {
        ApiVersionResponse version = apiVersionService.getVersion(versionId);
        return ApiResponse.success(version);
    }

    /**
     * 创建新版本
     * 保存当前API的完整配置快照，生成一个新的版本记录
     * 版本快照包括API的所有配置信息，用于版本回滚和历史查看
     *
     * @param apiId API的唯一标识ID
     * @param userId 用户ID，从请求头中获取
     * @param request 版本创建请求，包含版本号、描述等
     * @return 创建成功后返回版本详细信息
     */
    @PostMapping("/create")
    public ApiResponse<ApiVersionResponse> createVersion(
            @PathVariable String apiId,
            @RequestHeader(ApiConstants.Headers.USER_ID) String userId,
            @Valid @RequestBody CreateApiVersionRequest request) {
        ApiVersionResponse version = apiVersionService.createVersion(apiId, userId, request);
        return ApiResponse.success(ApiResponseMessage.VERSION_CREATE_SUCCESS, version);
    }

    /**
     * 回滚到指定版本
     * 将API的配置回滚到指定版本的状态，恢复所有配置信息
     * 回滚后需要重新发布API才能生效
     *
     * @param apiId API的唯一标识ID
     * @param versionId 目标版本ID
     * @return 操作结果
     */
    @PostMapping("/{versionId}/rollback")
    public ApiResponse<Void> rollbackToVersion(
            @PathVariable String apiId,
            @PathVariable String versionId) {
        apiVersionService.rollbackToVersion(apiId, versionId);
        return ApiResponse.success(ApiResponseMessage.ROLLBACK_SUCCESS, null);
    }

    /**
     * 删除版本
     * 删除指定的版本快照，删除后无法恢复
     * 不影响当前API的配置，只是删除历史记录
     *
     * @param apiId API的唯一标识ID
     * @param versionId 版本ID
     * @return 操作结果
     */
    @PostMapping("/{versionId}/delete")
    public ApiResponse<Void> deleteVersion(
            @PathVariable String apiId,
            @PathVariable String versionId) {
        apiVersionService.deleteVersion(versionId);
        return ApiResponse.success(ApiResponseMessage.VERSION_DELETE_SUCCESS, null);
    }

    /**
     * 比较两个版本的差异
     * 对比两个版本的配置差异，返回变更详情
     * 用于版本对比和变更审计
     *
     * @param apiId API的唯一标识ID
     * @param versionId1 第一个版本ID
     * @param versionId2 第二个版本ID
     * @return 版本差异详情
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
