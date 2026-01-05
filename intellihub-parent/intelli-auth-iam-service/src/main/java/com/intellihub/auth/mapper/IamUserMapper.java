package com.intellihub.auth.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.auth.entity.IamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
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
     * 根据用户名查询用户（不区分租户，用于登录场景）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM iam_user WHERE username = #{username} AND deleted_at IS NULL LIMIT 1")
    IamUser selectByUsername(@Param("username") String username);

    /**
     * 根据用户名和租户ID查询用户
     */
    @Select("SELECT * FROM iam_user WHERE username = #{username} AND tenant_id = #{tenantId} AND deleted_at IS NULL LIMIT 1")
    IamUser selectByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);

    /**
     * 根据租户ID查询所有用户
     */
    @Select("SELECT * FROM iam_user WHERE tenant_id = #{tenantId} AND deleted_at IS NULL")
    List<IamUser> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 查询所有活跃用户（管理场景，跨租户查询）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM iam_user WHERE deleted_at IS NULL")
    List<IamUser> selectAllActive();

    /**
     * 查询更新时间在指定时间之后的用户（用于增量同步）
     */
    @Select("<script>" +
            "SELECT * FROM iam_user WHERE deleted_at IS NULL " +
            "<if test='tenantId != null and tenantId != \"\"'> AND tenant_id = #{tenantId} </if>" +
            "<if test='lastSyncTime != null'> AND updated_at > #{lastSyncTime} </if>" +
            "</script>")
    List<IamUser> selectUpdatedAfter(@Param("tenantId") String tenantId, @Param("lastSyncTime") LocalDateTime lastSyncTime);
}
