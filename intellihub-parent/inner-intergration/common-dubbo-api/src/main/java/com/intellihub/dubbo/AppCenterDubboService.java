package com.intellihub.dubbo;

/**
 * 应用中心Dubbo服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AppCenterDubboService {

    /**
     * 根据AppKey获取应用信息
     *
     * @param appKey 应用Key
     * @return 应用信息
     */
    AppKeyInfoDTO getAppKeyInfo(String appKey);

    /**
     * 检查应用是否订阅了指定路径的API
     *
     * @param appId 应用ID
     * @param path  API路径
     * @return 是否订阅
     */
    boolean checkSubscriptionByPath(String appId, String path);

    /**
     * 验证AppKey和Secret
     *
     * @param appKey    应用Key
     * @param appSecret 应用Secret
     * @return 是否有效
     */
    boolean validateCredentials(String appKey, String appSecret);

    /**
     * 检查应用是否订阅了指定API
     *
     * @param appId 应用ID
     * @param apiId API ID
     * @return 是否订阅
     */
    boolean checkSubscriptionByApiId(String appId, String apiId);

    /**
     * 批量更新App配额使用
     * <p>
     * 由Governance服务调用，同步调用统计数据到App表
     * </p>
     *
     * @param callCounts App调用次数统计列表
     * @return 更新成功的记录数
     */
    int batchUpdateAppQuotaUsed(java.util.List<AppCallCountDTO> callCounts);
}
