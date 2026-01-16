package com.intellihub.dubbo;

/**
 * 治理服务统计Dubbo接口
 * <p>
 * 提供API统计数据查询服务
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface GovernanceStatsDubboService {

    /**
     * 获取API实时统计数据
     *
     * @param apiId API ID
     * @return API统计数据
     */
    ApiStatsDTO getApiStats(String apiId);
}

