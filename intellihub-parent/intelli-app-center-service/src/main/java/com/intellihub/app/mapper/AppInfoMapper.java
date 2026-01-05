package com.intellihub.app.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.app.entity.AppInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 应用信息Mapper接口
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AppInfoMapper extends BaseMapper<AppInfo> {

    /**
     * 统计指定租户的应用数量（跳过租户拦截器）
     *
     * @param tenantId 租户ID
     * @return 应用数量
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT COUNT(*) FROM app_info WHERE tenant_id = #{tenantId} AND deleted = 0")
    int countByTenantIdIgnoreTenant(@Param("tenantId") String tenantId);
}
