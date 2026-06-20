package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 性能告警请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceAlertRequest implements Serializable {

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
     * 模型名称
     */
    private String modelName;

    /**
     * 平均耗时（毫秒）
     */
    private Double avgDuration;

    /**
     * 失败率
     */
    private Double failureRate;

    /**
     * 总请求数
     */
    private Long totalRequests;

    /**
     * 失败次数
     */
    private Long failureCount;

    /**
     * 告警级别（warning/critical）
     */
    private String alertLevel;

    /**
     * 自定义消息
     */
    private String message;
}
