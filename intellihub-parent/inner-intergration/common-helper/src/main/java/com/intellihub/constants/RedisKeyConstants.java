package com.intellihub.constants;

/**
 * Redis Key常量
 *
 * @author intellihub
 * @since 1.0.0
 */
public class RedisKeyConstants {

    // ==================== Key前缀 ====================

    /**
     * 用户Token缓存前缀
     */
    public static final String USER_TOKEN_PREFIX = "intellihub:user:token:";

    /**
     * 用户信息缓存前缀
     */
    public static final String USER_INFO_PREFIX = "intellihub:user:info:";

    /**
     * API路由缓存Key
     */
    public static final String API_ROUTES_KEY = "intellihub:api:routes";

    /**
     * 应用信息缓存前缀
     */
    public static final String APP_INFO_PREFIX = "intellihub:app:info:";

    /**
     * 应用密钥缓存前缀
     */
    public static final String APP_KEY_PREFIX = "intellihub:app:key:";

    /**
     * 网关AppKey信息缓存前缀
     */
    public static final String GATEWAY_APPKEY_INFO_PREFIX = "intellihub:gateway:appkey:";

    /**
     * 网关限流前缀
     */
    public static final String GATEWAY_RATE_LIMIT_PREFIX = "intellihub:gateway:ratelimit:";

    /**
     * 订阅关系缓存前缀
     */
    public static final String SUBSCRIPTION_PREFIX = "intellihub:subscription:";

    /**
     * 租户有效性缓存前缀
     */
    public static final String TENANT_VALID_PREFIX = "intellihub:tenant:valid:";

    /**
     * Nonce防重放前缀
     */
    public static final String NONCE_PREFIX = "intellihub:nonce:";

    // ==================== 消息频道 ====================

    /**
     * API路由变更消息频道
     */
    public static final String CHANNEL_API_ROUTE_CHANGE = "intellihub:channel:api:route:change";

    /**
     * 应用状态变更消息频道
     */
    public static final String CHANNEL_APP_STATUS_CHANGE = "intellihub:channel:app:status:change";

    // ==================== TTL (秒) ====================

    /**
     * Token缓存时间 (2小时)
     */
    public static final long TTL_TOKEN = 7200;

    /**
     * 用户信息缓存时间 (30分钟)
     */
    public static final long TTL_USER_INFO = 1800;

    /**
     * AppKey信息缓存时间 (10分钟)
     */
    public static final long TTL_APPKEY_INFO = 600;

    /**
     * 订阅关系缓存时间 (5分钟)
     */
    public static final long TTL_SUBSCRIPTION = 300;

    /**
     * 租户有效性缓存时间 (5分钟)
     */
    public static final long TTL_TENANT_VALID = 300;

    /**
     * Nonce防重放有效时间 (5分钟)
     */
    public static final long TTL_NONCE = 300;

    /**
     * API路由缓存时间 (10分钟)
     */
    public static final long TTL_API_ROUTE = 600;

    // ==================== Key构建方法 ====================

    /**
     * 构建用户Token Key
     */
    public static String buildUserTokenKey(String userId) {
        return USER_TOKEN_PREFIX + userId;
    }

    /**
     * 构建用户信息Key
     */
    public static String buildUserInfoKey(String userId) {
        return USER_INFO_PREFIX + userId;
    }

    /**
     * 构建AppKey信息Key
     */
    public static String buildAppKeyInfoKey(String appKey) {
        return GATEWAY_APPKEY_INFO_PREFIX + appKey;
    }

    /**
     * 构建订阅路径Key
     */
    public static String buildSubscriptionPathKey(String appId, String path) {
        return SUBSCRIPTION_PREFIX + "path:" + appId + ":" + path.hashCode();
    }

    /**
     * 构建订阅API Key
     */
    public static String buildSubscriptionApiKey(String appId, String apiId) {
        return SUBSCRIPTION_PREFIX + "api:" + appId + ":" + apiId;
    }

    /**
     * 构建租户有效性Key
     */
    public static String buildTenantValidKey(String tenantId) {
        return TENANT_VALID_PREFIX + tenantId;
    }

    /**
     * 构建Nonce Key
     */
    public static String buildNonceKey(String appKey, String nonce) {
        return NONCE_PREFIX + appKey + ":" + nonce;
    }

    /**
     * 构建限流Key
     */
    public static String buildRateLimitKey(String type, String value) {
        return GATEWAY_RATE_LIMIT_PREFIX + type + ":" + value;
    }

    private RedisKeyConstants() {
    }
}
