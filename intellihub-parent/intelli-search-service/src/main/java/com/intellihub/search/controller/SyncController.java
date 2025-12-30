package com.intellihub.search.controller;

import com.intellihub.ApiResponse;
import com.intellihub.search.sync.ApiSyncTask;
import com.intellihub.search.sync.AppSyncTask;
import com.intellihub.search.sync.UserSyncTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据同步管理控制器
 *
 * @author IntelliHub
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search/sync")
@RequiredArgsConstructor
public class SyncController {

    private final ApiSyncTask apiSyncTask;
    private final AppSyncTask appSyncTask;
    private final UserSyncTask userSyncTask;

    /**
     * 手动触发全量同步（所有类型）
     */
    @PostMapping("/full")
    public ApiResponse<String> triggerFullSync() {
        log.info("手动触发全量同步（所有类型）");
        apiSyncTask.triggerFullSync();
        appSyncTask.triggerFullSync();
        userSyncTask.triggerFullSync();
        return ApiResponse.success("全量同步任务已触发");
    }

    /**
     * 手动触发 API 全量同步
     */
    @PostMapping("/api/full")
    public ApiResponse<String> triggerApiFullSync() {
        log.info("手动触发 API 全量同步");
        apiSyncTask.triggerFullSync();
        return ApiResponse.success("API 全量同步任务已触发");
    }

    /**
     * 手动触发 API 增量同步
     */
    @PostMapping("/api/incremental")
    public ApiResponse<String> triggerApiIncrementalSync() {
        log.info("手动触发 API 增量同步");
        apiSyncTask.triggerIncrementalSync();
        return ApiResponse.success("API 增量同步任务已触发");
    }

    /**
     * 手动触发应用全量同步
     */
    @PostMapping("/app/full")
    public ApiResponse<String> triggerAppFullSync() {
        log.info("手动触发应用全量同步");
        appSyncTask.triggerFullSync();
        return ApiResponse.success("应用全量同步任务已触发");
    }

    /**
     * 手动触发用户全量同步
     */
    @PostMapping("/user/full")
    public ApiResponse<String> triggerUserFullSync() {
        log.info("手动触发用户全量同步");
        userSyncTask.triggerFullSync();
        return ApiResponse.success("用户全量同步任务已触发");
    }
}
