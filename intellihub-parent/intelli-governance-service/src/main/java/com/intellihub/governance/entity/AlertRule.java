package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警规则实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("alert_rule")
public class AlertRule {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则类型(error_rate/latency/qps)
     */
    private String ruleType;

    /**
     * API路径(为空表示全局统计)
     * <p>
     * 对应 Redis Key 中的 apiPath 部分:
     * stats:realtime:{tenantId}:{apiPath}:{hour}:{type}
     * </p>
     * <p>
     * 示例:
     * - 空/null: 使用 "global" 查询全局统计
     * - "/api/user": 查询特定 API 的统计
     * </p>
     */
    private String apiId;

    /**
     * 阈值
     */
    private BigDecimal threshold;

    /**
     * 比较运算符(gt/lt/eq/gte/lte)
     */
    private String operator;

    /**
     * 持续时间(秒)
     */
    private Integer duration;

    /**
     * 通知渠道(email/sms/webhook/kafka)
     */
    private String notifyChannels;

    /**
     * 通知目标
     */
    private String notifyTargets;

    /**
     * 状态(active/disabled)
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

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
