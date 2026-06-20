package com.intellihub.dubbo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String tenantId;
    private String tenantCode;
    private java.util.List<String> roles;
    private java.util.List<String> permissions;
}
