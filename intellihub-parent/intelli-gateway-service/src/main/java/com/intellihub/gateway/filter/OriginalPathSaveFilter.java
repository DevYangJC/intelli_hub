package com.intellihub.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 原始路径保存过滤器
 * <p>
 * 在所有路由处理之前保存原始请求路径，供签名验证使用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class OriginalPathSaveFilter implements GlobalFilter, Ordered {

    public static final String ATTR_ORIGINAL_PATH = "ORIGINAL_REQUEST_PATH";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 保存原始请求路径
        String originalPath = exchange.getRequest().getURI().getPath();
        exchange.getAttributes().put(ATTR_ORIGINAL_PATH, originalPath);
        
        log.debug("保存原始请求路径: {}", originalPath);
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 最高优先级，在所有过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
