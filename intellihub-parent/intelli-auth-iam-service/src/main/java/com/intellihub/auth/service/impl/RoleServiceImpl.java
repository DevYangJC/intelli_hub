package com.intellihub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.auth.dto.request.CreateRoleRequest;
import com.intellihub.auth.dto.request.UpdateRolePermissionsRequest;
import com.intellihub.auth.dto.request.UpdateRoleRequest;
import com.intellihub.auth.dto.response.MenuResponse;
import com.intellihub.auth.dto.response.PermissionResponse;
import com.intellihub.auth.dto.response.RoleResponse;
import com.intellihub.auth.entity.*;
import com.intellihub.auth.mapper.*;
import com.intellihub.auth.service.RoleService;
import com.intellihub.constants.ResponseStatus;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.intellihub.context.UserContextHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final IamRoleMapper roleMapper;
    private final IamPermissionMapper permissionMapper;
    private final IamRolePermissionMapper rolePermissionMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final IamMenuMapper menuMapper;

    @Override
    public List<RoleResponse> listRoles() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        LambdaQueryWrapper<IamRole> wrapper = new LambdaQueryWrapper<>();
        // 查询系统角色和当前租户角色（角色表特殊处理，不能完全依赖拦截器）
        wrapper.and(w -> w
                .isNull(IamRole::getTenantId)
                .or()
                .eq(IamRole::getTenantId, tenantId)
        );
        wrapper.orderByAsc(IamRole::getSort);

        List<IamRole> roles = roleMapper.selectList(wrapper);

        return roles.stream().map(role -> {
            // 查询角色权限
            List<IamPermission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            List<String> permissionCodes = permissions.stream()
                    .map(IamPermission::getCode)
                    .collect(Collectors.toList());

            // 查询用户数量
            LambdaQueryWrapper<IamUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
            userRoleWrapper.eq(IamUserRole::getRoleId, role.getId());
            Long userCount = userRoleMapper.selectCount(userRoleWrapper);

            return RoleResponse.builder()
                    .id(role.getId())
                    .code(role.getCode())
                    .name(role.getName())
                    .description(role.getDescription())
                    .isSystem(role.getIsSystem())
                    .userCount(userCount.intValue())
                    .permissions(permissionCodes)
                    .createdAt(role.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        
        // 检查角色编码是否已存在（角色表特殊处理）
        LambdaQueryWrapper<IamRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamRole::getCode, request.getCode());
        wrapper.and(w -> w
                .isNull(IamRole::getTenantId)
                .or()
                .eq(IamRole::getTenantId, tenantId)
        );
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResponseStatus.DATA_EXISTS.getCode(), "角色编码已存在");
        }

        // 创建角色
        IamRole role = IamRole.builder()
                .tenantId(tenantId)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .isSystem(false)
                .sort(0)
                .status("active")
                .build();

        roleMapper.insert(role);

        // 分配权限
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            assignPermissionsByCode(role.getId(), request.getPermissions());
        }

        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(false)
                .userCount(0)
                .permissions(request.getPermissions())
                .createdAt(role.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 系统角色不允许修改
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException(ResponseStatus.OPERATION_NOT_ALLOWED.getCode(), "系统角色不允许修改");
        }

        if (StringUtils.hasText(request.getName())) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        roleMapper.updateById(role);

        // 更新权限
        if (request.getPermissions() != null) {
            rolePermissionMapper.deleteByRoleId(id);
            assignPermissionsByCode(id, request.getPermissions());
        }

        // 查询更新后的权限
        List<IamPermission> permissions = permissionMapper.selectPermissionsByRoleId(id);
        List<String> permissionCodes = permissions.stream()
                .map(IamPermission::getCode)
                .collect(Collectors.toList());

        // 查询用户数量
        LambdaQueryWrapper<IamUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(IamUserRole::getRoleId, id);
        Long userCount = userRoleMapper.selectCount(userRoleWrapper);

        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .userCount(userCount.intValue())
                .permissions(permissionCodes)
                .createdAt(role.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteRole(String id) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 系统角色不允许删除
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException(ResponseStatus.OPERATION_NOT_ALLOWED.getCode(), "系统角色不允许删除");
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<IamUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(IamUserRole::getRoleId, id);
        if (userRoleMapper.selectCount(userRoleWrapper) > 0) {
            throw new BusinessException(ResponseStatus.OPERATION_NOT_ALLOWED.getCode(), "该角色下存在用户，无法删除");
        }

        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);

        // 删除角色
        roleMapper.deleteById(id);
    }

    @Override
    public List<String> getRolePermissions(String id) {
        List<IamPermission> permissions = permissionMapper.selectPermissionsByRoleId(id);
        return permissions.stream()
                .map(IamPermission::getCode)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateRolePermissions(String id, UpdateRolePermissionsRequest request) {
        IamRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND);
        }

        // 系统角色不允许修改权限
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException(ResponseStatus.OPERATION_NOT_ALLOWED.getCode(), "系统角色权限不允许修改");
        }

        // 删除现有权限
        rolePermissionMapper.deleteByRoleId(id);

        // 根据权限编码添加新权限
        if (request.getPermissionCodes() != null && !request.getPermissionCodes().isEmpty()) {
            assignPermissionsByCode(id, request.getPermissionCodes());
        }
    }

    @Override
    public List<PermissionResponse> listPermissions() {
        LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(IamPermission::getGroupName, IamPermission::getSort);

        List<IamPermission> permissions = permissionMapper.selectList(wrapper);

        return permissions.stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .group(p.getGroupName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuResponse> getMenuTree(String userId) {
        // 查询所有菜单
        LambdaQueryWrapper<IamMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IamMenu::getVisible, true);
        wrapper.orderByAsc(IamMenu::getSort);

        List<IamMenu> menus = menuMapper.selectList(wrapper);

        // 如果有用户ID，过滤用户有权限的菜单
        if (StringUtils.hasText(userId)) {
            List<IamPermission> userPermissions = permissionMapper.selectPermissionsByUserId(userId);
            List<String> permissionCodes = userPermissions.stream()
                    .map(IamPermission::getCode)
                    .collect(Collectors.toList());

            // 检查是否有超级权限
            boolean hasAllPermission = permissionCodes.contains("*");

            if (!hasAllPermission) {
                menus = menus.stream()
                        .filter(menu -> menu.getPermission() == null || permissionCodes.contains(menu.getPermission()))
                        .collect(Collectors.toList());
            }
        }

        // 构建树形结构
        return buildMenuTree(menus, null);
    }

    /**
     * 根据权限编码分配权限
     */
    private void assignPermissionsByCode(String roleId, List<String> permissionCodes) {
        for (String code : permissionCodes) {
            LambdaQueryWrapper<IamPermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IamPermission::getCode, code);
            IamPermission permission = permissionMapper.selectOne(wrapper);
            if (permission != null) {
                IamRolePermission rolePermission = IamRolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permission.getId())
                        .build();
                rolePermissionMapper.insert(rolePermission);
            }
        }
    }

    /**
     * 构建菜单树
     */
    private List<MenuResponse> buildMenuTree(List<IamMenu> menus, String parentId) {
        List<MenuResponse> tree = new ArrayList<>();

        for (IamMenu menu : menus) {
            boolean isChild = (parentId == null && menu.getParentId() == null) ||
                    (parentId != null && parentId.equals(menu.getParentId()));

            if (isChild) {
                MenuResponse menuResponse = MenuResponse.builder()
                        .id(menu.getId())
                        .name(menu.getName())
                        .path(menu.getPath())
                        .component(menu.getComponent())
                        .icon(menu.getIcon())
                        .permission(menu.getPermission())
                        .sort(menu.getSort())
                        .children(buildMenuTree(menus, menu.getId()))
                        .build();
                tree.add(menuResponse);
            }
        }

        return tree;
    }
}
