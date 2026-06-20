package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 异常告警请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorAlertRequest implements Serializable {

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
     * 错误消息
     */
    private String errorMessage;

    /**
     * 错误次数
     */
    private Long errorCount;

    /**
     * 错误率
     */
    private Double errorRate;

    /**
     * 告警级别（warning/critical）
     */
    private String alertLevel;

    /**
     * 异常堆栈（可选）
     */
    private String stackTrace;
}
