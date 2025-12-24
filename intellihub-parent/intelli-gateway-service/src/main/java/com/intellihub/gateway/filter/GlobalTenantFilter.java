package com.intellihub.gateway.filter;

import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.dubbo.TenantDubboService;
import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.gateway.config.WhiteListConfig;
import com.intellihub.gateway.util.ReactiveRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局租户过滤器
 * <p>
 * 验证租户有效性，通过Dubbo调用认证服务
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class GlobalTenantFilter implements GlobalFilter, Ordered {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String DEFAULT_TENANT_ID = "default";

    @Autowired
    private WhiteListConfig whiteListConfig;

    @Autowired
    private ReactiveRedisUtil redisUtil;

    @DubboReference(version = "1.0.0", group = "intellihub", check = false, timeout = 3000)
    private TenantDubboService tenantDubboService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径不需要租户验证
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        String tenantId = request.getHeaders().getFirst(TENANT_ID_HEADER);

        // 如果没有租户ID，使用默认租户
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT_ID;
            log.debug("未找到租户ID，使用默认租户: {}", DEFAULT_TENANT_ID);
        }

        final String finalTenantId = tenantId;

        // 验证租户是否存在且有效（使用缓存）
        return isValidTenant(tenantId)
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.warn("无效的租户ID: {}", finalTenantId);
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.FORBIDDEN);
                        return response.setComplete();
                    }

                    // 在请求头中添加租户信息
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header(TENANT_ID_HEADER, finalTenantId)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                });
    }

    /**
     * 验证租户是否有效（带缓存）
     */
    private Mono<Boolean> isValidTenant(String tenantId) {
        // 默认租户始终有效
        if (DEFAULT_TENANT_ID.equals(tenantId)) {
            return Mono.just(true);
        }

        String cacheKey = RedisKeyConstants.buildTenantValidKey(tenantId);

        return redisUtil.get(cacheKey)
                .map("1"::equals)
                .switchIfEmpty(
                        Mono.fromCallable(() -> {
                            try {
                                boolean isValid = tenantDubboService.isValidTenant(tenantId);
                                log.debug("Dubbo验证租户 - tenantId: {}, isValid: {}", tenantId, isValid);
                                return isValid;
                            } catch (Exception e) {
                                log.error("Dubbo验证租户失败 - tenantId: {}", tenantId, e);
                                return true;
                            }
                        }).flatMap(isValid -> {
                            String value = isValid ? "1" : "0";
                            return redisUtil.set(cacheKey, value, RedisKeyConstants.TTL_TENANT_VALID)
                                    .thenReturn(isValid);
                        })
                );
    }

    /**
     * 判断是否是白名单路径
     */
    private boolean isWhiteListPath(String path) {
        return whiteListConfig.isWhiteListPath(path);
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.GLOBAL_TENANT_FILTER;
    }
}