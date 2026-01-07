package com.intellihub.aigc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Prompt模板实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("aigc_prompt_template")
public class AigcPromptTemplate {

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
     * 模板名称
     */
    private String name;

    /**
     * 模板代码（唯一标识）
     */
    private String code;

    /**
     * 模板内容（支持{变量}占位符）
     */
    private String content;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型（text/chat/custom）
     */
    private String type;

    /**
     * 变量定义（JSON格式）
     */
    private String variables;

    /**
     * 是否公共模板（0否1是）
     */
    private Integer isPublic;

    /**
     * 使用次数
     */
    private Long useCount;

    /**
     * 状态（0禁用1启用）
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除（0未删除1已删除）
     */
    private Integer deleted;
}
