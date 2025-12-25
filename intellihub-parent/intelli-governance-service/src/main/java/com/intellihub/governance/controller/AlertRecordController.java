package com.intellihub.governance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intellihub.ApiResponse;
import com.intellihub.context.UserContextHolder;
import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.service.AlertRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 告警记录控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/governance/v1/alert/records")
@RequiredArgsConstructor
public class AlertRecordController {

    private final AlertRecordService alertRecordService;

    /**
     * 分页查询告警记录
     */
    @GetMapping
    public ApiResponse<IPage<AlertRecord>> listRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Long ruleId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        IPage<AlertRecord> page = alertRecordService.listRecords(tenantId, status, alertLevel, 
                ruleId, startTime, endTime, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    /**
     * 获取告警统计
     */
    @GetMapping("/stats")
    public ApiResponse<AlertRecordService.AlertStats> getAlertStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        AlertRecordService.AlertStats stats = alertRecordService.getAlertStats(tenantId, startTime, endTime);
        return ApiResponse.success(stats);
    }

    /**
     * 手动恢复告警
     */
    @PostMapping("/{id}/resolve")
    public ApiResponse<Void> resolveAlert(@PathVariable Long id) {
        alertRecordService.resolveAlert(id);
        return ApiResponse.success(null);
    }
}
