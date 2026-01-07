package com.intellihub.aigc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AIGC配额配置实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("aigc_quota_config")
public class AigcQuotaConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 每日配额（Token数）
     */
    private Long dailyQuota;

    /**
     * 已用配额
     */
    private Long usedQuota;

    /**
     * 总成本（元）
     */
    private BigDecimal totalCost;

    /**
     * 配额重置时间
     */
    private LocalDateTime resetTime;

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
