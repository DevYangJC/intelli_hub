package com.intellihub.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.intellihub.common.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用API订阅关系实体类
 * <p>
 * 记录应用与API的订阅绑定关系
 * 只有订阅了API的应用才能调用该API
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("app_api_subscription")
public class AppApiSubscription {

    /**
     * 订阅ID，主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * API ID
     */
    private String apiId;

    /**
     * API名称（冗余字段，便于查询展示）
     */
    private String apiName;

    /**
     * API路径（冗余字段）
     */
    private String apiPath;

    /**
     * 订阅状态：active-生效，disabled-禁用，expired-过期
     */
    private SubscriptionStatus status;

    /**
     * 单独的调用限额（覆盖应用级配额），null表示使用应用配额
     */
    private Long quotaLimit;

    /**
     * 订阅生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 订阅过期时间，null表示永不过期
     */
    private LocalDateTime expireTime;

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
