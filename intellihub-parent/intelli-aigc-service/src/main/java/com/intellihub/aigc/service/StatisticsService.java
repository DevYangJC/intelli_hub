package com.intellihub.aigc.service;

import java.util.Map;

/**
 * 统计分析服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface StatisticsService {

    /**
     * 获取租户调用统计
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 统计数据
     */
    Map<String, Object> getTenantStatistics(String tenantId, int days);

    /**
     * 获取模型使用排行
     *
     * @param tenantId 租户ID
     * @param limit 返回数量
     * @return 排行数据
     */
    Map<String, Object> getModelRanking(String tenantId, int limit);

    /**
     * 获取每日调用趋势
     *
     * @param tenantId 租户ID
     * @param days 统计天数
     * @return 趋势数据
     */
    Map<String, Object> getDailyTrend(String tenantId, int days);

    /**
     * 获取实时概览
     *
     * @param tenantId 租户ID
     * @return 概览数据
     */
    Map<String, Object> getRealTimeOverview(String tenantId);

    /**
     * 获取用户使用排行
     *
     * @param tenantId 租户ID
     * @param limit 返回数量
     * @return 用户排行
     */
    Map<String, Object> getUserRanking(String tenantId, int limit);
}
