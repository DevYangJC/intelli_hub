package com.intellihub.api.service;

import com.intellihub.api.dto.request.ApiBackendRequest;
import com.intellihub.api.dto.response.ApiBackendResponse;

/**
 * API后端配置服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiBackendService {

    /**
     * 获取API的后端配置
     */
    ApiBackendResponse getBackendByApiId(String apiId);

    /**
     * 保存或更新后端配置
     */
    ApiBackendResponse saveBackend(String apiId, ApiBackendRequest request);

    /**
     * 删除后端配置
     */
    void deleteBackend(String apiId);

    /**
     * 测试后端连接
     */
    boolean testConnection(ApiBackendRequest request);
}
