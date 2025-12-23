package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新租户请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UpdateTenantRequest {

    @Size(max = 100, message = "租户名称不能超过100个字符")
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * Logo
     */
    private String logo;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;
}
