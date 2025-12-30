package com.intellihub.search.sync;

import com.intellihub.dubbo.ApiInfoDTO;
import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.search.constant.ApiStatus;
import com.intellihub.search.constant.SyncLockKey;
import com.intellihub.search.model.doc.ApiDoc;
import com.intellihub.search.service.ApiIndexService;
import com.intellihub.search.util.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * API 数据同步任务
 * <p>
 * 定时从 api-platform-service 拉取数据同步到 ES 索引
 * </p>
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiSyncTask {

    @DubboReference(check = false, timeout = 30000)
    private ApiPlatformDubboService apiPlatformDubboService;

    private final ApiIndexService apiIndexService;
    private final DistributedLock distributedLock;

    /**
     * 上次同步时间
     */
    private final AtomicReference<LocalDateTime> lastSyncTime = new AtomicReference<>(null);

    /**
     * 全量同步（每天凌晨2点执行）
     */
    @Scheduled(cron = "${intellihub.search.sync.full-cron:0 0 2 * * ?}")
    public void fullSync() {
        if (!distributedLock.tryLock(SyncLockKey.API_FULL_SYNC, 600)) {
            log.info("API 全量同步任务已在其他实例执行中，跳过");
            return;
        }

        log.info("开始全量同步 API 数据到 ES...");
        long startTime = System.currentTimeMillis();

        try {
            List<ApiInfoDTO> apiInfos = apiPlatformDubboService.getAllApiInfoForSync(null);
            if (apiInfos == null || apiInfos.isEmpty()) {
                log.info("无 API 数据需要同步");
                return;
            }

            List<ApiDoc> apiDocs = apiInfos.stream()
                    .map(this::convertToApiDoc)
                    .collect(Collectors.toList());

            apiIndexService.bulkIndexApis(apiDocs);

            lastSyncTime.set(LocalDateTime.now());
            log.info("全量同步完成: count={}, cost={}ms",
                    apiDocs.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage(), e);
        } finally {
            distributedLock.unlock(SyncLockKey.API_FULL_SYNC);
        }
    }

    /**
     * 增量同步（每5分钟执行一次）
     */
    @Scheduled(cron = "${intellihub.search.sync.incremental-cron:0 */5 * * * ?}")
    public void incrementalSync() {
        if (!distributedLock.tryLock(SyncLockKey.API_INCREMENTAL_SYNC, 120)) {
            log.debug("API 增量同步任务已在其他实例执行中，跳过");
            return;
        }

        try {
            LocalDateTime syncTime = lastSyncTime.get();
            if (syncTime == null) {
                log.info("首次增量同步，执行全量同步");
                distributedLock.unlock(SyncLockKey.API_INCREMENTAL_SYNC);
                fullSync();
                return;
            }

            log.debug("开始增量同步 API 数据, lastSyncTime={}", syncTime);
            long startTime = System.currentTimeMillis();

            List<ApiInfoDTO> apiInfos = apiPlatformDubboService.getApiInfoUpdatedAfter(null, syncTime);
            if (apiInfos == null || apiInfos.isEmpty()) {
                log.debug("无增量数据需要同步");
                lastSyncTime.set(LocalDateTime.now());
                return;
            }

            List<ApiDoc> apiDocs = apiInfos.stream()
                    .map(this::convertToApiDoc)
                    .collect(Collectors.toList());

            apiIndexService.bulkIndexApis(apiDocs);

            lastSyncTime.set(LocalDateTime.now());
            log.info("增量同步完成: count={}, cost={}ms",
                    apiDocs.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("增量同步失败: {}", e.getMessage(), e);
        } finally {
            distributedLock.unlock(SyncLockKey.API_INCREMENTAL_SYNC);
        }
    }

    /**
     * 手动触发全量同步
     */
    public void triggerFullSync() {
        fullSync();
    }

    /**
     * 手动触发增量同步
     */
    public void triggerIncrementalSync() {
        incrementalSync();
    }

    /**
     * DTO 转换为索引文档
     */
    private ApiDoc convertToApiDoc(ApiInfoDTO dto) {
        ApiDoc doc = new ApiDoc();
        doc.setId(dto.getId());
        doc.setName(dto.getName());
        doc.setCode(dto.getCode());
        doc.setPath(dto.getPath());
        doc.setMethod(dto.getMethod());
        doc.setProtocol(dto.getProtocol());
        doc.setDescription(dto.getDescription());
        doc.setGroupId(dto.getGroupId());
        doc.setGroupName(dto.getGroupName());
        doc.setStatus(dto.getStatus());
        doc.setStatusName(getStatusName(dto.getStatus()));
        doc.setAuthType(dto.getAuthType());
        doc.setVersion(dto.getVersion());
        doc.setTenantId(dto.getTenantId());
        doc.setCreatedBy(dto.getCreatedBy());
        doc.setCreatorName(dto.getCreatorName());
        doc.setPublishedAt(dto.getPublishedAt());
        doc.setCreatedAt(dto.getCreatedAt());
        doc.setUpdatedAt(dto.getUpdatedAt());
        return doc;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        return ApiStatus.getNameByCode(status);
    }
}
