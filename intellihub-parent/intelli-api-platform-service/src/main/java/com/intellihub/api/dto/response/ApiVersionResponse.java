package com.intellihub.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API版本响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiVersionResponse {

    private String id;

    private String apiId;

    private String version;

    private String snapshot;

    private String changeLog;

    private String createdBy;

    private LocalDateTime createdAt;
}
