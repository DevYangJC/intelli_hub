package com.intellihub.search.sync;

import com.intellihub.dubbo.AppCenterDubboService;
import com.intellihub.dubbo.AppInfoDTO;
import com.intellihub.search.constant.AppStatus;
import com.intellihub.search.constant.AppType;
import com.intellihub.search.model.doc.AppDoc;
import com.intellihub.search.service.AppIndexService;
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
 * 应用数据同步任务
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppSyncTask {

    @DubboReference(check = false, timeout = 30000)
    private AppCenterDubboService appCenterDubboService;

    private final AppIndexService appIndexService;

    private final AtomicReference<LocalDateTime> lastSyncTime = new AtomicReference<>(null);

    @Scheduled(cron = "${intellihub.search.sync.app-full-cron:0 0 2 * * ?}")
    public void fullSync() {
        log.info("开始全量同步应用数据到 ES...");
        long startTime = System.currentTimeMillis();

        try {
            List<AppInfoDTO> appInfos = appCenterDubboService.getAllAppInfoForSync(null);
            if (appInfos == null || appInfos.isEmpty()) {
                log.info("无应用数据需要同步");
                return;
            }

            List<AppDoc> appDocs = appInfos.stream()
                    .map(this::convertToAppDoc)
                    .collect(Collectors.toList());

            appIndexService.bulkIndexApps(appDocs);

            lastSyncTime.set(LocalDateTime.now());
            log.info("全量同步应用完成: count={}, cost={}ms",
                    appDocs.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("全量同步应用失败: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${intellihub.search.sync.app-incremental-cron:0 */5 * * * ?}")
    public void incrementalSync() {
        LocalDateTime syncTime = lastSyncTime.get();
        if (syncTime == null) {
            log.info("首次增量同步应用，执行全量同步");
            fullSync();
            return;
        }

        log.debug("开始增量同步应用数据, lastSyncTime={}", syncTime);
        long startTime = System.currentTimeMillis();

        try {
            List<AppInfoDTO> appInfos = appCenterDubboService.getAppInfoUpdatedAfter(null, syncTime);
            if (appInfos == null || appInfos.isEmpty()) {
                log.debug("无增量应用数据需要同步");
                lastSyncTime.set(LocalDateTime.now());
                return;
            }

            List<AppDoc> appDocs = appInfos.stream()
                    .map(this::convertToAppDoc)
                    .collect(Collectors.toList());

            appIndexService.bulkIndexApps(appDocs);

            lastSyncTime.set(LocalDateTime.now());
            log.info("增量同步应用完成: count={}, cost={}ms",
                    appDocs.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("增量同步应用失败: {}", e.getMessage(), e);
        }
    }

    public void triggerFullSync() {
        fullSync();
    }

    private AppDoc convertToAppDoc(AppInfoDTO dto) {
        AppDoc doc = new AppDoc();
        doc.setId(dto.getId());
        doc.setTenantId(dto.getTenantId());
        doc.setName(dto.getName());
        doc.setCode(dto.getCode());
        doc.setDescription(dto.getDescription());
        doc.setAppType(dto.getAppType());
        doc.setAppTypeName(getAppTypeName(dto.getAppType()));
        doc.setAppKey(dto.getAppKey());
        doc.setStatus(dto.getStatus());
        doc.setStatusName(getStatusName(dto.getStatus()));
        doc.setContactName(dto.getContactName());
        doc.setContactEmail(dto.getContactEmail());
        doc.setCreatedBy(dto.getCreatedBy());
        doc.setCreatedByName(dto.getCreatedByName());
        doc.setCreatedAt(dto.getCreatedAt());
        doc.setUpdatedAt(dto.getUpdatedAt());
        return doc;
    }

    private String getAppTypeName(String appType) {
        return AppType.getNameByCode(appType);
    }

    private String getStatusName(String status) {
        return AppStatus.getNameByCode(status);
    }
}
