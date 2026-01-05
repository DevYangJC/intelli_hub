package com.intellihub.auth.controller;

import com.intellihub.auth.dto.request.CreateRoleRequest;
import com.intellihub.auth.dto.request.UpdateRolePermissionsRequest;
import com.intellihub.auth.dto.request.UpdateRoleRequest;
import com.intellihub.auth.dto.response.MenuResponse;
import com.intellihub.auth.dto.response.PermissionResponse;
import com.intellihub.auth.dto.response.RoleResponse;
import com.intellihub.auth.service.RoleService;
import com.intellihub.auth.util.JwtUtil;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色权限管理控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/iam/v1")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final JwtUtil jwtUtil;

    /**
     * 获取角色列表
     */
    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles() {
        // 租户ID由多租户拦截器自动处理
        List<RoleResponse> roles = roleService.listRoles();
        return ApiResponse.success(roles);
    }

    /**
     * 创建角色
     */
    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        // 租户ID由多租户拦截器自动处理
        RoleResponse role = roleService.createRole(request);
        return ApiResponse.success("创建成功", role);
    }

    /**
     * 更新角色
     */
    @PutMapping("/roles/{id}")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse role = roleService.updateRole(id, request);
        return ApiResponse.success("更新成功", role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/roles/{id}/permissions")
    public ApiResponse<List<String>> getRolePermissions(@PathVariable String id) {
        List<String> permissions = roleService.getRolePermissions(id);
        return ApiResponse.success(permissions);
    }

    /**
     * 更新角色权限
     */
    @PutMapping("/roles/{id}/permissions")
    public ApiResponse<Void> updateRolePermissions(
            @PathVariable String id,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        roleService.updateRolePermissions(id, request);
        return ApiResponse.success("权限更新成功", null);
    }

    /**
     * 获取所有权限列表
     */
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        List<PermissionResponse> permissions = roleService.listPermissions();
        return ApiResponse.success(permissions);
    }

    /**
     * 获取菜单树
     */
    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> getMenuTree(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                userId = jwtUtil.getUserId(authorization.substring(7));
            } catch (Exception e) {
                // 忽略Token解析错误
            }
        }
        List<MenuResponse> menus = roleService.getMenuTree(userId);
        return ApiResponse.success(menus);
    }
}
