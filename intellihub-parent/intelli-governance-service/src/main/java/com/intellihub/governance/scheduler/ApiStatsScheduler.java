package com.intellihub.governance.scheduler;

import com.intellihub.dubbo.ApiCallCountDTO;
import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.dubbo.ApiStatsDTO;
import com.intellihub.governance.service.ApiStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API统计定时任务
 * <p>
 * 1. 每5分钟同步Redis统计数据到MySQL
 * 2. 每天0点重置今日统计
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiStatsScheduler {

    private final ApiStatsService apiStatsService;

    @DubboReference(check = false, timeout = 10000)
    private ApiPlatformDubboService apiPlatformDubboService;

    /**
     *      * 每5分钟同步统计数据到MySQL
     *      * <p>
     *      * 表示每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncStatsToDatabase() {
        log.info("[API统计同步] 开始同步统计数据到MySQL");

        try {
            // 1. 获取所有API ID
            Set<String> apiIds = apiStatsService.getAllApiIds();
            if (apiIds.isEmpty()) {
                log.info("[API统计同步] 没有需要同步的API统计数据");
                return;
            }

            log.info("[API统计同步] 发现 {} 个API需要同步", apiIds.size());

            // 2. 批量获取统计数据
            List<ApiCallCountDTO> callCounts = new ArrayList<>();
            for (String apiId : apiIds) {
                ApiStatsDTO stats = apiStatsService.getApiStats(apiId);
                if (stats != null) {
                    ApiCallCountDTO dto = new ApiCallCountDTO();
                    dto.setApiId(stats.getApiId());
                    dto.setTodayCalls(stats.getTodayCalls());
                    dto.setTotalCalls(stats.getTotalCalls());
                    callCounts.add(dto);
                }
            }

            if (callCounts.isEmpty()) {
                log.info("[API统计同步] 没有有效的统计数据");
                return;
            }

            // 3. 调用Dubbo接口批量更新
            int successCount = apiPlatformDubboService.batchUpdateApiCallCounts(callCounts);
            log.info("[API统计同步] 同步完成 - 成功: {}/{}", successCount, callCounts.size());

        } catch (Exception e) {
            log.error("[API统计同步] 同步失败", e);
        }
    }

    /**
     * 每天0点重置今日统计
     * <p>
     * Cron表达式：0 0 0 * * ? 表示每天0点执行
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyStats() {
        log.info("[API统计重置] 开始重置今日统计");

        try {
            apiStatsService.resetDailyStats();
            log.info("[API统计重置] 今日统计重置完成");
        } catch (Exception e) {
            log.error("[API统计重置] 重置失败", e);
        }
    }

    /**
     * 手动触发同步（用于测试）
     */
    public void manualSync() {
        log.info("[API统计同步] 手动触发同步");
        syncStatsToDatabase();
    }

    /**
     * 手动触发重置（用于测试）
     */
    public void manualReset() {
        log.info("[API统计重置] 手动触发重置");
        resetDailyStats();
    }
}

