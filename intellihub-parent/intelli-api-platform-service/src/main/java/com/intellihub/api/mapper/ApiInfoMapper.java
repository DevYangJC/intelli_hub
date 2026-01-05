package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * API信息Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiInfoMapper extends BaseMapper<ApiInfo> {

    /**
     * 统计指定租户的API数量（跳过租户拦截器）
     *
     * @param tenantId 租户ID
     * @return API数量
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COUNT(*) FROM api_info WHERE tenant_id = #{tenantId} AND deleted_at IS NULL")
    int countByTenantIdIgnoreTenant(@Param("tenantId") String tenantId);

    /**
     * 统计指定租户的今日调用次数（跳过租户拦截器）
     *
     * @param tenantId 租户ID
     * @return 今日调用次数
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COALESCE(SUM(today_calls), 0) FROM api_info WHERE tenant_id = #{tenantId} AND deleted_at IS NULL")
    long sumTodayCallsByTenantIdIgnoreTenant(@Param("tenantId") String tenantId);
}
