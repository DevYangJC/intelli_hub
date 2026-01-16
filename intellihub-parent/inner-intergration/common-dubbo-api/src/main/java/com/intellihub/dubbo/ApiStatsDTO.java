package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * API统计数据DTO
 * <p>
 * 用于Dubbo服务间传输API统计数据
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiStatsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * API ID
     */
    private String apiId;
    
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
