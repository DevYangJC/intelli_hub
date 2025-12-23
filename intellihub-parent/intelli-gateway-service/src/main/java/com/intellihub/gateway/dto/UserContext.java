package com.intellihub.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户上下文信息
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContext {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 当前租户ID
     */
    private String tenantId;

    /**
     * 当前租户编码
     */
    private String tenantCode;

    /**
     * 角色（单个角色）
     */
    private String role;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 是否有效（用于网关判断）
     */
    private Boolean valid;
    
    /**
     * 过期时间
     */
    private String expiresAt;
}