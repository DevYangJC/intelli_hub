package com.intellihub.dubbo;

import java.util.List;
import java.util.Map;

/**
 * 治理服务Dubbo接口
 * <p>
 * 提供API统计数据查询功能
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface GovernanceDubboService {
    
    /**
     * 获取单个API的统计数据
     *
     * @param apiId API ID
     * @return API统计数据，如果不存在返回null
     */
    ApiStatsDTO getApiStats(String apiId);
    
    /**
     * 批量获取API统计数据
     *
     * @param apiIds API ID列表
     * @return API统计数据Map，key为apiId
     */
    Map<String, ApiStatsDTO> batchGetApiStats(List<String> apiIds);
}

