package com.intellihub.governance.dubbo;

import com.intellihub.dubbo.ApiStatsDTO;
import com.intellihub.dubbo.GovernanceDubboService;
import com.intellihub.governance.service.ApiStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 治理服务Dubbo接口实现
 * <p>
 * 提供API统计数据查询功能
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService(group = "intellihub", timeout = 3000)
@RequiredArgsConstructor
public class GovernanceDubboServiceImpl implements GovernanceDubboService {
    
    private final ApiStatsService apiStatsService;
    
    @Override
    public ApiStatsDTO getApiStats(String apiId) {
        log.debug("Dubbo调用 - 获取API统计数据: apiId={}", apiId);
        try {
            return apiStatsService.getApiStats(apiId);
        } catch (Exception e) {
            log.error("获取API统计数据失败 - apiId: {}", apiId, e);
            return null;
        }
    }
    
    @Override
    public Map<String, ApiStatsDTO> batchGetApiStats(List<String> apiIds) {
        log.debug("Dubbo调用 - 批量获取API统计数据: count={}", apiIds != null ? apiIds.size() : 0);
        try {
            return apiStatsService.batchGetApiStats(apiIds);
        } catch (Exception e) {
            log.error("批量获取API统计数据失败 - apiIds: {}", apiIds, e);
            return Collections.emptyMap();
        }
    }
}

