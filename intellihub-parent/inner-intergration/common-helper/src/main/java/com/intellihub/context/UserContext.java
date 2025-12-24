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
