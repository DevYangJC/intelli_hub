package com.intellihub.auth.controller;

import com.intellihub.auth.dto.request.AssignRolesRequest;
import com.intellihub.auth.dto.request.CreateUserRequest;
import com.intellihub.auth.dto.request.UpdateUserRequest;
import com.intellihub.auth.dto.request.UserQueryRequest;
import com.intellihub.auth.dto.response.UserInfoResponse;
import com.intellihub.auth.service.UserService;
import com.intellihub.ApiResponse;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/iam/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping
    public ApiResponse<PageData<UserInfoResponse>> listUsers(UserQueryRequest request) {
        // 租户ID由多租户拦截器自动处理
        PageData<UserInfoResponse> pageData = userService.listUsers(request);
        return ApiResponse.success(pageData);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<UserInfoResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 租户ID由多租户拦截器自动处理
        UserInfoResponse user = userService.createUser(request);
        return ApiResponse.success("创建成功", user);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserInfoResponse> getUserById(@PathVariable String id) {
        UserInfoResponse user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<UserInfoResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserInfoResponse user = userService.updateUser(id, request);
        return ApiResponse.success("更新成功", user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enableUser(@PathVariable String id) {
        userService.enableUser(id);
        return ApiResponse.success("启用成功", null);
    }

    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disableUser(@PathVariable String id) {
        userService.disableUser(id);
        return ApiResponse.success("禁用成功", null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Map<String, String>> resetPassword(@PathVariable String id) {
        String newPassword = userService.resetPassword(id);
        Map<String, String> result = new HashMap<>();
        result.put("newPassword", newPassword);
        return ApiResponse.success("密码重置成功", result);
    }

    /**
     * 分配角色
     */
    @PostMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(
            @PathVariable String id,
            @Valid @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request);
        return ApiResponse.success("角色分配成功", null);
    }
}
