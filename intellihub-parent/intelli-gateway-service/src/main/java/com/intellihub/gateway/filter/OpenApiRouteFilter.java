package com.intellihub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.dubbo.ApiRouteDTO;
import com.intellihub.gateway.service.DubboGenericService;
import com.intellihub.gateway.service.OpenApiRouteService;
import com.intellihub.gateway.service.dubbo.DubboInvocationContext;
import com.intellihub.gateway.service.dubbo.DubboInvocationContextBuilder;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
    private final DubboInvocationContextBuilder contextBuilder;
    private final LoadBalancerClient loadBalancerClient;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.create();

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
     * 转发到HTTP后端（使用LoadBalancerClient解析服务名，然后用WebClient调用）
     */
    private Mono<Void> forwardToHttpBackend(ServerWebExchange exchange, GatewayFilterChain chain, ApiRouteDTO route) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // === 新增：缓存检查 ===
        if (Boolean.TRUE.equals(route.getCacheEnabled()) && route.getCacheTtl() != null && route.getCacheTtl() > 0) {
            // 只对GET请求启用缓存
            if (HttpMethod.GET.equals(request.getMethod())) {
                log.debug("API启用缓存 - apiId: {}, ttl: {}s", route.getApiId(), route.getCacheTtl());
                return checkCacheAndForward(exchange, route);
            }
        }

        try {
            // 解析后端地址
            String actualUri = resolveBackendUri(route);
            log.info("转发请求到后端 - 原路径: {}, 后端地址: {}", request.getPath().value(), actualUri);

            String method = route.getBackendMethod() != null ? route.getBackendMethod() : request.getMethod().name();
            
            // 使用WebClient转发请求
            WebClient.RequestBodySpec requestSpec = webClient
                    .method(HttpMethod.valueOf(method))
                    .uri(actualUri)
                    .headers(headers -> {
                        // 复制原始请求头（排除Host）
                        request.getHeaders().forEach((name, values) -> {
                            if (!"Host".equalsIgnoreCase(name)) {
                                headers.addAll(name, values);
                            }
                        });
                    });

            // 获取缓存的请求体
            String cachedBody = (String) exchange.getAttributes().get(CacheBodyFilter.ATTR_CACHED_BODY);
            
            Mono<String> responseMono;
            if (cachedBody != null && !cachedBody.isEmpty()) {
                responseMono = requestSpec.bodyValue(cachedBody)
                        .retrieve()
                        .bodyToMono(String.class);
            } else {
                responseMono = requestSpec.retrieve().bodyToMono(String.class);
            }

            return responseMono
                    .flatMap(body -> {
                        response.setStatusCode(HttpStatus.OK);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                        return response.writeWith(Mono.just(buffer));
                    })
                    .onErrorResume(e -> {
                        log.error("转发请求失败 - uri: {}", actualUri, e);
                        return handleError(response, 502, "后端服务调用失败: " + e.getMessage());
                    });
        } catch (Exception e) {
            log.error("构建后端URI失败 - route: {}", route, e);
            return handleError(response, 500, "路由配置错误: " + e.getMessage());
        }
    }

    /**
     * 解析后端URI（如果是服务名，使用LoadBalancer解析为实际地址）
     */
    private String resolveBackendUri(ApiRouteDTO route) {
        String host = route.getBackendHost();
        String path = route.getBackendPath();
        
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("后端主机地址为空");
        }
        
        // 去除host中可能存在的协议前缀
        String cleanHost = host;
        if (host.startsWith("http://")) {
            cleanHost = host.substring(7);
        } else if (host.startsWith("https://")) {
            cleanHost = host.substring(8);
        }
        
        // 构建路径部分
        String fullPath = "";
        if (path != null && !path.isEmpty()) {
            fullPath = path.startsWith("/") ? path : "/" + path;
        }
        
        // 判断是否是服务名（不包含端口和IP格式）
        if (!cleanHost.contains(":") && !cleanHost.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            // 使用LoadBalancer解析服务名
            ServiceInstance instance = loadBalancerClient.choose(cleanHost);
            if (instance == null) {
                throw new IllegalStateException("服务不可用: " + cleanHost);
            }
            return instance.getUri().toString() + fullPath;
        }
        
        // 否则直接使用配置的地址
        String protocol = route.getBackendProtocol();
        if (protocol == null || protocol.isEmpty()) {
            protocol = "http";
        }
        return protocol.toLowerCase() + "://" + cleanHost + fullPath;
    }

    /**
     * 转发到Dubbo后端（泛化调用）
     * <p>
     * 使用建造者模式构建调用上下文，策略模式执行调用
     * </p>
     */
    private Mono<Void> forwardToDubboBackend(ServerWebExchange exchange, ApiRouteDTO route) {
        log.info("[OpenApiRouteFilter] Dubbo转发开始: path={}, interface={}, method={}, group={}", 
                route.getPath(), route.getDubboInterface(), route.getDubboMethod(), route.getDubboGroup());

        // 使用建造者构建调用上下文（参数提取由提取器链完成）
        DubboInvocationContext context = contextBuilder.build(exchange, route);

        // 执行泛化调用
        return dubboGenericService.invoke(context)
                .flatMap(result -> handleDubboResponse(exchange.getResponse(), result))
                .doOnSuccess(v -> log.info("[OpenApiRouteFilter] Dubbo转发完成: {}", context.toSummary()))
                .onErrorResume(e -> {
                    log.error("[OpenApiRouteFilter] Dubbo转发失败: {}, error={}", context.toSummary(), e.getMessage(), e);
                    return handleError(exchange.getResponse(), 500, "Dubbo服务调用失败: " + e.getMessage());
                });
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

    /**
     * 构建缓存Key
     */
    private String buildCacheKey(ServerHttpRequest request, ApiRouteDTO route) {
        String queryParams = request.getURI().getQuery();
        
        // 计算参数哈希
        String paramsHash = "";
        if (queryParams != null && !queryParams.isEmpty()) {
            paramsHash = DigestUtils.md5DigestAsHex(queryParams.getBytes(StandardCharsets.UTF_8));
        }
        
        return String.format("api:response:cache:%s:%s", route.getApiId(), paramsHash);
    }

    /**
     * 检查缓存并转发
     */
    private Mono<Void> checkCacheAndForward(ServerWebExchange exchange, ApiRouteDTO route) {
        String cacheKey = buildCacheKey(exchange.getRequest(), route);
        
        // 1. 尝试从Redis获取缓存
        return redisTemplate.opsForValue().get(cacheKey)
                .flatMap(cachedResponse -> {
                    // 缓存命中
                    log.debug("API响应缓存命中 - apiId: {}, cacheKey: {}", route.getApiId(), cacheKey);
                    return writeCachedResponse(exchange.getResponse(), cachedResponse);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 缓存未命中，转发请求并缓存响应
                    log.debug("API响应缓存未命中 - apiId: {}, cacheKey: {}", route.getApiId(), cacheKey);
                    return forwardAndCache(exchange, route, cacheKey);
                }));
    }

    /**
     * 转发请求并缓存响应
     */
    private Mono<Void> forwardAndCache(ServerWebExchange exchange, ApiRouteDTO route, String cacheKey) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        try {
            String actualUri = resolveBackendUri(route);
            String method = route.getBackendMethod() != null ? route.getBackendMethod() : request.getMethod().name();
            
            WebClient.RequestBodySpec requestSpec = webClient
                    .method(HttpMethod.valueOf(method))
                    .uri(actualUri)
                    .headers(headers -> {
                        request.getHeaders().forEach((name, values) -> {
                            if (!"Host".equalsIgnoreCase(name)) {
                                headers.addAll(name, values);
                            }
                        });
                    });

            // 获取缓存的请求体
            String cachedBody = (String) exchange.getAttributes().get(CacheBodyFilter.ATTR_CACHED_BODY);
            
            Mono<String> responseMono;
            if (cachedBody != null && !cachedBody.isEmpty()) {
                responseMono = requestSpec.bodyValue(cachedBody)
                        .retrieve()
                        .bodyToMono(String.class);
            } else {
                responseMono = requestSpec.retrieve().bodyToMono(String.class);
            }
            
            return responseMono
                    .flatMap(body -> {
                        // 保存到缓存
                        Duration ttl = Duration.ofSeconds(route.getCacheTtl());
                        redisTemplate.opsForValue().set(cacheKey, body, ttl)
                                .doOnSuccess(v -> log.debug("API响应已缓存 - apiId: {}, ttl: {}s", route.getApiId(), route.getCacheTtl()))
                                .subscribe();
                        
                        // 返回响应
                        response.setStatusCode(HttpStatus.OK);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        response.getHeaders().set("X-Cache-Status", "MISS"); // 标记缓存未命中
                        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                        return response.writeWith(Mono.just(buffer));
                    })
                    .onErrorResume(e -> {
                        log.error("转发请求失败 - uri: {}", actualUri, e);
                        return handleError(response, 502, "后端服务调用失败: " + e.getMessage());
                    });
        } catch (Exception e) {
            log.error("构建后端URI失败 - route: {}", route, e);
            return handleError(response, 500, "路由配置错误: " + e.getMessage());
        }
    }

    /**
     * 写入缓存的响应
     */
    private Mono<Void> writeCachedResponse(ServerHttpResponse response, String cachedBody) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("X-Cache-Status", "HIT"); // 标记缓存命中
        DataBuffer buffer = response.bufferFactory().wrap(cachedBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.OPEN_API_ROUTE_FILTER;
    }
}
