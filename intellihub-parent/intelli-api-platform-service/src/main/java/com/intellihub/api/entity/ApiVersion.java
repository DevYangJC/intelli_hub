package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API版本历史实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_version")
public class ApiVersion {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * API ID
     */
    private String apiId;

    /**
     * 版本号
     */
    private String version;

    /**
     * API快照(JSON)
     */
    private String snapshot;

    /**
     * 变更说明
     */
    private String changeLog;

    /**
     * 创建人ID
     */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
