package com.intellihub.gateway.filter;

import com.intellihub.gateway.service.CallLogReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.intellihub.gateway.config.FilterOrderConfig;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 访问日志过滤器
 * <p>
 * 记录访问日志并上报到Governance服务
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogFilter implements GlobalFilter, Ordered {

    private final CallLogReportService callLogReportService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 记录请求信息
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String method = request.getMethod().name();
        String uri = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");

        log.info("[Gateway Access] Start Time: {}, Method: {}, URI: {}, Query: {}, ClientIP: {}, UserAgent: {}",
                startTime, method, uri, query, clientIp, userAgent);

        long start = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long end = System.currentTimeMillis();
            int duration = (int) (end - start);
            int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
            boolean success = statusCode >= 200 && statusCode < 400;

            log.info("[Gateway Access] End Time: {}, URI: {}, Status: {}, Duration: {}ms",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                    uri, statusCode, duration);

            // 上报调用日志到Governance服务
            String tenantId = request.getHeaders().getFirst("X-Tenant-Id");
            String apiId = (String) exchange.getAttributes().get(OpenApiRouteMatchFilter.ATTR_API_ID);
            String appId = request.getHeaders().getFirst("X-App-Id");
            String appKey = request.getHeaders().getFirst("X-App-Key");
            
            // 获取错误信息（从 exchange 属性中获取，由异常处理器设置）
            String errorMessage = (String) exchange.getAttributes().get("errorMessage");
            if (!success && errorMessage == null) {
                errorMessage = "HTTP " + statusCode;
            }
            
            callLogReportService.reportCallLog(
                    tenantId, apiId, uri, method,
                    appId, appKey, clientIp,
                    statusCode, success, duration,
                    errorMessage, userAgent
            );
        }));
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.ACCESS_LOG_FILTER;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "";
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}