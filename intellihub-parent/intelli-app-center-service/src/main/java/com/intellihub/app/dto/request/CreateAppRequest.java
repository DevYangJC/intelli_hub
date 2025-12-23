package com.intellihub.app.dto.request;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 创建应用请求DTO
 * <p>
 * 用于接收创建应用时的请求参数
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class CreateAppRequest {

    /**
     * 应用名称，必填
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 100, message = "应用名称不能超过100个字符")
    private String name;

    /**
     * 应用编码，必填，租户内唯一
     */
    @NotBlank(message = "应用编码不能为空")
    @Size(max = 50, message = "应用编码不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "应用编码只能包含字母、数字、下划线和横线，且以字母开头")
    private String code;

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
     * 调用配额限制（每日），默认10000
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
     * AppKey过期时间，null表示永不过期
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
