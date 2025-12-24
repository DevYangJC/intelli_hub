package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
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

    @TableId(type = IdType.AUTO)
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
     * API ID(为空表示全局)
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
