package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API请求参数实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_request_param")
public class ApiRequestParam {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * API ID
     */
    private String apiId;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数类型：string/integer/boolean/array/object
     */
    private String type;

    /**
     * 参数位置：query/header/path/body
     */
    private String location;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 示例值
     */
    private String example;

    /**
     * 参数描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
