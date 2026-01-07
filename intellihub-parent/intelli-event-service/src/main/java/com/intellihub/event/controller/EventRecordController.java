package com.intellihub.event.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.ApiResponse;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventPublishRecord;
import com.intellihub.event.entity.EventStatistics;
import com.intellihub.event.mapper.EventConsumeRecordMapper;
import com.intellihub.event.mapper.EventPublishRecordMapper;
import com.intellihub.event.mapper.EventStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件记录查询 API
 * <p>
 * 提供事件发布记录、消费记录和统计数据的查询接口
 * </p>
 *
 * @author IntelliHub
 */
@RestController
@RequestMapping("/event/v1")
@RequiredArgsConstructor
public class EventRecordController {

    private final EventPublishRecordMapper publishRecordMapper;
    private final EventConsumeRecordMapper consumeRecordMapper;
    private final EventStatisticsMapper statisticsMapper;

    /**
     * 获取事件发布记录列表
     *
     * @param eventCode 事件编码（可选）
     * @param status    状态（可选）：PUBLISHED / FAILED
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param page      页码
     * @param size      每页大小
     * @return 发布记录分页列表
     */
    @GetMapping("/records/publish")
    public ApiResponse<Page<EventPublishRecord>> getPublishRecords(
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        LambdaQueryWrapper<EventPublishRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 条件过滤
        if (StringUtils.hasText(eventCode)) {
            queryWrapper.eq(EventPublishRecord::getEventCode, eventCode);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(EventPublishRecord::getStatus, status);
        }
        if (startTime != null) {
            queryWrapper.ge(EventPublishRecord::getPublishTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(EventPublishRecord::getPublishTime, endTime);
        }

        // 按发布时间倒序
        queryWrapper.orderByDesc(EventPublishRecord::getPublishTime);

        Page<EventPublishRecord> pageResult = publishRecordMapper.selectPage(
                new Page<>(page, size), queryWrapper);
        return ApiResponse.success(pageResult);
    }

    /**
     * 获取事件消费记录列表
     *
     * @param eventCode      事件编码（可选）
     * @param subscriptionId 订阅ID（可选）
     * @param status         状态（可选）：SUCCESS / FAILED / RETRYING / PENDING
     * @param startTime      开始时间（可选）
     * @param endTime        结束时间（可选）
     * @param page           页码
     * @param size           每页大小
     * @return 消费记录分页列表
     */
    @GetMapping("/records/consume")
    public ApiResponse<Page<EventConsumeRecord>> getConsumeRecords(
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) String subscriptionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        LambdaQueryWrapper<EventConsumeRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 条件过滤
        if (StringUtils.hasText(eventCode)) {
            queryWrapper.eq(EventConsumeRecord::getEventCode, eventCode);
        }
        if (StringUtils.hasText(subscriptionId)) {
            queryWrapper.eq(EventConsumeRecord::getSubscriptionId, subscriptionId);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(EventConsumeRecord::getStatus, status);
        }
        if (startTime != null) {
            queryWrapper.ge(EventConsumeRecord::getConsumeTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(EventConsumeRecord::getConsumeTime, endTime);
        }

        // 按消费时间倒序
        queryWrapper.orderByDesc(EventConsumeRecord::getConsumeTime);

        Page<EventConsumeRecord> pageResult = consumeRecordMapper.selectPage(
                new Page<>(page, size), queryWrapper);
        return ApiResponse.success(pageResult);
    }

    /**
     * 获取事件统计数据
     *
     * @param eventCode 事件编码（可选）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 统计数据列表
     */
    @GetMapping("/statistics")
    public ApiResponse<List<EventStatistics>> getStatistics(
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LambdaQueryWrapper<EventStatistics> queryWrapper = new LambdaQueryWrapper<>();

        // 条件过滤
        if (StringUtils.hasText(eventCode)) {
            queryWrapper.eq(EventStatistics::getEventCode, eventCode);
        }
        if (startDate != null) {
            queryWrapper.ge(EventStatistics::getStatDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(EventStatistics::getStatDate, endDate);
        }

        // 按统计日期倒序
        queryWrapper.orderByDesc(EventStatistics::getStatDate);

        // 限制返回数量，避免数据量过大
        queryWrapper.last("LIMIT 100");

        List<EventStatistics> statistics = statisticsMapper.selectList(queryWrapper);
        return ApiResponse.success(statistics);
    }
}
