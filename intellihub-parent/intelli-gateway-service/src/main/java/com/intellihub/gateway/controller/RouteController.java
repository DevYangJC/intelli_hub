package com.intellihub.gateway.controller;

import com.intellihub.gateway.service.OpenApiRouteService;
import com.intellihub.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 路由管理控制器
 * <p>
 * 提供路由刷新等管理功能
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/gateway/routes")
@RequiredArgsConstructor
public class RouteController {

    private final OpenApiRouteService openApiRouteService;

    /**
     * 刷新路由配置（统一使用开放API路由刷新）
     */
    @PostMapping("/refresh")
    public Mono<ApiResponse<String>> refreshRoutes() {
        return Mono.fromRunnable(() -> openApiRouteService.refreshAllRoutes())
                .thenReturn(ApiResponse.success("路由刷新成功，共加载 " + openApiRouteService.getRouteCacheSize() + " 条路由"));
    }

    /**
     * 刷新开放API路由配置
     * <p>
     * 从API Platform重新加载所有已发布的API路由配置
     * </p>
     */
    @PostMapping("/open-api/refresh")
    public Mono<ApiResponse<String>> refreshOpenApiRoutes() {
        return Mono.fromRunnable(() -> openApiRouteService.refreshAllRoutes())
                .thenReturn(ApiResponse.success("开放API路由刷新成功，共加载 " + openApiRouteService.getRouteCacheSize() + " 条路由"));
    }

    /**
     * 刷新单个API的路由配置
     *
     * @param apiId API ID
     */
    @PostMapping("/open-api/refresh-one")
    public Mono<ApiResponse<String>> refreshOneRoute(@RequestParam String apiId) {
        return openApiRouteService.refreshRoute(apiId)
                .thenReturn(ApiResponse.success("API路由刷新成功"));
    }

    /**
     * 获取开放API路由缓存状态
     */
    @GetMapping("/open-api/status")
    public Mono<ApiResponse<String>> getOpenApiRouteStatus() {
        int cacheSize = openApiRouteService.getRouteCacheSize();
        return Mono.just(ApiResponse.success("当前缓存路由数量: " + cacheSize));
    }
}
