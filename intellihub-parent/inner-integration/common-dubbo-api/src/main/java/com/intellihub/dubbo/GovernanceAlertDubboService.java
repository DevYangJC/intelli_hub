package com.intellihub.dubbo;

/**
 * Governance告警Dubbo服务接口
 * 供其他服务调用统一告警能力
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface GovernanceAlertDubboService {

    /**
     * 发送配额预警
     *
     * @param request 配额预警请求
     */
    void sendQuotaAlert(QuotaAlertRequest request);

    /**
     * 发送成本预警
     *
     * @param request 成本预警请求
     */
    void sendCostAlert(CostAlertRequest request);

    /**
     * 发送异常告警
     *
     * @param request 异常告警请求
     */
    void sendErrorAlert(ErrorAlertRequest request);

    /**
     * 发送性能告警
     *
     * @param request 性能告警请求
     */
    void sendPerformanceAlert(PerformanceAlertRequest request);

    /**
     * 发送通用告警
     *
     * @param request 通用告警请求
     */
    void sendGenericAlert(GenericAlertRequest request);
}
