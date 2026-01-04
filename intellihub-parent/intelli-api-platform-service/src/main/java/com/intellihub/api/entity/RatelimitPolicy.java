package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 限流策略实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("gateway_ratelimit_policy")
public class RatelimitPolicy {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String tenantId;

    private String name;

    private String description;

    private String type;

    private String dimension;

    private Integer limitValue;

    private Integer timeWindow;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
