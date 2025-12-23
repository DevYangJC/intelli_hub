package com.intellihub.common.dubbo;

/**
 * 应用中心Dubbo服务接口
 * <p>
 * 提供AppKey验证、订阅关系检查等功能
 * 供其他服务（如API Platform）调用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AppCenterDubboService {

    /**
     * 根据AppKey获取应用信息
     *
     * @param appKey AppKey
     * @return 应用信息，不存在返回null
     */
    AppKeyInfoDTO getAppKeyInfo(String appKey);

    /**
     * 检查应用是否订阅了指定路径的API
     *
     * @param appId 应用ID
     * @param path  API路径
     * @return 是否已订阅
     */
    boolean checkSubscriptionByPath(String appId, String path);

    /**
     * 验证AppKey和AppSecret
     *
     * @param appKey    AppKey
     * @param appSecret AppSecret
     * @return 是否验证通过
     */
    boolean validateCredentials(String appKey, String appSecret);

    /**
     * 检查应用是否订阅了指定API
     *
     * @param appId 应用ID
     * @param apiId API ID
     * @return 是否已订阅
     */
    boolean checkSubscriptionByApiId(String appId, String apiId);
}
