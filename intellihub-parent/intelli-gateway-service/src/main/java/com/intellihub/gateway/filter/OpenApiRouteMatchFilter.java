package com.intellihub.gateway.filter;

import com.intellihub.common.dubbo.ApiRouteDTO;
import com.intellihub.gateway.service.OpenApiRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import com.intellihub.gateway.config.FilterOrderConfig;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 开放API路由匹配过滤器
 * <p>
 * 在认证之前执行，负责：
 * 1. 匹配请求路径到API配置（支持路径参数）
 * 2. 将API信息存入exchange.getAttributes()供后续过滤器使用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiRouteMatchFilter implements GlobalFilter, Ordered {

    private final OpenApiRouteService routeService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Exchange属性Key - API路由配置
     */
    public static final String ATTR_API_ROUTE = "gateway.api.route";

    /**
     * Exchange属性Key - API ID
     */
    public static final String ATTR_API_ID = "gateway.api.id";

    /**
     * Exchange属性Key - 是否为开放API请求
     */
    public static final String ATTR_IS_OPEN_API = "gateway.api.isOpenApi";

    /**
     * 需要处理的开放API路径前缀
     */
    private static final List<String> OPEN_API_PATHS = Arrays.asList(
            "/open/**",
            "/external/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // 检查是否是开放API请求
        if (!isOpenApiRequest(path)) {
            exchange.getAttributes().put(ATTR_IS_OPEN_API, false);
            return chain.filter(exchange);
        }

        exchange.getAttributes().put(ATTR_IS_OPEN_API, true);
        log.debug("开放API路由匹配 - {} {}", method, path);

        // 匹配路由配置（支持路径参数）
        return routeService.matchRoute(path, method)
                .doOnNext(route -> {
                    // 将API信息存入exchange属性，供后续过滤器使用
                    exchange.getAttributes().put(ATTR_API_ROUTE, route);
                    exchange.getAttributes().put(ATTR_API_ID, route.getApiId());
                    log.debug("API路由匹配成功 - path: {}, apiId: {}, apiName: {}",
                            path, route.getApiId(), route.getApiName());
                })
                .then(chain.filter(exchange))
                .switchIfEmpty(chain.filter(exchange));
    }

    /**
     * 检查是否是开放API请求
     */
    private boolean isOpenApiRequest(String path) {
        for (String pattern : OPEN_API_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.OPEN_API_ROUTE_MATCH_FILTER;
    }
}
