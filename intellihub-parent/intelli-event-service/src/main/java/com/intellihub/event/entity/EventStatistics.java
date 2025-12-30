package com.intellihub.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 事件统计实体
 * 用于按日统计事件的发布和消费情况，包括成功率、失败率、平均耗时等指标
 * 便于监控事件系统的运行状况和性能分析
 *
 * @author IntelliHub
 */
@Data
@TableName("event_statistics")
public class EventStatistics {

    /**
     * 统计ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID，用于多租户隔离
     */
    private String tenantId;

    /**
     * 事件编码，标识事件类型
     */
    private String eventCode;

    /**
     * 统计日期，按天统计
     */
    private LocalDate statDate;

    /**
     * 发布次数，当天该事件的发布总次数
     */
    private Long publishCount;

    /**
     * 消费次数，当天该事件的消费总次数
     */
    private Long consumeCount;

    /**
     * 成功次数，当天消费成功的次数
     */
    private Long successCount;

    /**
     * 失败次数，当天消费失败的次数
     */
    private Long failedCount;

    /**
     * 平均耗时（毫秒）
     * 当天所有消费的平均处理时间
     */
    private Integer avgCostTime;

    /**
     * 最大耗时（毫秒）
     * 当天消费的最长处理时间
     */
    private Integer maxCostTime;

    /**
     * 创建时间，记录入库时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间，统计数据更新时间
     */
    private LocalDateTime updatedAt;
}
