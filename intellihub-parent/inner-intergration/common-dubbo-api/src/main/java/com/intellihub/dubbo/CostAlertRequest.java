package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 成本预警请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostAlertRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 当前成本
     */
    private Double currentCost;

    /**
     * 预测成本
     */
    private Double forecastCost;

    /**
     * 成本阈值
     */
    private Double threshold;

    /**
     * 告警级别（warning/critical）
     */
    private String alertLevel;

    /**
     * 自定义消息
     */
    private String message;
}
