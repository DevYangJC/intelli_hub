package com.intellihub.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Token校验响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyResponse {

    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 角色
     */
    private String role;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}
