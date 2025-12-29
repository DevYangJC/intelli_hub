package com.intellihub.governance.dto;

import lombok.Data;

import java.util.List;

/**
 * API统计详情DTO
 * <p>
 * 用于API详情页的统计数据展示
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiStatsDetailDTO {

    /**
     * 今日调用次数
     */
    private Long todayCalls;

    /**
     * 总调用次数
     */
    private Long totalCalls;

    /**
     * 平均响应时间(ms)
     */
    private Integer avgLatency;

    /**
     * 成功率(%)
     */
    private Double successRate;

    /**
     * 今日调用环比增长(%)
     */
    private Double todayTrend;

    /**
     * 总调用环比增长(%)
     */
    private Double totalTrend;

    /**
     * 响应时间环比变化(%)
     */
    private Double latencyTrend;

    /**
     * 成功率环比变化(%)
     */
    private Double successRateTrend;

    /**
     * 状态码分布
     */
    private List<StatusDistribution> statusDistribution;

    /**
     * 响应时间分布
     */
    private List<LatencyDistribution> latencyDistribution;

    /**
     * 状态码分布项
     */
    @Data
    public static class StatusDistribution {
        /**
         * 状态码类别 (2xx, 4xx, 5xx)
         */
        private String code;

        /**
         * 调用次数
         */
        private Long count;

        /**
         * 占比(%)
         */
        private Double percent;

        /**
         * 类型标识 (success, warning, danger)
         */
        private String type;

        /**
         * 颜色值
         */
        private String color;
    }

    /**
     * 响应时间分布项
     */
    @Data
    public static class LatencyDistribution {
        /**
         * 时间范围 (< 50ms, 50-100ms, 100-500ms, > 500ms)
         */
        private String range;

        /**
         * 占比(%)
         */
        private Double percent;

        /**
         * 颜色值
         */
        private String color;
    }
}
