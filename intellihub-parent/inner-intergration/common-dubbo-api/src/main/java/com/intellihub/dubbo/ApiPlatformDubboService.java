package com.intellihub.dubbo;

import java.util.List;

/**
 * API平台Dubbo服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiPlatformDubboService {

    /**
     * 根据路径和方法获取API路由
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return API路由
     */
    ApiRouteDTO getRouteByPath(String path, String method);

    /**
     * 获取所有已发布的API路由
     *
     * @return API路由列表
     */
    List<ApiRouteDTO> getAllPublishedRoutes();

    /**
     * 根据API ID获取路由
     *
     * @param apiId API ID
     * @return API路由
     */
    ApiRouteDTO getRouteByApiId(String apiId);

    /**
     * 检查API是否已发布
     *
     * @param path   路径
     * @param method 方法
     * @return 是否已发布
     */
    boolean isApiPublished(String path, String method);

    /**
     * 通配符匹配路由
     *
     * @param requestPath 请求路径
     * @param method      请求方法
     * @return API路由
     */
    ApiRouteDTO matchRouteByPath(String requestPath, String method);

    /**
     * 批量更新API调用次数
     * <p>
     * 由Governance服务调用，同步调用统计数据到API表
     * </p>
     *
     * @param callCounts API调用次数统计列表
     * @return 更新成功的记录数
     */
    int batchUpdateApiCallCounts(List<ApiCallCountDTO> callCounts);

    /**
     * 获取所有 API 信息（用于搜索索引同步）
     *
     * @param tenantId 租户ID（可选，为空则查所有租户）
     * @return API 信息列表
     */
    List<ApiInfoDTO> getAllApiInfoForSync(Long tenantId);

    /**
     * 获取增量更新的 API 信息（用于搜索索引增量同步）
     *
     * @param tenantId    租户ID（可选）
     * @param lastSyncTime 上次同步时间
     * @return API 信息列表
     */
    List<ApiInfoDTO> getApiInfoUpdatedAfter(Long tenantId, java.time.LocalDateTime lastSyncTime);
}
