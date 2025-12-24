package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.ApiCallStatsHourly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API调用统计Mapper(小时维度)
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiCallStatsHourlyMapper extends BaseMapper<ApiCallStatsHourly> {

    /**
     * 查询时间范围内的统计趋势（按时间点聚合）
     */
    @Select("SELECT stat_time, tenant_id, " +
            "SUM(total_count) as total_count, " +
            "SUM(success_count) as success_count, " +
            "SUM(fail_count) as fail_count, " +
            "AVG(avg_latency) as avg_latency, " +
            "MAX(max_latency) as max_latency, " +
            "MIN(min_latency) as min_latency " +
            "FROM api_call_stats_hourly " +
            "WHERE tenant_id = #{tenantId} " +
            "AND stat_time >= #{startTime} AND stat_time <= #{endTime} " +
            "GROUP BY stat_time, tenant_id " +
            "ORDER BY stat_time ASC")
    List<ApiCallStatsHourly> selectTrend(
            @Param("tenantId") String tenantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定API的统计趋势
     */
    @Select("SELECT * FROM api_call_stats_hourly " +
            "WHERE tenant_id = #{tenantId} AND api_path = #{apiPath} " +
            "AND stat_time >= #{startTime} AND stat_time <= #{endTime} " +
            "ORDER BY stat_time ASC")
    List<ApiCallStatsHourly> selectApiTrend(
            @Param("tenantId") String tenantId,
            @Param("apiPath") String apiPath,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
