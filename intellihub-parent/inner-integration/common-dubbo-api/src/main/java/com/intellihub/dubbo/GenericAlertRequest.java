package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 通用告警请求
 * 适用于所有类型的告警
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericAlertRequest implements Serializable {

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
     * 告警类型（quota/cost/error/performance/custom）
     */
    private String alertType;

    /**
     * 告警级别（info/warning/critical）
     */
    private String alertLevel;

    /**
     * 告警标题
     */
    private String title;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 当前值
     */
    private String currentValue;

    /**
     * 阈值
     */
    private String thresholdValue;

    /**
     * 扩展信息
     */
    private Map<String, Object> extraData;
}
