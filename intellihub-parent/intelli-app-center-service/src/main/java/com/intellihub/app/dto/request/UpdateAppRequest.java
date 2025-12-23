package com.intellihub.app.dto.request;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 更新应用请求DTO
 * <p>
 * 用于接收更新应用时的请求参数
 * 所有字段均为可选，只更新传入的非空字段
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UpdateAppRequest {

    /**
     * 应用名称
     */
    @Size(max = 100, message = "应用名称不能超过100个字符")
    private String name;

    /**
     * 应用描述
     */
    @Size(max = 500, message = "应用描述不能超过500个字符")
    private String description;

    /**
     * 应用类型：internal-内部应用，external-外部应用
     */
    @Pattern(regexp = "^(internal|external)$", message = "应用类型只能是internal或external")
    private String appType;

    /**
     * 调用配额限制（每日）
     */
    @Min(value = 0, message = "配额限制不能为负数")
    private Long quotaLimit;

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * IP白名单，多个用逗号分隔
     */
    private String ipWhitelist;

    /**
     * AppKey过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String contactEmail;

    /**
     * 联系人电话
     */
    private String contactPhone;
}
