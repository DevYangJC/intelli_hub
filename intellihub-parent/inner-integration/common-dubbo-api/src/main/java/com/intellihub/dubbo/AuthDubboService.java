package com.intellihub.dubbo;

import java.util.List;

/**
 * 认证Dubbo服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AuthDubboService {

    /**
     * 验证Token
     *
     * @param token JWT Token
     * @return 验证结果
     */
    ValidateTokenResponse validateToken(String token);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserById(String userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoDTO getUserByUsername(String username);

    /**
     * 获取所有用户信息（用于搜索索引同步）
     *
     * @param tenantId 租户ID（可选）
     * @return 用户信息列表
     */
    List<UserInfoSearchDTO> getAllUserInfoForSync(String tenantId);

    /**
     * 获取增量更新的用户信息（用于搜索索引增量同步）
     *
     * @param tenantId     租户ID（可选）
     * @param lastSyncTime 上次同步时间
     * @return 用户信息列表
     */
    List<UserInfoSearchDTO> getUserInfoUpdatedAfter(String tenantId, java.time.LocalDateTime lastSyncTime);
}
