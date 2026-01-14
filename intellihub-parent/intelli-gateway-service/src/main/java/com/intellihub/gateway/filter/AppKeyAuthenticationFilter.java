package com.intellihub.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.gateway.config.FilterOrderConfig;
import com.intellihub.dubbo.ApiRouteDTO;
import com.intellihub.util.SignatureUtil;
import com.intellihub.gateway.config.AppKeyConfig;
import com.intellihub.gateway.service.AppKeyService;
import com.intellihub.gateway.vo.AppKeyInfo;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import com.intellihub.gateway.util.ReactiveRedisUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * AppKey签名认证过滤器
 * <p>
 * 验证API调用的AppKey签名，实现：
 * 1. AppKey有效性校验
 * 2. HMAC-SHA256签名验证
 * 3. 防重放攻击（Nonce + Timestamp）
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppKeyAuthenticationFilter implements GlobalFilter, Ordered {

    private final AppKeyConfig appKeyConfig;
    private final AppKeyService appKeyService;
    private final ReactiveRedisUtil redisUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String HEADER_APP_KEY = "X-App-Key";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String HEADER_SIGNATURE = "X-Signature";

    private static final String AUTH_TYPE_NONE = "none";
    private static final String AUTH_TYPE_SIGNATURE = "signature";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否启用AppKey认证
        if (!appKeyConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 使用原始请求路径（签名验证必须使用客户端发送的原始路径）
        // 从 OriginalPathSaveFilter 保存的属性中获取
        String originalPath = exchange.getAttribute(OriginalPathSaveFilter.ATTR_ORIGINAL_PATH);
        final String path = (originalPath != null) ? originalPath : request.getURI().getPath();
        final String method = request.getMethod().name();

        // 优先从API路由配置中获取认证类型（由OpenApiRouteMatchFilter设置）
        ApiRouteDTO route = (ApiRouteDTO) exchange.getAttributes().get(OpenApiRouteMatchFilter.ATTR_API_ROUTE);
        
        if (route != null) {
            // 使用API配置的认证类型
            String authType = route.getAuthType();
            if (AUTH_TYPE_NONE.equalsIgnoreCase(authType)) {
                log.debug("跨过认证 - API认证类型为none - path: {}", path);
                return chain.filter(exchange);
            }
            // 如果authType为signature或其他值，继续执行AppKey认证
        } else {
            // 没有API路由配置，回退到路径配置检查
            if (!appKeyConfig.requiresAppKeyAuth(path)) {
                return chain.filter(exchange);
            }
        }

        log.debug("AppKey认证过滤器处理请求: {} {}", method, path);

        // 获取认证相关请求头
        String appKey = request.getHeaders().getFirst(HEADER_APP_KEY);
        String timestamp = request.getHeaders().getFirst(HEADER_TIMESTAMP);
        String nonce = request.getHeaders().getFirst(HEADER_NONCE);
        String signature = request.getHeaders().getFirst(HEADER_SIGNATURE);

        // 验证必要参数
        if (!StringUtils.hasText(appKey)) {
            return handleUnauthorized(response, "缺少X-App-Key请求头。请先在控制台创建应用并获取AppKey和AppSecret，然后在请求头中添加认证信息");
        }
        if (!StringUtils.hasText(timestamp)) {
            return handleUnauthorized(response, "缺少X-Timestamp请求头。请在请求头中添加时间戳（Unix秒级时间戳）");
        }
        if (!StringUtils.hasText(nonce)) {
            return handleUnauthorized(response, "缺少X-Nonce请求头。请在请求头中添加随机字符串（用于防止重放攻击）");
        }
        if (!StringUtils.hasText(signature)) {
            return handleUnauthorized(response, "缺少X-Signature请求头。请使用AppSecret对请求参数进行签名");
        }

        // 验证时间戳（防止重放攻击）
        long requestTime;
        try {
            requestTime = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return handleUnauthorized(response, "时间戳格式错误");
        }

        long currentTime = System.currentTimeMillis() / 1000;
        if (Math.abs(currentTime - requestTime) > appKeyConfig.getTimestampTolerance()) {
            log.warn("请求时间戳超出允许范围 - AppKey: {}, Timestamp: {}, Current: {}", 
                    appKey, requestTime, currentTime);
            return handleUnauthorized(response, "请求已过期，请检查系统时间");
        }

        // 验证Nonce（防止重放攻击）
        String nonceKey = RedisKeyConstants.buildNonceKey(appKey, nonce);
        
        return redisUtil.setIfAbsent(nonceKey, "1", RedisKeyConstants.TTL_NONCE)
                .flatMap(success -> {
                    if (Boolean.FALSE.equals(success)) {
                        log.warn("Nonce重复使用 - AppKey: {}, Nonce: {}", appKey, nonce);
                        return handleUnauthorized(response, "请求已处理，请勿重复提交");
                    }
                    
                    // 获取应用信息并验证签名
                    return appKeyService.getAppKeyInfo(appKey)
                            .flatMap(appKeyInfo -> {
                                if (appKeyInfo == null) {
                                    log.warn("AppKey不存在 - AppKey: {}", appKey);
                                    return handleUnauthorized(response, "无效的AppKey。请检查AppKey是否正确，或在控制台重新创建应用");
                                }
                                
                                // 检查应用状态
                                if (!"active".equals(appKeyInfo.getStatus())) {
                                    log.warn("应用已禁用 - AppKey: {}, Status: {}", appKey, appKeyInfo.getStatus());
                                    return handleUnauthorized(response, "应用已禁用。请在控制台启用应用后重试");
                                }
                                
                                // 检查应用是否过期
                                if (appKeyInfo.getExpireTime() != null && 
                                    appKeyInfo.getExpireTime() < System.currentTimeMillis()) {
                                    log.warn("应用已过期 - AppKey: {}", appKey);
                                    return handleUnauthorized(response, "应用凭证已过期。请在控制台续期或重新创建应用");
                                }
                                
                                // 检查IP白名单
                                if (!checkIpWhitelist(request, appKeyInfo)) {
                                    String clientIp = getClientIp(request);
                                    log.warn("IP不在白名单中 - AppKey: {}, IP: {}, Whitelist: {}", 
                                            appKey, clientIp, appKeyInfo.getIpWhitelist());
                                    return handleForbidden(response, "您的IP不在白名单中，无权访问此应用");
                                }
                                
                                // 检查配额
                                return checkQuota(appKeyInfo)
                                    .flatMap(quotaAllowed -> {
                                        if (!quotaAllowed) {
                                            log.warn("配额已耗尽 - AppId: {}, Used: {}, Limit: {}", 
                                                    appKeyInfo.getAppId(), 
                                                    appKeyInfo.getQuotaUsed(), 
                                                    appKeyInfo.getQuotaLimit());
                                            return handleForbidden(response, 
                                                    "今日调用配额已用完，请明天再试或联系管理员提升配额");
                                        }
                                        
                                        // 获取API ID
                                        String apiId = (String) exchange.getAttributes().get(OpenApiRouteMatchFilter.ATTR_API_ID);
                                        
                                        // 验证签名
                                        boolean signatureValid = SignatureUtil.verifySignature(
                                                signature, method, path, timestamp, nonce, appKeyInfo.getAppSecret());
                                        
                                        if (!signatureValid) {
                                            log.warn("签名验证失败 - AppKey: {}, Path: {}", appKey, path);
                                            return handleUnauthorized(response, "签名验证失败。请检查AppSecret是否正确，并确保签名算法符合规范");
                                        }
                                        
                                        // 检查订阅关系 - 优先使用API ID（由OpenApiRouteMatchFilter提供）
                                        Mono<Boolean> subscriptionCheck;
                                        
                                        if (apiId != null) {
                                            // 使用API ID检查订阅（更精确）
                                            subscriptionCheck = appKeyService.checkSubscriptionByApiId(appKeyInfo.getAppId(), apiId);
                                        } else {
                                            // 降级使用路径检查
                                            subscriptionCheck = appKeyService.checkSubscription(appKeyInfo.getAppId(), path);
                                        }
                                        
                                        return subscriptionCheck
                                                .flatMap(hasSubscription -> {
                                                    if (!hasSubscription) {
                                                        log.warn("应用未订阅该API - AppKey: {}, ApiId: {}, Path: {}", appKey, apiId, path);
                                                        return handleForbidden(response, "应用未订阅该API，请先在应用中心订阅");
                                                    }
                                                    
                                                    // 将应用信息添加到请求头，传递给下游服务
                                                    ServerHttpRequest modifiedRequest = request.mutate()
                                                            .header("X-App-Id", appKeyInfo.getAppId())
                                                            .header("X-App-Key", appKey)
                                                            .header("X-Tenant-Id", safeString(appKeyInfo.getTenantId()))
                                                            .build();
                                                    
                                                    log.info("AppKey认证成功 - AppKey: {}, AppId: {}, ApiId: {}, Path: {}", 
                                                            appKey, appKeyInfo.getAppId(), apiId, path);
                                                    
                                                    // 请求成功后，异步增加配额计数
                                                    return chain.filter(exchange.mutate().request(modifiedRequest).build())
                                                            .then(Mono.fromRunnable(() -> incrementQuotaAsync(appKeyInfo.getAppId())));
                                                });
                                    });
                            })
                            .switchIfEmpty(handleUnauthorized(response, "无效的AppKey"));
                });
    }

    @Override
    public int getOrder() {
        return FilterOrderConfig.APP_KEY_AUTHENTICATION_FILTER;
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
     * 处理禁止访问请求
     */
    private Mono<Void> handleForbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<?> errorResponse = ApiResponse.failed(403, message);

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
    
    /**
     * 检查IP白名单
     */
    private boolean checkIpWhitelist(ServerHttpRequest request, AppKeyInfo appKeyInfo) {
        String ipWhitelist = appKeyInfo.getIpWhitelist();
        
        // 如果没有配置白名单，则不限制
        if (ipWhitelist == null || ipWhitelist.trim().isEmpty()) {
            return true;
        }
        
        String clientIp = getClientIp(request);
        if (clientIp == null) {
            return false;
        }
        
        // 解析白名单（逗号分隔）
        String[] allowedIps = ipWhitelist.split(",");
        for (String allowedIp : allowedIps) {
            allowedIp = allowedIp.trim();
            
            // 精确匹配
            if (clientIp.equals(allowedIp)) {
                return true;
            }
            
            // 支持CIDR格式（如：192.168.1.0/24）
            if (allowedIp.contains("/") && matchCIDR(clientIp, allowedIp)) {
                return true;
            }
            
            // 支持通配符（如：192.168.1.*）
            if (allowedIp.contains("*") && matchWildcard(clientIp, allowedIp)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = null;
        String[] headerNames = {
            "X-Forwarded-For", "X-Real-IP", 
            "Proxy-Client-IP", "WL-Proxy-Client-IP"
        };
        
        for (String headerName : headerNames) {
            String headerValue = request.getHeaders().getFirst(headerName);
            if (headerValue != null && !headerValue.isEmpty()) {
                // X-Forwarded-For可能有多个IP，取第一个
                ip = headerValue.split(",")[0].trim();
                break;
            }
        }
        
        if (ip == null) {
            java.net.InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null) {
                ip = remoteAddress.getAddress().getHostAddress();
            }
        }
        
        return ip;
    }
    
    /**
     * 通配符匹配（如：192.168.1.* 匹配 192.168.1.100）
     */
    private boolean matchWildcard(String ip, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return ip.matches(regex);
    }
    
    /**
     * CIDR匹配（如：192.168.1.0/24）
     */
    private boolean matchCIDR(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            long ipLong = ipToLong(ip);
            long networkLong = ipToLong(networkIp);
            long mask = -1L << (32 - prefixLength);
            
            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.error("CIDR格式错误: {}", cidr, e);
            return false;
        }
    }
    
    /**
     * IP转换为长整型
     */
    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24)
             + (Long.parseLong(octets[1]) << 16)
             + (Long.parseLong(octets[2]) << 8)
             + Long.parseLong(octets[3]);
    }
    
    /**
     * 检查配额
     */
    private Mono<Boolean> checkQuota(AppKeyInfo appKeyInfo) {
        Long quotaLimit = appKeyInfo.getQuotaLimit();
        
        // 如果没有配置配额限制，则不限制
        if (quotaLimit == null || quotaLimit <= 0) {
            return Mono.just(true);
        }
        
        // 从Redis获取当前使用量
        String quotaKey = "app:quota:" + appKeyInfo.getAppId();
        
        return redisUtil.get(quotaKey)
            .map(used -> {
                long usedCount = Long.parseLong(used);
                return usedCount < quotaLimit;
            })
            .defaultIfEmpty(true); // Redis中没有数据，说明还未使用
    }
    
    /**
     * 异步增加配额计数
     */
    private void incrementQuotaAsync(String appId) {
        String quotaKey = "app:quota:" + appId;
        
        redisUtil.increment(quotaKey)
            .subscribe(
                count -> log.debug("配额计数增加成功 - AppId: {}, Count: {}", appId, count),
                error -> log.error("配额计数增加失败 - AppId: {}", appId, error)
            );
    }
}
