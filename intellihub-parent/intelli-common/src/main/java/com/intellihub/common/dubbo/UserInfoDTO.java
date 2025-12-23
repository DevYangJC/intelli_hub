package com.intellihub.common.dubbo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户信息 DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 状态
     */
    private Integer status;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;
}
