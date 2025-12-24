package com.intellihub.governance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intellihub.common.model.Result;
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
@RequestMapping("/api/v1/alert/records")
@RequiredArgsConstructor
public class AlertRecordController {

    private final AlertRecordService alertRecordService;

    /**
     * 分页查询告警记录
     */
    @GetMapping
    public Result<IPage<AlertRecord>> listRecords(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Long ruleId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AlertRecord> page = alertRecordService.listRecords(tenantId, status, alertLevel, 
                ruleId, startTime, endTime, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取告警统计
     */
    @GetMapping("/stats")
    public Result<AlertRecordService.AlertStats> getAlertStats(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "default") String tenantId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        AlertRecordService.AlertStats stats = alertRecordService.getAlertStats(tenantId, startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 手动恢复告警
     */
    @PostMapping("/{id}/resolve")
    public Result<Void> resolveAlert(@PathVariable Long id) {
        alertRecordService.resolveAlert(id);
        return Result.success();
    }
}
