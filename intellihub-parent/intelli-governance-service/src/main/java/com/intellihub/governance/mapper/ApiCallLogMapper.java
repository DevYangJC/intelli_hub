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

    /**
     * 按API ID统计今日调用次数
     * <p>
     * 对应 XML: countTodayCallsByApiId
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return API调用次数统计列表
     */
    List<Map<String, Object>> countTodayCallsByApiId(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按API ID统计历史总调用次数
     * <p>
     * 对应 XML: countTotalCallsByApiId
     * </p>
     *
     * @return API调用次数统计列表
     */
    List<Map<String, Object>> countTotalCallsByApiId();

    /**
     * 按App ID统计总调用次数（配额使用）
     * <p>
     * 对应 XML: countCallsByAppId
     * </p>
     *
     * @return App调用次数统计列表
     */
    List<Map<String, Object>> countCallsByAppId();

    /**
     * 获取今日有调用的API ID列表
     * <p>
     * 对应 XML: selectTodayCalledApiIds
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return API ID列表
     */
    List<String> selectTodayCalledApiIds(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
