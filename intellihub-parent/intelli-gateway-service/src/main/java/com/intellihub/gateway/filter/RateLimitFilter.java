package com.intellihub.gateway.filter;

import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.gateway.config.RateLimitConfig;
import com.intellihub.gateway.service.RateLimitService;
import com.intellihub.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 限流过滤器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private RateLimitConfig rateLimitConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否启用限流
        if (!rateLimitConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String ip = getClientIp(request);

        // 获取限流配置
        RateLimitConfig.Limit limit = getLimitConfig(path);

        // 构建限流Key（IP级别）
        String ipKey = rateLimitService.buildKey("ip", ip);
        String pathKey = rateLimitService.buildKey("path", path);
        String combinedKey = rateLimitService.buildKey("combined", ip + ":" + path);

        // 并行检查不同维度的限流
        return rateLimitService.isAllowed(ipKey, limit.getRequests() * 2, limit.getWindow())  // IP级别限流（更宽松）
                .flatMap(ipAllowed -> {
                    if (Boolean.FALSE.equals(ipAllowed)) {
                        return handleRateLimit(exchange.getResponse(), "IP级别限流触发");
                    }
                    return rateLimitService.isAllowed(pathKey, limit.getRequests() * 10, limit.getWindow());  // Path级别限流（更宽松）
                })
                .flatMap(pathAllowed -> {
                    if (Boolean.FALSE.equals(pathAllowed)) {
                        return handleRateLimit(exchange.getResponse(), "Path级别限流触发");
                    }
                    // 检查IP+Path组合限流
                    return rateLimitService.isAllowed(combinedKey, limit.getRequests(), limit.getWindow());
                })
                .flatMap(combinedAllowed -> {
                    if (Boolean.FALSE.equals(combinedAllowed)) {
                        return handleRateLimit(exchange.getResponse(), "请求过于频繁，请稍后再试");
                    }

                    log.debug("限流检查通过 - IP: {}, Path: {}", ip, path);
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.RATE_LIMIT_FILTER;
    }

    /**
     * 获取限流配置
     */
    private RateLimitConfig.Limit getLimitConfig(String path) {
        // 检查是否有特定路径的配置
        for (String pattern : rateLimitConfig.getLimits().keySet()) {
            if (pathMatcher.match(pattern, path)) {
                return rateLimitConfig.getLimits().get(pattern);
            }
        }
        // 返回默认配置
        return rateLimitConfig.getDefaultLimit();
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null) {
                InetAddress address = remoteAddress.getAddress();
                ip = address != null ? address.getHostAddress() : "127.0.0.1";
            } else {
                ip = "127.0.0.1";
            }
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 处理限流
     */
    private Mono<Void> handleRateLimit(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 添加限流相关的响应头
        response.getHeaders().set("X-RateLimit-Limit", String.valueOf(rateLimitConfig.getDefaultLimit().getRequests()));
        response.getHeaders().set("X-RateLimit-Remaining", "0");
        response.getHeaders().set("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 60));

        ApiResponse<?> errorResponse = ApiResponse.failed(429, message);

        try {
            String result = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写入限流响应失败:", e);
            return Mono.error(e);
        }
    }
}