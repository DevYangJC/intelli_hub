package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.ApiCallStatsDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * API调用统计Mapper(天维度)
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiCallStatsDailyMapper extends BaseMapper<ApiCallStatsDaily> {

    /**
     * 查询日期范围内的统计趋势
     */
    @Select("SELECT * FROM api_call_stats_daily " +
            "WHERE tenant_id = #{tenantId} " +
            "AND stat_date >= #{startDate} AND stat_date <= #{endDate} " +
            "ORDER BY stat_date ASC")
    List<ApiCallStatsDaily> selectTrend(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询指定API的统计趋势
     */
    @Select("SELECT * FROM api_call_stats_daily " +
            "WHERE tenant_id = #{tenantId} AND api_path = #{apiPath} " +
            "AND stat_date >= #{startDate} AND stat_date <= #{endDate} " +
            "ORDER BY stat_date ASC")
    List<ApiCallStatsDaily> selectApiTrend(
            @Param("tenantId") String tenantId,
            @Param("apiPath") String apiPath,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取Top N API（按调用量排序）
     */
    @Select("SELECT * FROM api_call_stats_daily " +
            "WHERE tenant_id = #{tenantId} AND stat_date = #{statDate} " +
            "ORDER BY total_count DESC LIMIT #{limit}")
    List<ApiCallStatsDaily> selectTopApis(
            @Param("tenantId") String tenantId,
            @Param("statDate") LocalDate statDate,
            @Param("limit") int limit);
}
