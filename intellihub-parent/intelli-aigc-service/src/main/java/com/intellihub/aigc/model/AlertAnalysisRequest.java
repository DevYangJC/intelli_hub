package com.intellihub.aigc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * 告警分析请求
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertAnalysisRequest {

    /**
     * 告警ID
     */
    private String alertId;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 规则类型 (error_rate/latency/qps)
     */
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 告警级别 (info/warning/critical)
     */
    private String alertLevel;

    /**
     * 告警消息
     */
    private String alertMessage;

    /**
     * 当前值
     */
    private BigDecimal currentValue;

    /**
     * 阈值
     */
    private BigDecimal thresholdValue;

    /**
     * 触发告警的请求详情
     */
    private List<RequestDetail> requestDetails;

    /**
     * 请求详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestDetail {
        private String requestId;
        private String apiPath;
        private String method;
        private Integer statusCode;
        private Boolean success;
        private Integer latency;
        private String errorMessage;
        private String clientIp;
        private String requestTime;
    }
}
