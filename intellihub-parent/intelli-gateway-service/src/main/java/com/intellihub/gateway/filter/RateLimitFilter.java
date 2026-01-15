package com.intellihub.gateway.filter;

import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.gateway.config.RateLimitConfig;
import com.intellihub.gateway.service.RateLimitService;
import com.intellihub.ApiResponse;
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

        // 使用组合维度（IP+Path）限流 - 最严格且最有效
        String rateLimitKey = rateLimitService.buildKey("combined", ip + ":" + path);

        // 检查限流
        return rateLimitService.checkLimit(rateLimitKey, limit.getRequests(), limit.getWindow())
                .flatMap(result -> {
                    if (!result.isAllowed()) {
                        return handleRateLimit(exchange.getResponse(), 
                                rateLimitConfig.getErrorMessage(), result);
                    }

                    // 检查通过，增加计数
                    return rateLimitService.incrementCounter(rateLimitKey, limit.getWindow())
                            .then(Mono.defer(() -> {
                                // 添加限流信息到响应头
                                ServerHttpResponse response = exchange.getResponse();
                                response.getHeaders().set("X-RateLimit-Limit", 
                                        String.valueOf(result.getLimit()));
                                response.getHeaders().set("X-RateLimit-Remaining", 
                                        String.valueOf(result.getRemaining() - 1)); // 减1因为已经计数
                                response.getHeaders().set("X-RateLimit-Reset", 
                                        String.valueOf(result.getResetTime()));

                                log.debug("限流检查通过 - IP: {}, Path: {}, 剩余: {}", 
                                        ip, path, result.getRemaining() - 1);
                    return chain.filter(exchange);
                            }));
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
     * 获取客户端真实IP（增强版）
     * <p>
     * 改进：
     * 1. 支持更多代理头
     * 2. IP格式校验
     * 3. 内网IP过滤
     * 4. 从X-Forwarded-For取最后一个非内网IP
     * </p>
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = null;
        
        // 1. 尝试从各种Header获取
        String[] headerNames = {
            "X-Forwarded-For", 
            "X-Real-IP", 
            "Proxy-Client-IP", 
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String headerName : headerNames) {
            String headerValue = request.getHeaders().getFirst(headerName);
            if (isValidIp(headerValue)) {
                ip = headerValue;
                break;
            }
        }
        
        // 2. 如果Header中没有，从RemoteAddress获取
        if (!isValidIp(ip)) {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                ip = remoteAddress.getAddress().getHostAddress();
            }
        }
        
        // 3. X-Forwarded-For可能有多个IP，取最后一个可信代理的前一个
        if (ip != null && ip.contains(",")) {
            String[] ips = ip.split(",");
            // 从后向前找第一个非内网IP
            for (int i = ips.length - 1; i >= 0; i--) {
                String candidateIp = ips[i].trim();
                if (isValidIp(candidateIp) && !isInternalIp(candidateIp)) {
                    ip = candidateIp;
                    break;
                }
            }
            // 如果都是内网IP，取第一个
            if (ip.contains(",")) {
                ip = ips[0].trim();
        }
        }
        
        // 4. 最终校验
        if (!isValidIp(ip)) {
            ip = "unknown";
        }
        
        return ip;
    }

    /**
     * 校验IP格式是否有效
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        
        // 简单的IP格式校验（支持IPv4和IPv6）
        // IPv4: xxx.xxx.xxx.xxx
        if (ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            // 进一步校验每段不超过255
            String[] parts = ip.split("\\.");
            for (String part : parts) {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        }
        
        // IPv6: 包含冒号
        if (ip.contains(":")) {
            // 简单判断，不做严格校验
            return true;
        }
        
        return false;
    }

    /**
     * 判断是否是内网IP
     */
    private boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4内网地址段
        return ip.startsWith("10.") || 
               ip.startsWith("192.168.") || 
               ip.startsWith("172.16.") || ip.startsWith("172.17.") || 
               ip.startsWith("172.18.") || ip.startsWith("172.19.") || 
               ip.startsWith("172.20.") || ip.startsWith("172.21.") || 
               ip.startsWith("172.22.") || ip.startsWith("172.23.") || 
               ip.startsWith("172.24.") || ip.startsWith("172.25.") || 
               ip.startsWith("172.26.") || ip.startsWith("172.27.") || 
               ip.startsWith("172.28.") || ip.startsWith("172.29.") || 
               ip.startsWith("172.30.") || ip.startsWith("172.31.") || 
               ip.equals("127.0.0.1") || 
               ip.equals("localhost") ||
               ip.equals("0:0:0:0:0:0:0:1") ||
               ip.equals("::1");
    }

    /**
     * 处理限流
     */
    private Mono<Void> handleRateLimit(ServerHttpResponse response, String message, 
                                       RateLimitService.RateLimitResult result) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 添加限流相关的响应头（使用实际数据）
        response.getHeaders().set("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.getHeaders().set("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.getHeaders().set("X-RateLimit-Reset", String.valueOf(result.getResetTime()));
        response.getHeaders().set("Retry-After", String.valueOf(result.getResetTime() - System.currentTimeMillis() / 1000));

        ApiResponse<?> errorResponse = ApiResponse.failed(429, message);

        try {
            String resultJson = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(resultJson.getBytes(StandardCharsets.UTF_8));
            
            log.warn("限流触发 - Message: {}, Limit: {}, Remaining: {}, Reset: {}", 
                    message, result.getLimit(), result.getRemaining(), result.getResetTime());
            
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写入限流响应失败:", e);
            return Mono.error(e);
        }
    }
}