package com.intellihub.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户上下文
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long tenantId;
    private String userIdStr;
    private String tenantIdStr;
    private String username;
    private String token;
    
    /**
     * 用户角色列表（用于权限判断和租户隔离豁免）
     */
    private java.util.List<String> roles;
    
    /**
     * 是否忽略租户隔离
     * true: 不拼接 tenant_id 条件（系统管理员或特殊场景）
     * false: 自动拼接 tenant_id 条件（默认）
     */
    private boolean ignoreTenant = false;

    public static UserContext of(Long userId, Long tenantId, String username) {
        UserContext context = new UserContext();
        context.setUserId(userId);
        context.setTenantId(tenantId);
        context.setUserIdStr(userId != null ? String.valueOf(userId) : null);
        context.setTenantIdStr(tenantId != null ? String.valueOf(tenantId) : null);
        context.setUsername(username);
        return context;
    }

    public static UserContext of(String userId, String tenantId, String username) {
        UserContext context = new UserContext();
        context.setUserIdStr(userId);
        context.setTenantIdStr(tenantId);
        context.setUsername(username);
        return context;
    }
}
