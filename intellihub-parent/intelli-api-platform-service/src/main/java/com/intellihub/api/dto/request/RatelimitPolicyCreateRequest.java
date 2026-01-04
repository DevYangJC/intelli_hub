package com.intellihub.api.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 限流策略创建请求DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class RatelimitPolicyCreateRequest {

    @NotBlank(message = "策略名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "限流类型不能为空")
    @Pattern(regexp = "qps|concurrency", message = "限流类型必须为qps或concurrency")
    private String type;

    @NotBlank(message = "限流维度不能为空")
    @Pattern(regexp = "global|ip|path|ip_path|user", message = "限流维度必须为global/ip/path/ip_path/user之一")
    private String dimension;

    @Min(value = 1, message = "限流阈值必须大于0")
    private Integer limitValue;

    @Min(value = 1, message = "时间窗口必须大于0")
    private Integer timeWindow = 1;
}
