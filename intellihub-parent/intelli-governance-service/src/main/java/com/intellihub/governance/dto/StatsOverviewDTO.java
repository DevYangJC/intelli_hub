package com.intellihub.governance.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计概览DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class StatsOverviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 今日调用总数
     */
    private Long todayTotalCount;

    /**
     * 今日成功数
     */
    private Long todaySuccessCount;

    /**
     * 今日失败数
     */
    private Long todayFailCount;

    /**
     * 今日成功率(%)
     */
    private Double todaySuccessRate;

    /**
     * 今日平均延迟(ms)
     */
    private Integer todayAvgLatency;

    /**
     * 昨日调用总数
     */
    private Long yesterdayTotalCount;

    /**
     * 日环比增长率(%)
     */
    private Double dayOverDayRate;

    /**
     * 当前小时QPS
     */
    private Double currentQps;

    /**
     * API总数
     */
    private Integer apiCount;

    /**
     * 应用总数
     */
    private Integer appCount;
}
