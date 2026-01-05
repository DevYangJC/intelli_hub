package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface IamRoleMapper extends BaseMapper<IamRole> {

    /**
     * 根据用户ID查询角色列表（登录场景，跨租户查询）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT r.* FROM iam_role r " +
            "INNER JOIN iam_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<IamRole> selectRolesByUserId(@Param("userId") String userId);
}
