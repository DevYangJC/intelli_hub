package com.intellihub.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("iam_tenant")
public class IamTenant {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 租户编码
     */
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * Logo URL
     */
    private String logo;

    /**
     * 状态：active/inactive
     */
    private String status;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大应用数
     */
    private Integer maxApps;

    /**
     * 最大API数
     */
    private Integer maxApis;

    /**
     * 每日最大调用次数
     */
    private Long maxCallsPerDay;

    /**
     * 每月最大调用次数
     */
    private Long maxCallsPerMonth;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 软删除时间
     */
//    @TableLogic
    private LocalDateTime deletedAt;
}
