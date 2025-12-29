package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_announcement")
public class Announcement {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String tenantId;

    private String title;

    private String description;

    /**
     * 类型: notice-系统通知, feature-功能更新, maintenance-维护公告, warning-重要提醒
     */
    private String type;

    private String meta;

    /**
     * 状态: published-已发布, draft-草稿
     */
    private String status;

    private LocalDateTime publishTime;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

//    @TableLogic
    private Integer deleted;
}
