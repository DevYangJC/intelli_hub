package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 配额预警请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaAlertRequest implements Serializable {

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
     * 配额使用百分比
     */
    private Double usagePercentage;

    /**
     * 已使用配额
     */
    private Long usedQuota;

    /**
     * 总配额
     */
    private Long totalQuota;

    /**
     * 告警级别（warning/critical）
     */
    private String alertLevel;

    /**
     * 自定义消息
     */
    private String message;
}
