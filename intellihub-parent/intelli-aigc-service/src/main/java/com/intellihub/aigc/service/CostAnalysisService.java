package com.intellihub.aigc.service;

import java.util.Map;

/**
 * 成本分析服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface CostAnalysisService {

    /**
     * 获取成本概览
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 成本概览
     */
    Map<String, Object> getCostOverview(String tenantId, int days);

    /**
     * 按模型分析成本
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 模型成本分析
     */
    Map<String, Object> getCostByModel(String tenantId, int days);

    /**
     * 按日期分析成本
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 每日成本趋势
     */
    Map<String, Object> getCostByDate(String tenantId, int days);

    /**
     * 获取成本预测
     *
     * @param tenantId 租户ID
     * @return 本月预测成本
     */
    Map<String, Object> getCostForecast(String tenantId);

    /**
     * 导出成本报表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表数据
     */
    Map<String, Object> exportCostReport(String tenantId, String startDate, String endDate);
}
