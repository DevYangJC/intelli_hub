package com.intellihub.common.constants;

/**
 * Redis Key常量统一管理
 * <p>
 * 所有模块的Redis Key都在此统一定义，避免Key冲突和管理混乱
 * </p>
 * <pre>
 * Key命名规范：
 * 1. 使用冒号(:)分隔层级，如 gateway:route:xxx
 * 2. 业务前缀 + 功能模块 + 具体标识
 * 3. 所有Key都要在此文件中定义并注明用途
 * </pre>
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
        // 禁止实例化
    }

    // ==================== 前缀定义 ====================

    /**
     * 网关服务前缀
     */
    public static final String GATEWAY_PREFIX = "gateway:";

    /**
     * 认证服务前缀
     */
    public static final String AUTH_PREFIX = "auth:";

    /**
     * API平台服务前缀
     */
    public static final String API_PREFIX = "api:";

    /**
     * 应用中心服务前缀
     */
    public static final String APP_PREFIX = "app:";

    // ==================== 网关模块 - 路由缓存 ====================

    /**
     * API路由缓存前缀
     * <p>
     * 用途：缓存已发布的API路由配置，避免每次请求都查询数据库
     * 格式：gateway:route:{path}:{method}
     * 示例：gateway:route:/open/user/info:GET
     * 过期时间：10分钟
     * </p>
     */
    public static final String GATEWAY_ROUTE_PREFIX = GATEWAY_PREFIX + "route:";

    /**
     * 构建路由缓存Key
     */
    public static String buildRouteKey(String path, String method) {
        return GATEWAY_ROUTE_PREFIX + path + ":" + method.toUpperCase();
    }

    // ==================== 网关模块 - 限流 ====================

    /**
     * 限流计数前缀
     * <p>
     * 用途：记录限流计数，用于滑动窗口/固定窗口限流算法
     * 格式：gateway:ratelimit:{type}:{value}:count
     * 示例：gateway:ratelimit:ip:192.168.1.1:count
     * 过期时间：根据限流窗口动态设置
     * </p>
     */
    public static final String GATEWAY_RATE_LIMIT_PREFIX = GATEWAY_PREFIX + "ratelimit:";

    /**
     * 构建IP限流Key
     */
    public static String buildRateLimitIpKey(String ip) {
        return GATEWAY_RATE_LIMIT_PREFIX + "ip:" + ip;
    }

    /**
     * 构建路径限流Key
     */
    public static String buildRateLimitPathKey(String path) {
        return GATEWAY_RATE_LIMIT_PREFIX + "path:" + path;
    }

    /**
     * 构建组合限流Key（IP+Path）
     */
    public static String buildRateLimitCombinedKey(String ip, String path) {
        return GATEWAY_RATE_LIMIT_PREFIX + "combined:" + ip + ":" + path;
    }

    // ==================== 网关模块 - AppKey认证 ====================

    /**
     * AppKey信息缓存前缀
     * <p>
     * 用途：缓存应用的AppKey信息，包含AppSecret、状态等
     * 格式：gateway:appkey:info:{appKey}
     * 示例：gateway:appkey:info:ak_xxxxxxxxxxxx
     * 过期时间：10分钟
     * </p>
     */
    public static final String GATEWAY_APPKEY_INFO_PREFIX = GATEWAY_PREFIX + "appkey:info:";

    /**
     * 构建AppKey信息缓存Key
     */
    public static String buildAppKeyInfoKey(String appKey) {
        return GATEWAY_APPKEY_INFO_PREFIX + appKey;
    }

    /**
     * AppKey Nonce防重放前缀
     * <p>
     * 用途：防止重放攻击，记录已使用的Nonce
     * 格式：gateway:appkey:nonce:{appKey}:{nonce}
     * 示例：gateway:appkey:nonce:ak_xxx:abc123
     * 过期时间：5分钟（与时间戳容差一致）
     * </p>
     */
    public static final String GATEWAY_APPKEY_NONCE_PREFIX = GATEWAY_PREFIX + "appkey:nonce:";

    /**
     * 构建Nonce防重放Key
     */
    public static String buildNonceKey(String appKey, String nonce) {
        return GATEWAY_APPKEY_NONCE_PREFIX + appKey + ":" + nonce;
    }

    /**
     * API订阅关系缓存前缀（按路径）
     * <p>
     * 用途：缓存应用与API的订阅关系
     * 格式：gateway:appkey:sub:{appId}:{path}
     * 示例：gateway:appkey:sub:app123:/open/user/info
     * 过期时间：5分钟
     * </p>
     */
    public static final String GATEWAY_SUBSCRIPTION_PATH_PREFIX = GATEWAY_PREFIX + "appkey:sub:";

    /**
     * 构建订阅关系缓存Key（按路径）
     */
    public static String buildSubscriptionPathKey(String appId, String path) {
        return GATEWAY_SUBSCRIPTION_PATH_PREFIX + appId + ":" + path;
    }

    /**
     * API订阅关系缓存前缀（按API ID）
     * <p>
     * 用途：缓存应用与API的订阅关系（按API ID精确匹配）
     * 格式：gateway:appkey:sub:api:{appId}:{apiId}
     * 示例：gateway:appkey:sub:api:app123:api456
     * 过期时间：5分钟
     * </p>
     */
    public static final String GATEWAY_SUBSCRIPTION_API_PREFIX = GATEWAY_PREFIX + "appkey:sub:api:";

    /**
     * 构建订阅关系缓存Key（按API ID）
     */
    public static String buildSubscriptionApiKey(String appId, String apiId) {
        return GATEWAY_SUBSCRIPTION_API_PREFIX + appId + ":" + apiId;
    }

    // ==================== 网关模块 - 租户验证 ====================

    /**
     * 租户有效性缓存前缀
     * <p>
     * 用途：缓存租户是否有效的验证结果
     * 格式：gateway:tenant:valid:{tenantId}
     * 示例：gateway:tenant:valid:tenant001
     * 过期时间：5分钟
     * </p>
     */
    public static final String GATEWAY_TENANT_VALID_PREFIX = GATEWAY_PREFIX + "tenant:valid:";

    /**
     * 构建租户有效性缓存Key
     */
    public static String buildTenantValidKey(String tenantId) {
        return GATEWAY_TENANT_VALID_PREFIX + tenantId;
    }

    // ==================== 认证模块 ====================

    /**
     * 用户Token缓存前缀
     * <p>
     * 用途：缓存用户的JWT Token，用于Token黑名单或主动失效
     * 格式：auth:token:{userId}:{tokenId}
     * 过期时间：与Token有效期一致
     * </p>
     */
    public static final String AUTH_TOKEN_PREFIX = AUTH_PREFIX + "token:";

    /**
     * 验证码缓存前缀
     * <p>
     * 用途：存储图形验证码或短信验证码
     * 格式：auth:captcha:{captchaId}
     * 过期时间：5分钟
     * </p>
     */
    public static final String AUTH_CAPTCHA_PREFIX = AUTH_PREFIX + "captcha:";

    /**
     * 构建验证码Key
     */
    public static String buildCaptchaKey(String captchaId) {
        return AUTH_CAPTCHA_PREFIX + captchaId;
    }

    /**
     * 登录失败次数前缀
     * <p>
     * 用途：记录用户登录失败次数，用于防暴力破解
     * 格式：auth:login:fail:{username}
     * 过期时间：30分钟
     * </p>
     */
    public static final String AUTH_LOGIN_FAIL_PREFIX = AUTH_PREFIX + "login:fail:";

    /**
     * 构建登录失败次数Key
     */
    public static String buildLoginFailKey(String username) {
        return AUTH_LOGIN_FAIL_PREFIX + username;
    }

    // ==================== API平台模块 ====================

    /**
     * API信息缓存前缀
     * <p>
     * 用途：缓存API的基本信息
     * 格式：api:info:{apiId}
     * 过期时间：10分钟
     * </p>
     */
    public static final String API_INFO_PREFIX = API_PREFIX + "info:";

    /**
     * 构建API信息缓存Key
     */
    public static String buildApiInfoKey(String apiId) {
        return API_INFO_PREFIX + apiId;
    }

    // ==================== 应用中心模块 ====================

    /**
     * 应用信息缓存前缀
     * <p>
     * 用途：缓存应用的基本信息
     * 格式：app:info:{appId}
     * 过期时间：10分钟
     * </p>
     */
    public static final String APP_INFO_PREFIX = APP_PREFIX + "info:";

    /**
     * 构建应用信息缓存Key
     */
    public static String buildAppInfoKey(String appId) {
        return APP_INFO_PREFIX + appId;
    }

    // ==================== 发布订阅频道 ====================

    /**
     * API路由变更事件频道
     * <p>
     * 用途：API发布/下线时通知网关刷新路由缓存
     * </p>
     */
    public static final String CHANNEL_API_ROUTE_CHANGE = "channel:api:route:change";

    /**
     * 应用状态变更事件频道
     * <p>
     * 用途：应用启用/禁用时通知网关刷新AppKey缓存
     * </p>
     */
    public static final String CHANNEL_APP_STATUS_CHANGE = "channel:app:status:change";

    // ==================== 缓存过期时间常量（秒） ====================

    /**
     * 路由缓存过期时间：10分钟
     */
    public static final long TTL_ROUTE_CACHE = 600;

    /**
     * AppKey信息缓存过期时间：10分钟
     */
    public static final long TTL_APPKEY_INFO = 600;

    /**
     * Nonce防重放过期时间：5分钟
     */
    public static final long TTL_NONCE = 300;

    /**
     * 订阅关系缓存过期时间：5分钟
     */
    public static final long TTL_SUBSCRIPTION = 300;

    /**
     * 租户有效性缓存过期时间：5分钟
     */
    public static final long TTL_TENANT_VALID = 300;

    /**
     * 验证码过期时间：5分钟
     */
    public static final long TTL_CAPTCHA = 300;

    /**
     * 登录失败记录过期时间：30分钟
     */
    public static final long TTL_LOGIN_FAIL = 1800;
}
