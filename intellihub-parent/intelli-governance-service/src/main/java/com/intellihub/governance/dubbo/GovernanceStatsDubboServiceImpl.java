package com.intellihub.governance.dubbo;

import com.intellihub.dubbo.ApiStatsDTO;
import com.intellihub.dubbo.GovernanceStatsDubboService;
import com.intellihub.governance.service.ApiStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * 治理服务统计Dubbo接口实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@DubboService(version = "1.0.0", timeout = 5000)
@RequiredArgsConstructor
public class GovernanceStatsDubboServiceImpl implements GovernanceStatsDubboService {

    private final ApiStatsService apiStatsService;

    @Override
    public ApiStatsDTO getApiStats(String apiId) {
        log.debug("[Dubbo] 获取API统计 - apiId: {}", apiId);

        if (apiId == null || apiId.isEmpty()) {
            log.warn("[Dubbo] API ID为空");
            return null;
        }

        try {
            // 从Redis获取统计数据
            ApiStatsDTO stats = apiStatsService.getApiStats(apiId);
            
            if (stats == null) {
                log.debug("[Dubbo] 未找到API统计数据 - apiId: {}", apiId);
                return null;
            }

            // 转换为Dubbo DTO
            ApiStatsDTO dto = new ApiStatsDTO();
            dto.setApiId(stats.getApiId());
            dto.setTodayCalls(stats.getTodayCalls());
            dto.setTotalCalls(stats.getTotalCalls());
            dto.setSuccessCalls(stats.getSuccessCalls());
            dto.setAvgResponseTime(stats.getAvgResponseTime());
            dto.setSuccessRate(stats.getSuccessRate());

            log.debug("[Dubbo] API统计查询成功 - apiId: {}, todayCalls: {}, totalCalls: {}", 
                    apiId, dto.getTodayCalls(), dto.getTotalCalls());

            return dto;
        } catch (Exception e) {
            log.error("[Dubbo] 获取API统计失败 - apiId: {}", apiId, e);
            return null;
        }
    }
}

