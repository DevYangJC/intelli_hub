package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.ApiCallStatsDistribution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * API调用分布统计Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiCallStatsDistributionMapper extends BaseMapper<ApiCallStatsDistribution> {

    /**
     * 查询指定API在日期范围内的分布统计
     */
    List<ApiCallStatsDistribution> selectByApiIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("apiId") String apiId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 汇总指定API在日期范围内的分布统计
     */
    ApiCallStatsDistribution sumByApiIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("apiId") String apiId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
