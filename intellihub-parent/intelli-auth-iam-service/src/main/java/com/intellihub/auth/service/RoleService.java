package com.intellihub.auth.service;

import com.intellihub.auth.dto.request.CreateRoleRequest;
import com.intellihub.auth.dto.request.UpdateRolePermissionsRequest;
import com.intellihub.auth.dto.request.UpdateRoleRequest;
import com.intellihub.auth.dto.response.MenuResponse;
import com.intellihub.auth.dto.response.PermissionResponse;
import com.intellihub.auth.dto.response.RoleResponse;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface RoleService {

    /**
     * 获取角色列表
     */
    List<RoleResponse> listRoles(String tenantId);

    /**
     * 创建角色
     */
    RoleResponse createRole(String tenantId, CreateRoleRequest request);

    /**
     * 更新角色
     */
    RoleResponse updateRole(String id, UpdateRoleRequest request);

    /**
     * 删除角色
     */
    void deleteRole(String id);

    /**
     * 获取角色权限
     */
    List<String> getRolePermissions(String id);

    /**
     * 更新角色权限
     */
    void updateRolePermissions(String id, UpdateRolePermissionsRequest request);

    /**
     * 获取所有权限列表
     */
    List<PermissionResponse> listPermissions();

    /**
     * 获取菜单树
     */
    List<MenuResponse> getMenuTree(String userId);
}
