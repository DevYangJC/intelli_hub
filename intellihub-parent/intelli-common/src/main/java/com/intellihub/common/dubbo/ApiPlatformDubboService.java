package com.intellihub.common.dubbo;

import java.util.List;

/**
 * API平台Dubbo服务接口
 * <p>
 * 提供API路由配置查询功能，供网关动态路由使用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiPlatformDubboService {

    /**
     * 根据请求路径获取API路由配置
     *
     * @param path   请求路径（如 /open/user/info）
     * @param method 请求方法（如 GET）
     * @return 路由配置，不存在返回null
     */
    ApiRouteDTO getRouteByPath(String path, String method);

    /**
     * 获取所有已发布的API路由配置
     * <p>
     * 用于网关启动时加载路由配置
     * </p>
     *
     * @return 所有已发布的路由配置列表
     */
    List<ApiRouteDTO> getAllPublishedRoutes();

    /**
     * 根据API ID获取路由配置
     *
     * @param apiId API ID
     * @return 路由配置
     */
    ApiRouteDTO getRouteByApiId(String apiId);

    /**
     * 检查API路径是否存在且已发布
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return 是否存在
     */
    boolean isApiPublished(String path, String method);

    /**
     * 根据实际请求路径匹配API路由（支持路径参数）
     * <p>
     * 例如：/open/user/123 可以匹配 /open/user/{id}
     * </p>
     *
     * @param requestPath 实际请求路径
     * @param method      请求方法
     * @return 匹配的路由配置，不存在返回null
     */
    ApiRouteDTO matchRouteByPath(String requestPath, String method);
}
