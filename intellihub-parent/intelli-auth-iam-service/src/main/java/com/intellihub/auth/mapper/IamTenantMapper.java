package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 租户Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface IamTenantMapper extends BaseMapper<IamTenant> {

    /**
     * 根据租户编码查询租户
     *
     * @param code 租户编码
     * @return 租户信息
     */
    @Select("SELECT * FROM iam_tenant WHERE code = #{code} AND deleted_at IS NULL")
    IamTenant selectByCode(String code);
}
