package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 角色权限关联Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface IamRolePermissionMapper extends BaseMapper<IamRolePermission> {

    /**
     * 删除角色的所有权限关联
     */
    @Delete("DELETE FROM iam_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);
}
