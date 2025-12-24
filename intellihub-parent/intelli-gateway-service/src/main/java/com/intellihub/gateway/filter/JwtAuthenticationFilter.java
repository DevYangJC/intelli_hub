package com.intellihub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.gateway.config.JwtConfig;
import com.intellihub.gateway.config.WhiteListConfig;
import com.intellihub.gateway.dto.UserContext;
import com.intellihub.gateway.util.JwtUtil;
import com.intellihub.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 * 本地验证JWT Token，无需调用Auth服务，性能更高
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final WhiteListConfig whiteListConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否启用认证
        if (!jwtConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();

        log.debug("认证过滤器收到请求路径: {}", path);

        // 检查是否在白名单中
        if (whiteListConfig.isWhiteListPath(path)) {
            log.debug("路径 {} 在白名单中，跳过认证", path);
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst("Authorization");

        // 验证Authorization头格式
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求路径 {} 缺少Authorization头或格式错误", path);
            return handleUnauthorized(response, "缺少Authorization头或格式错误");
        }

        // 提取Token
        String token = authHeader.substring(7);
        if (token.isEmpty()) {
            return handleUnauthorized(response, "Token不能为空");
        }

        // 本地验证JWT Token
        try {
            UserContext userContext = jwtUtil.parseToken(token);

            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest.Builder requestBuilder = request.mutate()
                    .header("X-User-Id", safeString(userContext.getUserId()))
                    .header("X-Username", safeString(userContext.getUsername()));

            // 添加租户信息
            if (userContext.getTenantId() != null) {
                requestBuilder.header("X-Tenant-Id", userContext.getTenantId());
            }

            // 添加角色信息
            List<String> roles = userContext.getRoles();
            if (roles != null && !roles.isEmpty()) {
                requestBuilder.header("X-User-Roles", String.join(",", roles));
            }

            ServerHttpRequest modifiedRequest = requestBuilder.build();

            log.info("用户认证成功 - UserId: {}, Username: {}, Path: {}",
                    userContext.getUserId(), userContext.getUsername(), path);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Token已过期 - Path: {}", path);
            return handleUnauthorized(response, "Token已过期，请重新登录");
        } catch (JwtException e) {
            log.warn("Token验证失败 - Path: {}, Error: {}", path, e.getMessage());
            return handleUnauthorized(response, "Token无效");
        } catch (Exception e) {
            log.error("认证过程异常 - Path: {}", path, e);
            return handleUnauthorized(response, "认证失败");
        }
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.JWT_AUTHENTICATION_FILTER;
    }

    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<?> errorResponse = ApiResponse.failed(401, message);

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
     * 安全获取字符串，避免null
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }
}
