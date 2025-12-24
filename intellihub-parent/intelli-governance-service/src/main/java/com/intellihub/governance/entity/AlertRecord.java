package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警记录实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("alert_record")
public class AlertRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * API ID
     */
    private String apiId;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 告警级别(info/warning/critical)
     */
    private String alertLevel;

    /**
     * 告警内容
     */
    private String alertMessage;

    /**
     * 当前值
     */
    private BigDecimal currentValue;

    /**
     * 阈值
     */
    private BigDecimal thresholdValue;

    /**
     * 状态(firing/resolved)
     */
    private String status;

    /**
     * 触发时间
     */
    private LocalDateTime firedAt;

    /**
     * 恢复时间
     */
    private LocalDateTime resolvedAt;

    /**
     * 是否已通知
     */
    private Boolean notified;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
