package com.intellihub.api.service;

import com.intellihub.api.dto.request.CreateApiVersionRequest;
import com.intellihub.api.dto.response.ApiVersionResponse;

import java.util.List;

/**
 * API版本服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiVersionService {

    /**
     * 获取API的版本历史列表
     */
    List<ApiVersionResponse> listVersions(String apiId);

    /**
     * 获取指定版本详情
     */
    ApiVersionResponse getVersion(String versionId);

    /**
     * 创建新版本（保存当前API快照）
     */
    ApiVersionResponse createVersion(String apiId, String userId, CreateApiVersionRequest request);

    /**
     * 回滚到指定版本
     */
    void rollbackToVersion(String apiId, String versionId);

    /**
     * 删除版本
     */
    void deleteVersion(String versionId);

    /**
     * 比较两个版本差异
     */
    String compareVersions(String versionId1, String versionId2);
}
