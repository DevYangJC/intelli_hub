package com.intellihub.auth.service;

import com.intellihub.auth.dto.request.AssignRolesRequest;
import com.intellihub.auth.dto.request.CreateUserRequest;
import com.intellihub.auth.dto.request.UpdateUserRequest;
import com.intellihub.auth.dto.request.UserQueryRequest;
import com.intellihub.auth.dto.response.UserInfoResponse;
import com.intellihub.page.PageData;

/**
 * 用户服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 分页查询用户
     * <p>租户ID由多租户拦截器自动处理</p>
     */
    PageData<UserInfoResponse> listUsers(UserQueryRequest request);

    /**
     * 创建用户
     * <p>租户ID由多租户拦截器自动处理</p>
     */
    UserInfoResponse createUser(CreateUserRequest request);

    /**
     * 获取用户详情
     */
    UserInfoResponse getUserById(String id);

    /**
     * 更新用户
     */
    UserInfoResponse updateUser(String id, UpdateUserRequest request);

    /**
     * 删除用户
     */
    void deleteUser(String id);

    /**
     * 启用用户
     */
    void enableUser(String id);

    /**
     * 禁用用户
     */
    void disableUser(String id);

    /**
     * 重置密码
     */
    String resetPassword(String id);

    /**
     * 分配角色
     */
    void assignRoles(String id, AssignRolesRequest request);
}
