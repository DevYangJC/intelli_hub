package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 租户Mapper
 * 跳过租户拦截器：iam_tenant 表本身无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IamTenantMapper extends BaseMapper<IamTenant> {

    /**
     * 根据租户编码查询租户
     * 跳过租户拦截器：iam_tenant 表本身无 tenant_id
     *
     * @param code 租户编码
     * @return 租户信息
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM iam_tenant WHERE code = #{code} AND deleted_at IS NULL")
    IamTenant selectByCode(String code);
}
