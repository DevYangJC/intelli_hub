package com.intellihub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.auth.dto.request.AssignRolesRequest;
import com.intellihub.auth.dto.request.CreateUserRequest;
import com.intellihub.auth.dto.request.UpdateUserRequest;
import com.intellihub.auth.dto.request.UserQueryRequest;
import com.intellihub.auth.dto.response.UserInfoResponse;
import com.intellihub.auth.entity.*;
import com.intellihub.auth.mapper.*;
import com.intellihub.auth.service.UserService;
import com.intellihub.constants.ResponseStatus;
import com.intellihub.exception.BusinessException;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final IamUserMapper userMapper;
    private final IamTenantMapper tenantMapper;
    private final IamRoleMapper roleMapper;
    private final IamPermissionMapper permissionMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageData<UserInfoResponse> listUsers(String tenantId, UserQueryRequest request) {
        log.info("查询用户列表 - tenantId: '{}', page: {}, size: {}", tenantId, request.getPage(), request.getSize());
        
        // 先查询总数，用于调试
        Long totalCount = userMapper.selectCount(new LambdaQueryWrapper<IamUser>().eq(IamUser::getTenantId, tenantId));
        log.info("租户 {} 下的用户总数: {}", tenantId, totalCount);
        
        LambdaQueryWrapper<IamUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamUser::getTenantId, tenantId);

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(IamUser::getUsername, request.getKeyword())
                    .or()
                    .like(IamUser::getNickname, request.getKeyword())
                    .or()
                    .like(IamUser::getEmail, request.getKeyword())
                    .or()
                    .like(IamUser::getPhone, request.getKeyword())
            );
        }

        // 状态筛选
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(IamUser::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(IamUser::getCreatedAt);

        IPage<IamUser> page = userMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                wrapper
        );

        List<UserInfoResponse> records = page.getRecords().stream()
                .map(this::convertToUserInfoResponse)
                .collect(Collectors.toList());

        PageData<UserInfoResponse> pageData = new PageData<>(page.getCurrent(), page.getSize());
        pageData.setTotal(page.getTotal());
        pageData.setRecords(records);
        return pageData;
    }

    @Override
    @Transactional
    public UserInfoResponse createUser(String tenantId, CreateUserRequest request) {
        // 检查用户名是否已存在
        IamUser existingUser = userMapper.selectByUsernameAndTenantId(request.getUsername(), tenantId);
        if (existingUser != null) {
            throw new BusinessException(ResponseStatus.DATA_EXISTS.getCode(), "用户名已存在");
        }

        // 创建用户
        IamUser user = IamUser.builder()
                .tenantId(tenantId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status("active")
                .loginFailCount(0)
                .build();

        userMapper.insert(user);

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (String roleId : request.getRoleIds()) {
                IamUserRole userRole = IamUserRole.builder()
                        .userId(user.getId())
                        .roleId(roleId)
                        .build();
                userRoleMapper.insert(userRole);
            }
        }

        return convertToUserInfoResponse(user);
    }

    @Override
    public UserInfoResponse getUserById(String id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }
        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional
    public UserInfoResponse updateUser(String id, UpdateUserRequest request) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 更新用户信息
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        userMapper.updateById(user);

        // 更新角色
        if (request.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(id);
            for (String roleId : request.getRoleIds()) {
                IamUserRole userRole = IamUserRole.builder()
                        .userId(id)
                        .roleId(roleId)
                        .build();
                userRoleMapper.insert(userRole);
            }
        }

        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 软删除
        user.setDeletedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 删除角色关联
        userRoleMapper.deleteByUserId(id);
    }

    @Override
    public void enableUser(String id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        user.setStatus("active");
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        userMapper.updateById(user);
    }

    @Override
    public void disableUser(String id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        user.setStatus("inactive");
        userMapper.updateById(user);
    }

    @Override
    public String resetPassword(String id) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 生成随机密码
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        userMapper.updateById(user);

        return newPassword;
    }

    @Override
    @Transactional
    public void assignRoles(String id, AssignRolesRequest request) {
        IamUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 删除现有角色关联
        userRoleMapper.deleteByUserId(id);

        // 添加新角色关联
        for (String roleId : request.getRoleIds()) {
            IamUserRole userRole = IamUserRole.builder()
                    .userId(id)
                    .roleId(roleId)
                    .build();
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 转换为用户信息响应
     */
    private UserInfoResponse convertToUserInfoResponse(IamUser user) {
        // 查询租户
        IamTenant tenant = tenantMapper.selectById(user.getTenantId());

        // 查询角色和权限
        List<IamRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<IamPermission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

        List<String> roleNames = roles.stream().map(IamRole::getName).collect(Collectors.toList());
        List<String> roleCodes = roles.stream().map(IamRole::getCode).collect(Collectors.toList());
        List<String> permissionCodes = permissions.stream().map(IamPermission::getCode).collect(Collectors.toList());

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(roleCodes.isEmpty() ? null : roleCodes.get(0))
                .roleNames(roleNames)
                .permissions(permissionCodes)
                .tenantId(user.getTenantId())
                .tenantName(tenant != null ? tenant.getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }
}
