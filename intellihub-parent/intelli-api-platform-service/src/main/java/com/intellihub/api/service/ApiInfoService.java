package com.intellihub.api.service;

import com.intellihub.api.dto.request.ApiQueryRequest;
import com.intellihub.api.dto.request.CreateApiRequest;
import com.intellihub.api.dto.request.UpdateApiRequest;
import com.intellihub.api.dto.response.ApiInfoResponse;
import com.intellihub.api.dto.response.ApiRouteResponse;
import com.intellihub.page.PageData;

import java.util.List;

/**
 * API信息服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiInfoService {

    /**
     * 分页查询API列表
     * <p>租户ID由多租户拦截器自动处理</p>
     */
    PageData<ApiInfoResponse> listApis(ApiQueryRequest request);

    /**
     * 获取API详情
     */
    ApiInfoResponse getApiById(String id);

    /**
     * 创建API
     * <p>租户ID由多租户拦截器自动处理</p>
     */
    ApiInfoResponse createApi(String userId, String username, CreateApiRequest request);

    /**
     * 更新API
     */
    ApiInfoResponse updateApi(String id, UpdateApiRequest request);

    /**
     * 删除API
     */
    void deleteApi(String id);

    /**
     * 发布API
     */
    void publishApi(String id);

    /**
     * 下线API
     */
    void offlineApi(String id);

    /**
     * 废弃API
     */
    void deprecateApi(String id);

    /**
     * 复制API
     * <p>租户ID由多租户拦截器自动处理</p>
     */
    ApiInfoResponse copyApi(String id, String userId, String username);

    /**
     * 获取所有已发布API的路由配置
     * <p>
     * 供网关动态路由使用
     * </p>
     */
    List<ApiRouteResponse> getPublishedApiRoutes();

    /**
     * 分页查询公开API列表（跨租户，用于API市场）
     * <p>
     * 只返回已发布且公开的API
     * </p>
     */
    PageData<ApiInfoResponse> listPublicApis(ApiQueryRequest request);
}
