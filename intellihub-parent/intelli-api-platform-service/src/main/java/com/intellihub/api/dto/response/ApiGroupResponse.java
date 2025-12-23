package com.intellihub.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API分组响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiGroupResponse {

    private String id;
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private Integer sort;
    private String color;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 分组下的API数量
     */
    private Integer apiCount;
}
