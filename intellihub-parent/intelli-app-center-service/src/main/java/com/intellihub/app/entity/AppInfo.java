package com.intellihub.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.intellihub.common.enums.AppStatus;
import com.intellihub.common.enums.AppType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息实体类
 * <p>
 * 存储应用的基本信息，包括AppKey/AppSecret等凭证信息
 * 应用是API调用方的身份标识，通过AppKey进行API调用鉴权
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("app_info")
public class AppInfo {

    /**
     * 应用ID，主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 租户ID，用于多租户隔离
     */
    private String tenantId;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用编码，租户内唯一
     */
    private String code;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用类型：internal-内部应用，external-外部应用
     */
    private String appType;

    /**
     * AppKey，用于API调用认证，全局唯一
     */
    private String appKey;

    /**
     * AppSecret，用于签名验证，加密存储
     */
    private String appSecret;

    /**
     * 应用状态：active-正常，disabled-禁用，expired-过期
     */
    private String status;

    /**
     * 调用配额限制（每日）
     */
    private Long quotaLimit;

    /**
     * 已使用配额
     */
    private Long quotaUsed;

    /**
     * 配额重置时间
     */
    private LocalDateTime quotaResetTime;

    /**
     * 回调地址，用于异步通知
     */
    private String callbackUrl;

    /**
     * IP白名单，多个用逗号分隔
     */
    private String ipWhitelist;

    /**
     * AppKey过期时间，null表示永不过期
     */
    private LocalDateTime expireTime;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

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

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
