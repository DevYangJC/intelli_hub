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
}
