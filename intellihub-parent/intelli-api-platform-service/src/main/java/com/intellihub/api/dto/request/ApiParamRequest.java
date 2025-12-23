package com.intellihub.api.dto.request;

import lombok.Data;

/**
 * API参数请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiParamRequest {

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
}
