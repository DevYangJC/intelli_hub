package com.intellihub.governance.job;

import com.intellihub.dubbo.ApiCallCountDTO;
import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.dubbo.AppCallCountDTO;
import com.intellihub.dubbo.AppCenterDubboService;
import com.intellihub.governance.mapper.ApiCallLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用次数同步任务
 * <p>
 * 从 api_call_log 统计调用次数，通过 Dubbo 接口回写到 api_info 和 app_info
 * </p>
 * <p>
 * 设计说明：
 * 1. 使用 MyBatis-Plus 进行数据查询
 * 2. 跨微服务更新使用 Dubbo 接口
 * 3. 复杂 SQL 放在 XML 文件中
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
public class StatsCallCountSyncJob {

    @Autowired
    private ApiCallLogMapper callLogMapper;

    /**
     * API平台服务 Dubbo 接口
     * <p>
     * 用于更新 api_info 表的调用次数
     * </p>
     */
    @DubboReference(check = false, timeout = 10000)
    private ApiPlatformDubboService apiPlatformDubboService;

    /**
     * 应用中心服务 Dubbo 接口
     * <p>
     * 用于更新 app_info 表的配额使用
     * </p>
     */
    @DubboReference(check = false, timeout = 10000)
    private AppCenterDubboService appCenterDubboService;

    /**
     * 每5分钟同步一次调用次数
     * <p>
     * 定时任务入口，依次同步 API 调用次数和 App 配额使用
     * </p>
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncCallCounts() {
        log.info("[调用次数同步] ========== 开始同步 ==========");
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 同步 API 调用次数
            syncApiCallCounts();
            
            // 2. 同步 App 配额使用
            syncAppCallCounts();
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[调用次数同步] ========== 同步完成，耗时 {} ms ==========", costTime);
        } catch (Exception e) {
            log.error("[调用次数同步] 同步过程发生异常", e);
        }
    }

    /**
     * 同步 API 调用次数
     * <p>
     * 步骤：
     * 1. 从 api_call_log 统计今日各 API 的调用次数
     * 2. 从 api_call_log 统计各 API 的历史总调用次数
     * 3. 通过 Dubbo 接口批量更新 api_info 表
     * </p>
     */
    private void syncApiCallCounts() {
        log.info("[调用次数同步] 开始同步 API 调用次数...");
        
        // 计算今日时间范围
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        log.debug("[调用次数同步] 今日时间范围: {} - {}", todayStart, todayEnd);

        // 1. 使用 MyBatis-Plus XML 查询今日各 API 的调用次数
        List<Map<String, Object>> todayStats = callLogMapper.countTodayCallsByApiId(todayStart, todayEnd);
        log.info("[调用次数同步] 今日有调用的 API 数量: {}", todayStats.size());

        // 2. 使用 MyBatis-Plus XML 查询各 API 的历史总调用次数
        List<Map<String, Object>> totalStats = callLogMapper.countTotalCallsByApiId();
        
        // 构建总调用次数 Map，便于快速查找
        Map<String, Long> totalMap = new HashMap<>();
        for (Map<String, Object> row : totalStats) {
            String apiId = (String) row.get("apiId");
            Long count = ((Number) row.get("callCount")).longValue();
            totalMap.put(apiId, count);
        }
        log.debug("[调用次数同步] 总调用次数统计完成，共 {} 个 API", totalMap.size());

        // 3. 构建 DTO 列表，用于 Dubbo 调用
        List<ApiCallCountDTO> callCounts = new ArrayList<>();
        for (Map<String, Object> row : todayStats) {
            String apiId = (String) row.get("apiId");
            Long todayCalls = ((Number) row.get("callCount")).longValue();
            Long totalCalls = totalMap.getOrDefault(apiId, todayCalls);
            
            ApiCallCountDTO dto = new ApiCallCountDTO(apiId, todayCalls, totalCalls);
            callCounts.add(dto);
            
            log.debug("[调用次数同步] API统计: apiId={}, todayCalls={}, totalCalls={}", 
                    apiId, todayCalls, totalCalls);
        }

        // 4. 通过 Dubbo 接口批量更新 api_info 表
        if (!callCounts.isEmpty()) {
            try {
                int updated = apiPlatformDubboService.batchUpdateApiCallCounts(callCounts);
                log.info("[调用次数同步] API 调用次数更新完成: 成功 {} / {} 条", updated, callCounts.size());
            } catch (Exception e) {
                log.error("[调用次数同步] 调用 ApiPlatformDubboService 失败", e);
            }
        } else {
            log.info("[调用次数同步] 今日无 API 调用，跳过更新");
        }
    }

    /**
     * 同步 App 配额使用
     * <p>
     * 步骤：
     * 1. 从 api_call_log 统计各 App 的总调用次数
     * 2. 通过 Dubbo 接口批量更新 app_info 表的 quota_used 字段
     * </p>
     */
    private void syncAppCallCounts() {
        log.info("[调用次数同步] 开始同步 App 配额使用...");

        // 1. 使用 MyBatis-Plus XML 查询各 App 的总调用次数
        List<Map<String, Object>> stats = callLogMapper.countCallsByAppId();
        log.info("[调用次数同步] 有调用记录的 App 数量: {}", stats.size());

        // 2. 构建 DTO 列表，用于 Dubbo 调用
        List<AppCallCountDTO> callCounts = new ArrayList<>();
        for (Map<String, Object> row : stats) {
            String appId = (String) row.get("appId");
            Long quotaUsed = ((Number) row.get("callCount")).longValue();
            
            AppCallCountDTO dto = new AppCallCountDTO(appId, quotaUsed);
            callCounts.add(dto);
            
            log.debug("[调用次数同步] App统计: appId={}, quotaUsed={}", appId, quotaUsed);
        }

        // 3. 通过 Dubbo 接口批量更新 app_info 表
        if (!callCounts.isEmpty()) {
            try {
                int updated = appCenterDubboService.batchUpdateAppQuotaUsed(callCounts);
                log.info("[调用次数同步] App 配额更新完成: 成功 {} / {} 条", updated, callCounts.size());
            } catch (Exception e) {
                log.error("[调用次数同步] 调用 AppCenterDubboService 失败", e);
            }
        } else {
            log.info("[调用次数同步] 无 App 调用记录，跳过更新");
        }
    }

    /**
     * 启动时立即同步一次
     * <p>
     * 服务启动后30秒执行一次同步，确保数据及时更新
     * </p>
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void syncOnStartup() {
        log.info("[调用次数同步] 服务启动，执行首次同步...");
        syncCallCounts();
    }
}
