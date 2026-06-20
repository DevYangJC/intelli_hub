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

    // ==================== 统计相关 ====================

    /**
     * 实时统计前缀
     * 完整Key格式: stats:realtime:{tenantId}:{apiPath}:{hour}:{type}
     * type: total/success/fail
     */
    public static final String STATS_REALTIME_PREFIX = "stats:realtime:";

    /**
     * 统计类型 - 总数
     */
    public static final String STATS_TYPE_TOTAL = "total";

    /**
     * 统计类型 - 成功数
     */
    public static final String STATS_TYPE_SUCCESS = "success";

    /**
     * 统计类型 - 失败数
     */
    public static final String STATS_TYPE_FAIL = "fail";

    /**
     * 统计数据过期时间 (3小时)
     */
    public static final long TTL_STATS = 10800;

    // ==================== 告警相关 ====================

    /**
     * 告警请求记录前缀 (List类型)
     * 完整Key格式: alert:requests:{tenantId}:{hour}
     * 存储每个请求的JSON详情
     */
    public static final String ALERT_REQUESTS_PREFIX = "alert:requests:";

    /**
     * 告警统计汇总前缀 (Hash类型)
     * 完整Key格式: alert:stats:{tenantId}:{hour}
     * 字段: totalCount, successCount, failCount, latencySum
     */
    public static final String ALERT_STATS_PREFIX = "alert:stats:";

    /**
     * 告警数据过期时间 (1小时)
     */
    public static final long TTL_ALERT_DATA = 3600;

    /**
     * QPS 统计前缀 (每分钟独立统计)
     * 完整Key格式: alert:qps:{tenantId}:{minute}
     * minute 格式: yyyyMMddHHmm
     */
    public static final String ALERT_QPS_PREFIX = "alert:qps:";

    /**
     * QPS 数据过期时间 (5分钟)
     */
    public static final long TTL_QPS_DATA = 300;

    /**
     * 构建告警请求记录Key
     */
    public static String buildAlertRequestsKey(String tenantId, String hour) {
        return ALERT_REQUESTS_PREFIX + tenantId + ":" + hour;
    }

    /**
     * 构建告警统计汇总Key
     */
    public static String buildAlertStatsKey(String tenantId, String hour) {
        return ALERT_STATS_PREFIX + tenantId + ":" + hour;
    }

    /**
     * 构建 QPS 统计Key（每分钟独立）
     * @param tenantId 租户ID
     * @param minute 分钟标识，格式 yyyyMMddHHmm
     */
    public static String buildQpsKey(String tenantId, String minute) {
        return ALERT_QPS_PREFIX + tenantId + ":" + minute;
    }

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

    // ==================== 统计Key构建方法 ====================

    /**
     * 构建实时统计Key前缀
     * @param tenantId 租户ID
     * @param apiPath API路径或"global"表示全局
     * @param hour 小时格式 yyyyMMddHH
     * @return Key前缀 (不含type后缀)
     */
    public static String buildStatsKeyPrefix(String tenantId, String apiPath, String hour) {
        return STATS_REALTIME_PREFIX + tenantId + ":" + apiPath + ":" + hour;
    }

    /**
     * 构建实时统计Key - 总数
     */
    public static String buildStatsTotalKey(String tenantId, String apiPath, String hour) {
        return buildStatsKeyPrefix(tenantId, apiPath, hour) + ":" + STATS_TYPE_TOTAL;
    }

    /**
     * 构建实时统计Key - 成功数
     */
    public static String buildStatsSuccessKey(String tenantId, String apiPath, String hour) {
        return buildStatsKeyPrefix(tenantId, apiPath, hour) + ":" + STATS_TYPE_SUCCESS;
    }

    /**
     * 构建实时统计Key - 失败数
     */
    public static String buildStatsFailKey(String tenantId, String apiPath, String hour) {
        return buildStatsKeyPrefix(tenantId, apiPath, hour) + ":" + STATS_TYPE_FAIL;
    }

    /**
     * 构建全局实时统计Key前缀
     */
    public static String buildGlobalStatsKeyPrefix(String tenantId, String hour) {
        return buildStatsKeyPrefix(tenantId, "global", hour);
    }

    private RedisKeyConstants() {
    }
}
