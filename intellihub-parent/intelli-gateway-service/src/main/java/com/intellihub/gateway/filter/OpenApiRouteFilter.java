package com.intellihub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.common.dubbo.ApiRouteDTO;
import com.intellihub.gateway.service.DubboGenericService;
import com.intellihub.gateway.service.OpenApiRouteService;
import com.intellihub.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 开放API动态路由过滤器
 * <p>
 * 负责将开放API请求动态转发到真正的后端服务
 * 1. 查找路由配置
 * 2. 根据后端配置转发请求
 * 3. 支持HTTP后端和Mock响应
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiRouteFilter implements GlobalFilter, Ordered {

    private final OpenApiRouteService routeService;
    private final DubboGenericService dubboGenericService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 检查是否是开放API请求（由OpenApiRouteMatchFilter标记）
        Boolean isOpenApi = (Boolean) exchange.getAttributes().get(OpenApiRouteMatchFilter.ATTR_IS_OPEN_API);
        if (!Boolean.TRUE.equals(isOpenApi)) {
            return chain.filter(exchange);
        }

        // 从exchange属性获取路由配置（由OpenApiRouteMatchFilter设置）
        ApiRouteDTO route = (ApiRouteDTO) exchange.getAttributes().get(OpenApiRouteMatchFilter.ATTR_API_ROUTE);
        
        if (route == null) {
            log.warn("未找到API路由配置 - path: {}", path);
            return handleNotFound(exchange.getResponse(), "API不存在或未发布");
        }

        log.debug("开放API路由处理 - path: {}, apiId: {}, backendType: {}", 
                path, route.getApiId(), route.getBackendType());

        // 检查是否启用Mock
        if (Boolean.TRUE.equals(route.getMockEnabled()) && route.getMockResponse() != null) {
            log.debug("返回Mock响应 - path: {}", path);
            return handleMockResponse(exchange.getResponse(), route.getMockResponse());
        }

        // 根据后端类型转发
        if ("http".equalsIgnoreCase(route.getBackendType())) {
            return forwardToHttpBackend(exchange, chain, route);
        } else if ("dubbo".equalsIgnoreCase(route.getBackendType())) {
            return forwardToDubboBackend(exchange, route);
        } else {
            log.warn("未知的后端类型 - type: {}, path: {}", route.getBackendType(), path);
            return handleError(exchange.getResponse(), 500, "后端配置错误");
        }
    }

    /**
     * 转发到HTTP后端
     */
    private Mono<Void> forwardToHttpBackend(ServerWebExchange exchange, GatewayFilterChain chain, ApiRouteDTO route) {
        ServerHttpRequest request = exchange.getRequest();

        // 构建后端URI
        String backendUri = buildBackendUri(route);
        log.info("转发请求到后端 - 原路径: {}, 后端地址: {}", request.getPath().value(), backendUri);

        try {
            URI uri = URI.create(backendUri);

            // 修改请求，设置新的目标URI
            ServerHttpRequest modifiedRequest = request.mutate()
                    .uri(uri)
                    .method(HttpMethod.valueOf(route.getBackendMethod() != null ? 
                            route.getBackendMethod() : request.getMethod().name()))
                    .build();

            // 设置路由URI属性，让Gateway知道转发目标
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, uri);

            // 继续过滤器链
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            log.error("构建后端URI失败 - route: {}", route, e);
            return handleError(exchange.getResponse(), 500, "路由配置错误");
        }
    }

    /**
     * 转发到Dubbo后端（泛化调用）
     */
    private Mono<Void> forwardToDubboBackend(ServerWebExchange exchange, ApiRouteDTO route) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.info("Dubbo泛化调用 - path: {}, interface: {}, method: {}",
                path, route.getDubboInterface(), route.getDubboMethod());

        // 从Query参数和Body获取调用参数
        return extractRequestParams(exchange)
                .flatMap(params -> dubboGenericService.invoke(route, params))
                .flatMap(result -> {
                    // 将Dubbo调用结果返回给客户端
                    return handleDubboResponse(exchange.getResponse(), result);
                })
                .onErrorResume(e -> {
                    log.error("Dubbo泛化调用失败 - path: {}", path, e);
                    return handleError(exchange.getResponse(), 500, "Dubbo服务调用失败: " + e.getMessage());
                });
    }

    /**
     * 提取请求参数
     * <p>
     * 从Query参数和缓存的Body中提取参数
     * </p>
     */
    private Mono<java.util.Map<String, Object>> extractRequestParams(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        java.util.Map<String, Object> params = new java.util.HashMap<>();

        // 提取Query参数
        request.getQueryParams().forEach((key, values) -> {
            if (values.size() == 1) {
                params.put(key, values.get(0));
            } else {
                params.put(key, values);
            }
        });

        // 如果是POST/PUT/PATCH，从缓存的Body中提取参数
        String method = request.getMethod().name();
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            // 使用CacheBodyFilter缓存的Body
            String cachedBody = (String) exchange.getAttributes().get(CacheBodyFilter.ATTR_CACHED_BODY);
            if (cachedBody != null && !cachedBody.isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> bodyParams = objectMapper.readValue(cachedBody, java.util.Map.class);
                    params.putAll(bodyParams);
                    log.debug("从缓存Body提取参数 - params: {}", bodyParams.keySet());
                } catch (Exception e) {
                    log.warn("解析请求Body失败 - body: {}", cachedBody.substring(0, Math.min(100, cachedBody.length())), e);
                }
            }
        }

        return Mono.just(params);
    }

    /**
     * 处理Dubbo响应
     */
    private Mono<Void> handleDubboResponse(ServerHttpResponse response, Object result) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String json = objectMapper.writeValueAsString(ApiResponse.success(result));
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("序列化Dubbo响应失败", e);
            return handleError(response, 500, "响应序列化失败");
        }
    }

    /**
     * 构建后端URI
     */
    private String buildBackendUri(ApiRouteDTO route) {
        StringBuilder sb = new StringBuilder();

        // 协议
        String protocol = route.getBackendProtocol();
        if (protocol == null || protocol.isEmpty()) {
            protocol = "http";
        }
        sb.append(protocol.toLowerCase()).append("://");

        // 主机地址
        String host = route.getBackendHost();
        if (host != null && !host.isEmpty()) {
            // 如果是服务名（不包含端口和IP格式），使用lb://前缀
            if (!host.contains(":") && !host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                return "lb://" + host + (route.getBackendPath() != null ? route.getBackendPath() : "");
            }
            sb.append(host);
        }

        // 路径
        if (route.getBackendPath() != null && !route.getBackendPath().isEmpty()) {
            if (!route.getBackendPath().startsWith("/")) {
                sb.append("/");
            }
            sb.append(route.getBackendPath());
        }

        return sb.toString();
    }

    /**
     * 处理Mock响应
     */
    private Mono<Void> handleMockResponse(ServerHttpResponse response, String mockData) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = response.bufferFactory().wrap(mockData.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 处理404
     */
    private Mono<Void> handleNotFound(ServerHttpResponse response, String message) {
        return handleError(response, 404, message);
    }

    /**
     * 处理错误响应
     */
    private Mono<Void> handleError(ServerHttpResponse response, int statusCode, String message) {
        response.setStatusCode(HttpStatus.valueOf(statusCode));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<?> errorResponse = ApiResponse.failed(statusCode, message);

        try {
            String result = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写入响应失败:", e);
            return Mono.error(e);
        }
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.OPEN_API_ROUTE_FILTER;
    }
}
