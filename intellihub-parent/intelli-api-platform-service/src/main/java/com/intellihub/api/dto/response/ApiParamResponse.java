package com.intellihub.api.dto.response;

import lombok.Data;

/**
 * API参数响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiParamResponse {

    private String id;
    private String apiId;
    private String name;
    private String type;
    private String location;
    private Boolean required;
    private String defaultValue;
    private String example;
    private String description;
    private Integer sort;
}
