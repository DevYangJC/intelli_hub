package com.intellihub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统核心角色枚举
 * <p>
 * 仅用于代码中判断核心角色，权限由数据库动态管理
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {

    /**
     * 超级管理员 - 跨租户全权限，不受租户隔离
     */
    PLATFORM_ADMIN("platform_admin", "超级管理员", false),

    /**
     * 租户管理员 - 本租户管理权限，受租户隔离
     */
    TENANT_ADMIN("tenant_admin", "租户管理员", true),

    /**
     * 普通用户 - 基础使用权限，受租户隔离
     */
    USER("user", "普通用户", true);

    /**
     * 角色代码（与数据库 iam_role.code 对应）
     */
    private final String code;

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 是否受租户隔离
     * <p>
     * false: 跨租户访问（如超级管理员）
     * true: 仅能访问本租户数据
     * </p>
     */
    private final boolean tenantIsolation;

    /**
     * 根据角色代码获取枚举
     *
     * @param code 角色代码
     * @return 角色枚举，未找到返回 null
     */
    public static RoleEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 判断是否为管理员角色（超级管理员或租户管理员）
     *
     * @return true 如果是管理员角色
     */
    public boolean isAdmin() {
        return this == PLATFORM_ADMIN || this == TENANT_ADMIN;
    }

    /**
     * 判断是否为超级管理员
     *
     * @return true 如果是超级管理员
     */
    public boolean isPlatformAdmin() {
        return this == PLATFORM_ADMIN;
    }

    /**
     * 判断是否为租户管理员
     *
     * @return true 如果是租户管理员
     */
    public boolean isTenantAdmin() {
        return this == TENANT_ADMIN;
    }

    /**
     * 判断是否可以跨租户访问
     *
     * @return true 如果可以跨租户访问
     */
    public boolean canCrossTenant() {
        return !tenantIsolation;
    }
}
