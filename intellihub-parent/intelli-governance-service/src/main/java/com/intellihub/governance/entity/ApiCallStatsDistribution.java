package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API调用分布统计实体(天维度)
 * <p>
 * 用于存储状态码分布和响应时间分布的预聚合数据
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("api_call_stats_distribution")
public class ApiCallStatsDistribution {

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
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 2xx状态码数量
     */
    @TableField("count_2xx")
    private Long count2xx;

    /**
     * 4xx状态码数量
     */
    @TableField("count_4xx")
    private Long count4xx;

    /**
     * 5xx状态码数量
     */
    @TableField("count_5xx")
    private Long count5xx;

    /**
     * 响应时间<50ms数量
     */
    @TableField("latency_lt_50")
    private Long latencyLt50;

    /**
     * 响应时间50-100ms数量
     */
    @TableField("latency_50_to_100")
    private Long latency50To100;

    /**
     * 响应时间100-500ms数量
     */
    @TableField("latency_100_to_500")
    private Long latency100To500;

    /**
     * 响应时间>500ms数量
     */
    @TableField("latency_gt_500")
    private Long latencyGt500;

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
