package com.intellihub.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 限流策略响应DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class RatelimitPolicyResponse {

    private String id;

    private String name;

    private String description;

    private String type;

    private String dimension;

    private Integer limitValue;

    private Integer timeWindow;

    private String status;

    private Integer appliedRoutes;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
