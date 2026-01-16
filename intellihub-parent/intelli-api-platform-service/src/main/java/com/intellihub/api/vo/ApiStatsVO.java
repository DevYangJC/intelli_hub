package com.intellihub.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API统计数据VO
 * <p>
 * 用于前端展示API调用统计信息
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStatsVO {
    
    /**
     * 今日调用次数
     */
    private Long todayCalls;
    
    /**
     * 总调用次数
     */
    private Long totalCalls;
    
    /**
     * 成功调用次数
     */
    private Long successCalls;
    
    /**
     * 成功率（百分比，如 99.5）
     */
    private Double successRate;
    
    /**
     * 平均响应时间（毫秒）
     */
    private Double avgResponseTime;
}

