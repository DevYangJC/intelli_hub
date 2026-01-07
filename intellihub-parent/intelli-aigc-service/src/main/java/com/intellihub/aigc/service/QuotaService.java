package com.intellihub.aigc.service;

import com.intellihub.aigc.entity.AigcRequestLog;

/**
 * 配额管理服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface QuotaService {

    /**
     * 检查配额是否充足
     *
     * @param tenantId 租户ID
     * @param tokensNeeded 需要的Token数
     * @return 是否充足
     */
    boolean checkQuota(String tenantId, int tokensNeeded);

    /**
     * 扣减配额
     *
     * @param tenantId 租户ID
     * @param tokensUsed 使用的Token数
     */
    void deductQuota(String tenantId, int tokensUsed);

    /**
     * 记录请求日志
     *
     * @param log 请求日志
     */
    void recordRequestLog(AigcRequestLog log);

    /**
     * 获取租户配额使用情况
     *
     * @param tenantId 租户ID
     * @return 配额信息
     */
    java.util.Map<String, Object> getQuotaUsage(String tenantId);
}
