package com.intellihub.gateway.service;

import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.dubbo.AppCenterDubboService;
import com.intellihub.dubbo.AppKeyInfoDTO;
import com.intellihub.gateway.util.ReactiveRedisUtil;
import com.intellihub.gateway.vo.AppKeyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * AppKey服务
 * <p>
 * 负责获取和缓存AppKey信息
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
public class AppKeyService {

    private final ReactiveRedisUtil redisUtil;

    @DubboReference(check = false, timeout = 5000)
    private AppCenterDubboService appCenterDubboService;

    public AppKeyService(ReactiveRedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 获取AppKey信息
     * <p>
     * 优先从Redis缓存获取，缓存不存在则从app-center服务获取
     * </p>
     *
     * @param appKey AppKey
     * @return AppKey信息
     */
    public Mono<AppKeyInfo> getAppKeyInfo(String appKey) {
        String cacheKey = RedisKeyConstants.buildAppKeyInfoKey(appKey);
        
        return redisUtil.getObject(cacheKey, AppKeyInfo.class)
                .doOnNext(info -> log.debug("从缓存获取AppKey信息 - AppKey: {}", appKey))
                .switchIfEmpty(fetchFromAppCenter(appKey, cacheKey));
    }

    /**
     * 从app-center服务获取AppKey信息（通过Dubbo）
     */
    private Mono<AppKeyInfo> fetchFromAppCenter(String appKey, String cacheKey) {
        log.debug("从app-center服务获取AppKey信息（Dubbo） - AppKey: {}", appKey);
        
        return Mono.fromCallable(() -> {
            try {
                AppKeyInfoDTO dto = appCenterDubboService.getAppKeyInfo(appKey);
                if (dto == null) {
                    log.warn("AppKey不存在 - AppKey: {}", appKey);
                    return null;
                }
                return convertFromDTO(dto);
            } catch (Exception e) {
                log.error("Dubbo调用app-center服务失败 - AppKey: {}", appKey, e);
                return null;
            }
        }).flatMap(appKeyInfo -> {
            if (appKeyInfo == null) {
                return Mono.empty();
            }
            // 缓存到Redis
            return redisUtil.setObject(cacheKey, appKeyInfo, RedisKeyConstants.TTL_APPKEY_INFO)
                    .thenReturn(appKeyInfo);
        });
    }

    /**
     * 从DTO转换为AppKeyInfo
     */
    private AppKeyInfo convertFromDTO(AppKeyInfoDTO dto) {
        AppKeyInfo info = new AppKeyInfo();
        info.setAppId(dto.getAppId());
        info.setTenantId(dto.getTenantId());
        info.setAppName(dto.getAppName());
        info.setAppKey(dto.getAppKey());
        info.setAppSecret(dto.getAppSecret());
        info.setStatus(dto.getStatus());
        info.setExpireTime(dto.getExpireTime());
        
        // ✅ 日志追踪：记录从DTO转换后的租户ID
        log.info("[AppKey认证] DTO转AppKeyInfo - AppKey: {}, TenantId: {}", dto.getAppKey(), dto.getTenantId());
        
        // 设置新增字段
        info.setIpWhitelist(dto.getIpWhitelist());
        info.setQuotaLimit(dto.getQuotaLimit());
        info.setQuotaUsed(dto.getQuotaUsed());
        info.setQuotaResetTime(dto.getQuotaResetTime());
        
        return info;
    }

    /**
     * 清除AppKey缓存
     */
    public Mono<Boolean> invalidateCache(String appKey) {
        String cacheKey = RedisKeyConstants.buildAppKeyInfoKey(appKey);
        return redisUtil.delete(cacheKey);
    }

    /**
     * 清除指定AppKey的缓存（用于Pub/Sub通知）
     *
     * @param appKey AppKey
     * @return 操作结果
     */
    public Mono<Void> clearCache(String appKey) {
        String cacheKey = RedisKeyConstants.buildAppKeyInfoKey(appKey);
        return redisUtil.delete(cacheKey)
                .doOnSuccess(result -> log.info("清除AppKey缓存 - appKey: {}, result: {}", appKey, result))
                .then();
    }

    /**
     * 清除所有AppKey缓存（用于Pub/Sub通知）
     *
     * @return 操作结果
     */
    public Mono<Void> clearAllCache() {
        String pattern = RedisKeyConstants.GATEWAY_APPKEY_INFO_PREFIX + "*";
        return redisUtil.deleteByPattern(pattern)
                .doOnSuccess(count -> log.info("清除所有AppKey缓存，删除数量: {}", count))
                .then();
    }

    /**
     * 检查应用是否订阅了指定路径的API（通过Dubbo）
     *
     * @param appId 应用ID
     * @param path  请求路径
     * @return 是否有订阅权限
     */
    public Mono<Boolean> checkSubscription(String appId, String path) {
        String cacheKey = RedisKeyConstants.buildSubscriptionPathKey(appId, path);
        
        return redisUtil.get(cacheKey)
                .map("1"::equals)
                .switchIfEmpty(
                    Mono.fromCallable(() -> {
                        try {
                            return appCenterDubboService.checkSubscriptionByPath(appId, path);
                        } catch (Exception e) {
                            log.error("Dubbo检查订阅关系失败 - AppId: {}, Path: {}", appId, path, e);
                            return false;
                        }
                    }).flatMap(hasSubscription -> {
                        String value = hasSubscription ? "1" : "0";
                        return redisUtil.set(cacheKey, value, RedisKeyConstants.TTL_SUBSCRIPTION)
                                .thenReturn(hasSubscription);
                    })
                );
    }

    /**
     * 检查应用是否订阅了指定API（通过API ID）
     *
     * @param appId 应用ID
     * @param apiId API ID
     * @return 是否有订阅权限
     */
    public Mono<Boolean> checkSubscriptionByApiId(String appId, String apiId) {
        String cacheKey = RedisKeyConstants.buildSubscriptionApiKey(appId, apiId);
        
        return redisUtil.get(cacheKey)
                .map("1"::equals)
                .switchIfEmpty(
                    Mono.fromCallable(() -> {
                        try {
                            return appCenterDubboService.checkSubscriptionByApiId(appId, apiId);
                        } catch (Exception e) {
                            log.error("Dubbo检查订阅关系失败 - AppId: {}, ApiId: {}", appId, apiId, e);
                            return false;
                        }
                    }).flatMap(hasSubscription -> {
                        String value = hasSubscription ? "1" : "0";
                        return redisUtil.set(cacheKey, value, RedisKeyConstants.TTL_SUBSCRIPTION)
                                .thenReturn(hasSubscription);
                    })
                );
    }
}
