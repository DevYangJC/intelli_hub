package com.intellihub.governance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.governance.entity.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API调用日志Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {

    /**
     * 统计指定时间段内的调用数据（用于聚合）
     */
    @Select("SELECT " +
            "tenant_id, api_id, api_path, app_id, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) as fail_count, " +
            "AVG(latency) as avg_latency, " +
            "MAX(latency) as max_latency, " +
            "MIN(latency) as min_latency " +
            "FROM api_call_log " +
            "WHERE request_time >= #{startTime} AND request_time < #{endTime} " +
            "GROUP BY tenant_id, api_id, api_path, app_id")
    List<Map<String, Object>> aggregateByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的日志（用于清理）
     */
    @Select("DELETE FROM api_call_log WHERE request_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
}
