package com.intellihub.aigc.interceptor;

import com.intellihub.aigc.ratelimit.RedisRateLimiter;
import com.intellihub.context.UserContextHolder;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 限流拦截器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiter rateLimiter;

    // 租户限流：每分钟100次
    private static final int TENANT_LIMIT_PER_MINUTE = 100;
    private static final int TENANT_WINDOW_SECONDS = 60;

    // 用户限流：每分钟20次
    private static final int USER_LIMIT_PER_MINUTE = 20;
    private static final int USER_WINDOW_SECONDS = 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // 只对AIGC接口限流
        String uri = request.getRequestURI();
        if (!uri.startsWith("/v1/aigc/")) {
            return true;
        }

        // 健康检查和文档接口不限流
        if (uri.contains("/actuator/") || uri.contains("/swagger-") || uri.contains("/v3/api-docs")) {
            return true;
        }

        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();

        // 租户级限流
        if (tenantId != null && !tenantId.isEmpty()) {
            boolean allowed = rateLimiter.tryAcquireByTenant(
                    tenantId, 
                    TENANT_LIMIT_PER_MINUTE, 
                    TENANT_WINDOW_SECONDS
            );
            
            if (!allowed) {
                throw new BusinessException("租户请求频率过高，请稍后再试");
            }
        }

        // 用户级限流
        if (userId != null && !userId.isEmpty()) {
            boolean allowed = rateLimiter.tryAcquireByUser(
                    userId, 
                    USER_LIMIT_PER_MINUTE, 
                    USER_WINDOW_SECONDS
            );
            
            if (!allowed) {
                throw new BusinessException("请求频率过高，请稍后再试");
            }
        }

        return true;
    }
}
