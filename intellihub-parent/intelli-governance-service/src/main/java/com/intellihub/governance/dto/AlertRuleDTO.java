package com.intellihub.governance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 告警规则DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AlertRuleDTO {

    private Long id;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String name;

    /**
     * 规则类型(error_rate/latency/qps)
     */
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * API ID(为空表示全局)
     */
    private String apiId;

    /**
     * API路径(前端展示用)
     */
    private String apiPath;

    /**
     * 阈值
     */
    @NotNull(message = "阈值不能为空")
    private BigDecimal threshold;

    /**
     * 比较运算符(gt/lt/eq/gte/lte)
     */
    @NotBlank(message = "比较运算符不能为空")
    private String operator;

    /**
     * 持续时间(秒)
     */
    private Integer duration = 60;

    /**
     * 通知渠道(email/sms/webhook/kafka)，逗号分隔
     */
    private String notifyChannels;

    /**
     * 通知目标，逗号分隔
     */
    private String notifyTargets;

    /**
     * 状态(active/disabled)
     */
    private String status = "active";
}
