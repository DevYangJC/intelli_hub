package com.intellihub.gateway.service;

import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.dubbo.ApiRouteDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开放API路由服务
 * <p>
 * 负责加载和缓存API路由配置，支持动态刷新
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
public class OpenApiRouteService {

    @DubboReference(check = false, timeout = 5000)
    private ApiPlatformDubboService apiPlatformDubboService;

    private final ReactiveStringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 本地路由缓存（path:method -> ApiRouteDTO）
     * 注意：path可能包含路径参数，如 /open/user/{id}
     * 使用Caffeine缓存，支持自动过期和容量限制
     */
    private final Cache<String, ApiRouteDTO> localRouteCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10000)
            .build();

    /**
     * API ID索引缓存（apiId -> cacheKey）
     */
    private final Map<String, String> apiIdIndex = new ConcurrentHashMap<>();

    public OpenApiRouteService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 启动时加载所有已发布的路由配置
     */
    @PostConstruct
    public void init() {
        log.info("开始加载API路由配置...");
        try {
            refreshAllRoutes();
        } catch (Exception e) {
            log.warn("启动时加载路由配置失败，将在首次请求时懒加载: {}", e.getMessage());
        }
    }

    /**
     * 刷新所有路由配置
     */
    public void refreshAllRoutes() {
        try {
            List<ApiRouteDTO> routes = apiPlatformDubboService.getAllPublishedRoutes();
            localRouteCache.invalidateAll();
            apiIdIndex.clear();

            for (ApiRouteDTO route : routes) {
                String cacheKey = buildCacheKey(route.getPath(), route.getMethod());
                localRouteCache.put(cacheKey, route);
                apiIdIndex.put(route.getApiId(), cacheKey);
            }

            log.info("路由配置刷新完成，共加载 {} 条路由", routes.size());
        } catch (Exception e) {
            log.error("刷新路由配置失败", e);
            throw e;
        }
    }

    /**
     * 匹配路由配置（支持路径参数）
     * <p>
     * 例如：/open/user/123 可以匹配 /open/user/{id}
     * </p>
     *
     * @param requestPath  实际请求路径
     * @param method       请求方法
     * @return 匹配的路由配置
     */
    public Mono<ApiRouteDTO> matchRoute(String requestPath, String method) {
        // 1. 先尝试精确匹配
        String exactKey = buildCacheKey(requestPath, method);
        ApiRouteDTO exactMatch = localRouteCache.getIfPresent(exactKey);
        if (exactMatch != null) {
            log.debug("精确匹配路由 - path: {}, method: {}", requestPath, method);
            return Mono.just(exactMatch);
        }

        // 2. 尝试通配符匹配（支持路径参数）
        for (Map.Entry<String, ApiRouteDTO> entry : localRouteCache.asMap().entrySet()) {
            ApiRouteDTO route = entry.getValue();
            // 方法必须匹配
            if (!method.equalsIgnoreCase(route.getMethod()) && !"ALL".equalsIgnoreCase(route.getMethod())) {
                continue;
            }
            // 使用AntPathMatcher匹配路径（支持 {id} 等路径参数）
            if (pathMatcher.match(route.getPath(), requestPath)) {
                log.debug("通配符匹配路由 - requestPath: {}, pattern: {}", requestPath, route.getPath());
                return Mono.just(route);
            }
        }

        // 3. 本地缓存未命中，尝试从Dubbo服务获取
        return Mono.fromCallable(() -> {
            log.debug("从API Platform匹配路由 - path: {}, method: {}", requestPath, method);
            ApiRouteDTO route = apiPlatformDubboService.matchRouteByPath(requestPath, method);
            if (route != null) {
                // 加入本地缓存
                String cacheKey = buildCacheKey(route.getPath(), route.getMethod());
                localRouteCache.put(cacheKey, route);
                apiIdIndex.put(route.getApiId(), cacheKey);
            }
            return route;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> {
            log.error("匹配路由配置失败 - path: {}, method: {}", requestPath, method, e);
            return Mono.empty();
        });
    }

    /**
     * 获取路由配置
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return 路由配置
     */
    public Mono<ApiRouteDTO> getRoute(String path, String method) {
        String cacheKey = buildCacheKey(path, method);

        // 1. 先从本地缓存获取
        ApiRouteDTO cachedRoute = localRouteCache.get(cacheKey);
        if (cachedRoute != null) {
            log.debug("从本地缓存获取路由配置 - path: {}, method: {}", path, method);
            return Mono.just(cachedRoute);
        }

        // 2. 本地缓存未命中，从Dubbo服务获取
        return Mono.fromCallable(() -> {
            log.debug("从API Platform获取路由配置 - path: {}, method: {}", path, method);
            ApiRouteDTO route = apiPlatformDubboService.getRouteByPath(path, method);
            if (route != null) {
                // 加入本地缓存
                localRouteCache.put(cacheKey, route);
            }
            return route;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> {
            log.error("获取路由配置失败 - path: {}, method: {}", path, method, e);
            return Mono.empty();
        });
    }

    /**
     * 刷新单个API的路由配置
     *
     * @param apiId API ID
     */
    public Mono<Void> refreshRoute(String apiId) {
        return Mono.fromRunnable(() -> {
            try {
                ApiRouteDTO route = apiPlatformDubboService.getRouteByApiId(apiId);
                if (route != null) {
                    String cacheKey = buildCacheKey(route.getPath(), route.getMethod());
                    localRouteCache.put(cacheKey, route);
                    log.info("刷新路由配置成功 - apiId: {}, path: {}", apiId, route.getPath());
                }
            } catch (Exception e) {
                log.error("刷新路由配置失败 - apiId: {}", apiId, e);
            }
        });
    }

    /**
     * 移除路由配置
     *
     * @param path   请求路径
     * @param method 请求方法
     */
    public void removeRoute(String path, String method) {
        String cacheKey = buildCacheKey(path, method);
        localRouteCache.invalidate(cacheKey);
        log.info("移除路由配置 - path: {}, method: {}", path, method);
    }

    /**
     * 根据API ID移除路由配置
     *
     * @param apiId API ID
     */
    public void removeRouteByApiId(String apiId) {
        String cacheKey = apiIdIndex.remove(apiId);
        if (cacheKey != null) {
            ApiRouteDTO removed = localRouteCache.getIfPresent(cacheKey);
            localRouteCache.invalidate(cacheKey);
            if (removed != null) {
                log.info("移除路由配置 - apiId: {}, path: {}", apiId, removed.getPath());
            }
        } else {
            log.debug("未找到待移除的路由 - apiId: {}", apiId);
        }
    }

    /**
     * 检查路径是否有对应的路由配置
     */
    public boolean hasRoute(String path, String method) {
        String cacheKey = buildCacheKey(path, method);
        return localRouteCache.asMap().containsKey(cacheKey);
    }

    /**
     * 获取缓存的路由数量
     */
    public long getRouteCacheSize() {
        return localRouteCache.estimatedSize();
    }

    private String buildCacheKey(String path, String method) {
        return path + ":" + method.toUpperCase();
    }
}
