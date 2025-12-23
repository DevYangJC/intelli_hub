package com.intellihub.common.dubbo;

/**
 * 认证服务 Dubbo 接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AuthDubboService {

    /**
     * 验证 Token 并获取用户信息
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
}
