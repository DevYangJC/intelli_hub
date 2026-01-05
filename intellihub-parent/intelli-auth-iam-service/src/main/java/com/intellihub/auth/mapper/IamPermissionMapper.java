package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 * 跳过租户拦截器：iam_permission 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface IamPermissionMapper extends BaseMapper<IamPermission> {

    /**
     * 根据角色ID查询权限列表
     * 跳过租户拦截器：iam_permission 和 iam_role_permission 表均无 tenant_id
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT p.* FROM iam_permission p " +
            "INNER JOIN iam_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<IamPermission> selectPermissionsByRoleId(@Param("roleId") String roleId);

    /**
     * 根据用户ID查询权限列表
     * 跳过租户拦截器：涉及 iam_permission/iam_role_permission/iam_user_role 表，均无 tenant_id
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT DISTINCT p.* FROM iam_permission p " +
            "INNER JOIN iam_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN iam_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<IamPermission> selectPermissionsByUserId(@Param("userId") String userId);
}
