package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 用户角色关联Mapper
 * 跳过租户拦截器：iam_user_role 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IamUserRoleMapper extends BaseMapper<IamUserRole> {

    /**
     * 删除用户的所有角色关联
     */
    @Delete("DELETE FROM iam_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);
}
