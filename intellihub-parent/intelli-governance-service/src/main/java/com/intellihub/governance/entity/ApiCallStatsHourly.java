package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API调用统计实体(小时维度)
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("api_call_stats_hourly")
public class ApiCallStatsHourly {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * API ID
     */
    private String apiId;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 统计时间(小时整点)
     */
    private LocalDateTime statTime;

    /**
     * 总调用次数
     */
    private Long totalCount;

    /**
     * 成功次数
     */
    private Long successCount;

    /**
     * 失败次数
     */
    private Long failCount;

    /**
     * 平均响应时间(ms)
     */
    private Integer avgLatency;

    /**
     * 最大响应时间(ms)
     */
    private Integer maxLatency;

    /**
     * 最小响应时间(ms)
     */
    private Integer minLatency;

    /**
     * P95响应时间(ms)
     */
    private Integer p95Latency;

    /**
     * P99响应时间(ms)
     */
    private Integer p99Latency;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
