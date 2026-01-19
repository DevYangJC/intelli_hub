package com.intellihub.gateway.config;

/**
 * 网关过滤器执行顺序配置
 * <p>
 * 统一管理所有GlobalFilter的执行顺序，数值越小越先执行
 * </p>
 * <pre>
 * 执行顺序：
 * 1. CacheBodyFilter          (-200) - 缓存请求Body，供后续过滤器使用
 * 2. AccessLogFilter          (-100) - 访问日志记录（请求开始）
 * 3. OpenApiRouteMatchFilter  (-50)  - 开放API路由匹配，设置API上下文
 * 4. RateLimitFilter          (100)  - 限流控制（在认证之前，防止资源浪费）
 * 5. JwtAuthenticationFilter  (1000) - JWT Token认证（管理后台请求）
 * 6. GlobalTenantFilter       (1050) - 租户上下文处理（在JWT认证之后）
 * 7. AppKeyAuthenticationFilter (1100) - AppKey签名认证（开放API请求）
 * 8. OpenApiRouteFilter       (1200) - 开放API动态路由转发
 * </pre>
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class FilterOrderConfig {

    private FilterOrderConfig() {
        // 禁止实例化
    }

    /**
     * CacheBodyFilter - 缓存请求Body
     * <p>最先执行，确保Body可以被后续过滤器读取</p>
     */
    public static final int CACHE_BODY_FILTER = -200;

    /**
     * AccessLogFilter - 访问日志记录
     * <p>记录请求开始时间和基本信息</p>
     */
    public static final int ACCESS_LOG_FILTER = -100;

    /**
     * OpenApiRouteMatchFilter - 开放API路由匹配
     * <p>匹配API配置，将路由信息存入exchange属性供后续使用</p>
     */
    public static final int OPEN_API_ROUTE_MATCH_FILTER = -50;

    /**
     * RateLimitFilter - 限流控制
     * <p>
     * 在认证之前执行限流，原因：
     * 1. 防止攻击者用无效Token消耗认证资源
     * 2. 尽早拦截恶意请求，保护后端服务
     * </p>
     */
    public static final int RATE_LIMIT_FILTER = 100;

    /**
     * JwtAuthenticationFilter - JWT认证
     * <p>验证管理后台请求的JWT Token</p>
     */
    public static final int JWT_AUTHENTICATION_FILTER = 1000;

    /**
     * GlobalTenantFilter - 租户上下文处理
     * <p>
     * 在JWT认证之后执行，可以使用JWT解析后的租户信息
     * 从请求头提取租户信息，设置租户上下文
     * </p>
     */
    public static final int GLOBAL_TENANT_FILTER = 1050;

    /**
     * AppKeyAuthenticationFilter - AppKey签名认证
     * <p>验证开放API请求的AppKey签名</p>
     */
    public static final int APP_KEY_AUTHENTICATION_FILTER = 1100;

    /**
     * OpenApiRouteFilter - 开放API路由转发
     * <p>根据API配置将请求转发到后端服务</p>
     */
    public static final int OPEN_API_ROUTE_FILTER = 1200;
}
