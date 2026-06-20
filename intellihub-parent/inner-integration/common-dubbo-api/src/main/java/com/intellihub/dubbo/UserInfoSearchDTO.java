package com.intellihub.dubbo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 DTO（用于搜索索引同步）
 *
 * @author IntelliHub
 */
@Data
public class UserInfoSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tenantId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
