package com.intellihub.search.sync;

import com.intellihub.dubbo.AuthDubboService;
import com.intellihub.dubbo.UserInfoSearchDTO;
import com.intellihub.search.constant.UserStatus;
import com.intellihub.search.model.doc.UserDoc;
import com.intellihub.search.service.UserIndexService;
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
 * 用户数据同步任务
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncTask {

    @DubboReference(check = false, timeout = 30000)
    private AuthDubboService authDubboService;

    private final UserIndexService userIndexService;

    private final AtomicReference<LocalDateTime> lastSyncTime = new AtomicReference<>(null);

    @Scheduled(cron = "${intellihub.search.sync.user-full-cron:0 0 2 * * ?}")
    public void fullSync() {
        log.info("开始全量同步用户数据到 ES...");
        long startTime = System.currentTimeMillis();

        try {
            List<UserInfoSearchDTO> userInfos = authDubboService.getAllUserInfoForSync(null);
            if (userInfos == null || userInfos.isEmpty()) {
                log.info("无用户数据需要同步");
                return;
            }

            List<UserDoc> userDocs = userInfos.stream()
                    .map(this::convertToUserDoc)
                    .collect(Collectors.toList());

            userIndexService.bulkIndexUsers(userDocs);

            lastSyncTime.set(LocalDateTime.now());
            log.info("全量同步用户完成: count={}, cost={}ms",
                    userDocs.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("全量同步用户失败: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${intellihub.search.sync.user-incremental-cron:0 */5 * * * ?}")
    public void incrementalSync() {
        LocalDateTime syncTime = lastSyncTime.get();
        if (syncTime == null) {
            log.info("首次增量同步用户，执行全量同步");
            fullSync();
            return;
        }

        log.debug("开始增量同步用户数据, lastSyncTime={}", syncTime);
        long startTime = System.currentTimeMillis();

        try {
            List<UserInfoSearchDTO> userInfos = authDubboService.getUserInfoUpdatedAfter(null, syncTime);
            if (userInfos == null || userInfos.isEmpty()) {
                log.debug("无增量用户数据需要同步");
                lastSyncTime.set(LocalDateTime.now());
                return;
            }

            List<UserDoc> userDocs = userInfos.stream()
                    .map(this::convertToUserDoc)
                    .collect(Collectors.toList());

            userIndexService.bulkIndexUsers(userDocs);

            lastSyncTime.set(LocalDateTime.now());
            log.info("增量同步用户完成: count={}, cost={}ms",
                    userDocs.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("增量同步用户失败: {}", e.getMessage(), e);
        }
    }

    public void triggerFullSync() {
        fullSync();
    }

    private UserDoc convertToUserDoc(UserInfoSearchDTO dto) {
        UserDoc doc = new UserDoc();
        doc.setId(dto.getId());
        doc.setTenantId(dto.getTenantId());
        doc.setUsername(dto.getUsername());
        doc.setNickname(dto.getNickname());
        doc.setEmail(dto.getEmail());
        doc.setPhone(dto.getPhone());
        doc.setAvatar(dto.getAvatar());
        doc.setStatus(dto.getStatus());
        doc.setStatusName(getStatusName(dto.getStatus()));
        doc.setLastLoginAt(dto.getLastLoginAt());
        doc.setCreatedAt(dto.getCreatedAt());
        doc.setUpdatedAt(dto.getUpdatedAt());
        return doc;
    }

    private String getStatusName(String status) {
        return UserStatus.getNameByCode(status);
    }
}
