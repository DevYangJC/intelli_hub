package com.intellihub.context;

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

    public static Long getUserId() {
        UserContext context = get();
        return context != null ? context.getUserId() : null;
    }

    public static Long getTenantId() {
        UserContext context = get();
        return context != null ? context.getTenantId() : null;
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

    public static void clear() {
        CONTEXT.remove();
    }

    public static String getTenantIdStr() {
        UserContext context = get();
        return context != null ? context.getTenantIdStr() : null;
    }
}
