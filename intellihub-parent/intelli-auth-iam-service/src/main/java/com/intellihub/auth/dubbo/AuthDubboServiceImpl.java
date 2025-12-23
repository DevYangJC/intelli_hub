package com.intellihub.auth.dubbo;

import com.intellihub.common.dubbo.AuthDubboService;
import com.intellihub.common.dubbo.UserInfoDTO;
import com.intellihub.common.dubbo.ValidateTokenResponse;
import com.intellihub.auth.entity.IamPermission;
import com.intellihub.auth.entity.IamRole;
import com.intellihub.auth.entity.IamTenant;
import com.intellihub.auth.entity.IamUser;
import com.intellihub.auth.mapper.IamPermissionMapper;
import com.intellihub.auth.mapper.IamRoleMapper;
import com.intellihub.auth.mapper.IamTenantMapper;
import com.intellihub.auth.mapper.IamUserMapper;
import com.intellihub.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务 Dubbo 实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService(version = "1.0.0", group = "intellihub")
@RequiredArgsConstructor
public class AuthDubboServiceImpl implements AuthDubboService {

    private final IamUserMapper userMapper;
    private final IamTenantMapper tenantMapper;
    private final IamRoleMapper roleMapper;
    private final IamPermissionMapper permissionMapper;
    private final JwtUtil jwtUtil;

    @Override
    public ValidateTokenResponse validateToken(String token) {
        try {
            // 使用 JwtUtil 解析 Token，确保与生成 Token 时使用相同的密钥和方式
            String userId = jwtUtil.getUserId(token);
            if (userId == null) {
                return ValidateTokenResponse.fail("Token 中没有用户信息");
            }

            // 检查 Token 类型
            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                return ValidateTokenResponse.fail("无效的 Token 类型");
            }

            // 查询用户
            IamUser user = userMapper.selectById(userId);
            if (user == null) {
                return ValidateTokenResponse.fail("用户不存在");
            }

            if (!"active".equals(user.getStatus())) {
                return ValidateTokenResponse.fail("用户已被禁用");
            }

            // 构建用户信息
            UserInfoDTO userInfo = buildUserInfoDTO(user);
            return ValidateTokenResponse.success(userInfo);

        } catch (Exception e) {
            log.error("Token 验证失败", e);
            return ValidateTokenResponse.fail("Token 无效或已过期");
        }
    }

    @Override
    public UserInfoDTO getUserById(String userId) {
        IamUser user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return buildUserInfoDTO(user);
    }

    @Override
    public UserInfoDTO getUserByUsername(String username) {
        IamUser user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        return buildUserInfoDTO(user);
    }

    /**
     * 构建用户信息 DTO
     */
    private UserInfoDTO buildUserInfoDTO(IamUser user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus() != null ? ("active".equals(user.getStatus()) ? 1 : 0) : 0);
        dto.setTenantId(user.getTenantId());

        // 查询租户
        IamTenant tenant = tenantMapper.selectById(user.getTenantId());
        if (tenant != null) {
            dto.setTenantCode(tenant.getCode());
        }

        // 查询角色
        List<IamRole> roles = roleMapper.selectRolesByUserId(user.getId());
        dto.setRoles(roles.stream().map(IamRole::getCode).collect(Collectors.toList()));

        // 查询权限
        List<IamPermission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());
        dto.setPermissions(permissions.stream().map(IamPermission::getCode).collect(Collectors.toList()));

        return dto;
    }
}
