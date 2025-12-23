package com.intellihub.common.context;

import com.intellihub.common.dto.UserContext;

import java.util.Optional;

/**
 * 用户上下文持有者
 * 用于从请求中获取当前登录用户信息
 *
 * @author intellihub
 * @since 1.0.0
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> USER_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户上下文
     */
    public static void setUserContext(UserContext userContext) {
        USER_CONTEXT_THREAD_LOCAL.set(userContext);
    }

    /**
     * 获取用户上下文
     */
    public static UserContext getUserContext() {
        return USER_CONTEXT_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        UserContext context = getUserContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserContext context = getUserContext();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前租户ID
     */
    public static String getCurrentTenantId() {
        UserContext context = getUserContext();
        return context != null ? context.getTenantId() : null;
    }

    /**
     * 获取当前租户编码
     */
    public static String getCurrentTenantCode() {
        UserContext context = getUserContext();
        return context != null ? context.getTenantCode() : null;
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin() {
        UserContext context = getUserContext();
        return context != null && Boolean.TRUE.equals(context.getIsAdmin());
    }

    /**
     * 获取用户信息（可能为空）
     */
    public static Optional<UserContext> getCurrentUser() {
        return Optional.ofNullable(getUserContext());
    }

    /**
     * 清除用户上下文
     */
    public static void clear() {
        USER_CONTEXT_THREAD_LOCAL.remove();
    }
}