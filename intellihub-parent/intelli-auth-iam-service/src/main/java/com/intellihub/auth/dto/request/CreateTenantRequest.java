package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建租户请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class CreateTenantRequest {

    @NotBlank(message = "租户名称不能为空")
    @Size(max = 100, message = "租户名称不能超过100个字符")
    private String name;

    @NotBlank(message = "租户编码不能为空")
    @Size(max = 50, message = "租户编码不能超过50个字符")
    private String code;

    /**
     * 描述
     */
    private String description;

    /**
     * 管理员用户名
     */
    @NotBlank(message = "管理员用户名不能为空")
    private String adminUsername;

    /**
     * 管理员密码
     */
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;

    /**
     * 管理员邮箱
     */
    private String adminEmail;

    /**
     * 管理员手机号
     */
    private String adminPhone;

    /**
     * 配额设置
     */
    private TenantQuotaRequest quota;
}
