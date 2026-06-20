package com.intellihub.context;

import com.intellihub.enums.RoleEnum;

/**
 * 用户上下文持有者
 *
 * @author intellihub
 * @since 1.0.0
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }



    public static String getUsername() {
        UserContext context = get();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前用户ID（String类型）
     */
    public static String getCurrentUserId() {
        UserContext context = get();
        if (context == null) {
            return null;
        }
        if (context.getUserIdStr() != null) {
            return context.getUserIdStr();
        }
        return context.getUserId() != null ? String.valueOf(context.getUserId()) : null;
    }

    /**
     * 设置当前租户ID（用于异步消息处理或登录等特殊场景）
     */
    public static void setCurrentTenantId(String tenantId) {
        UserContext context = getOrCreate();
        context.setTenantIdStr(tenantId);
    }

    /**
     * 获取当前租户ID（String类型）
     */
    public static String getCurrentTenantId() {
        UserContext context = get();
        if (context == null) {
            return null;
        }
        if (context.getTenantIdStr() != null) {
            return context.getTenantIdStr();
        }
        return context.getTenantId() != null ? String.valueOf(context.getTenantId()) : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        return getUsername();
    }

    /**
     * 设置用户角色列表
     */
    public static void setRoles(java.util.List<String> roles) {
        UserContext context = getOrCreate();
        context.setRoles(roles);
    }

    /**
     * 获取用户角色列表
     */
    public static java.util.List<String> getRoles() {
        UserContext context = get();
        return context != null ? context.getRoles() : null;
    }

    /**
     * 判断是否包含指定角色（字符串方式）
     */
    public static boolean hasRole(String role) {
        java.util.List<String> roles = getRoles();
        return roles != null && roles.contains(role);
    }

    /**
     * 判断是否包含指定角色（枚举方式）
     */
    public static boolean hasRole(RoleEnum role) {
        return role != null && hasRole(role.getCode());
    }

    /**
     * 获取当前用户的主要角色枚举
     * 返回第一个匹配的核心角色
     */
    public static RoleEnum getPrimaryRole() {
        java.util.List<String> roles = getRoles();
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        // 按优先级返回：超级管理员 > 租户管理员 > 普通用户
        if (roles.contains(RoleEnum.PLATFORM_ADMIN.getCode())) {
            return RoleEnum.PLATFORM_ADMIN;
        }
        if (roles.contains(RoleEnum.TENANT_ADMIN.getCode())) {
            return RoleEnum.TENANT_ADMIN;
        }
        if (roles.contains(RoleEnum.USER.getCode())) {
            return RoleEnum.USER;
        }
        return null;
    }

    /**
     * 判断是否系统管理员（兼容旧代码）
     */
    public static boolean isSystemAdmin() {
        return hasRole("SYSTEM_ADMIN");
    }

    /**
     * 判断是否超级管理员（跨租户全权限）
     */
    public static boolean isPlatformAdmin() {
        return hasRole(RoleEnum.PLATFORM_ADMIN);
    }

    /**
     * 判断是否租户管理员
     */
    public static boolean isTenantAdmin() {
        return hasRole(RoleEnum.TENANT_ADMIN);
    }

    /**
     * 判断是否普通用户
     */
    public static boolean isNormalUser() {
        return hasRole(RoleEnum.USER);
    }

    /**
     * 判断是否为管理员（超级管理员或租户管理员）
     */
    public static boolean isAdmin() {
        return isPlatformAdmin() || isTenantAdmin();
    }

    /**
     * 判断当前用户是否可以跨租户访问
     */
    public static boolean canCrossTenant() {
        RoleEnum role = getPrimaryRole();
        return role != null && role.canCrossTenant();
    }

    /**
     * 设置是否忽略租户隔离
     */
    public static void setIgnoreTenant(boolean ignore) {
        UserContext context = getOrCreate();
        context.setIgnoreTenant(ignore);
    }

    /**
     * 判断是否忽略租户隔离
     */
    public static boolean isIgnoreTenant() {
        UserContext context = get();
        return context != null && context.isIgnoreTenant();
    }

    /**
     * 获取或创建上下文
     */
    private static UserContext getOrCreate() {
        UserContext context = get();
        if (context == null) {
            context = new UserContext();
            set(context);
        }
        return context;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
