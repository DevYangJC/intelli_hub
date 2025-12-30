package com.intellihub.dubbo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用信息 DTO（用于搜索索引同步）
 *
 * @author IntelliHub
 */
@Data
public class AppInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private String appType;
    private String appKey;
    private String status;
    private String contactName;
    private String contactEmail;
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
