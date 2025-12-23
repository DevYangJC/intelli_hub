package com.intellihub.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建API版本请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class CreateApiVersionRequest {

    /**
     * 版本号
     */
    @NotBlank(message = "版本号不能为空")
    private String version;

    /**
     * 变更说明
     */
    private String changeLog;
}
