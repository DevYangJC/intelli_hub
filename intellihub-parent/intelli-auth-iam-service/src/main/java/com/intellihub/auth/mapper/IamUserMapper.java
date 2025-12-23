package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface IamUserMapper extends BaseMapper<IamUser> {

    /**
     * 根据用户名查询用户（不区分租户）
     */
    @Select("SELECT * FROM iam_user WHERE username = #{username} AND deleted_at IS NULL LIMIT 1")
    IamUser selectByUsername(@Param("username") String username);

    /**
     * 根据用户名和租户ID查询用户
     */
    @Select("SELECT * FROM iam_user WHERE username = #{username} AND tenant_id = #{tenantId} AND deleted_at IS NULL LIMIT 1")
    IamUser selectByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);
}
