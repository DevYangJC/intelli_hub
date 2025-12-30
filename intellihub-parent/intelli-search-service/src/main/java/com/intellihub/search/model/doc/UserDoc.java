package com.intellihub.search.model.doc;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户索引文档
 *
 * @author IntelliHub
 */
@Data
@Accessors(chain = true)
public class UserDoc {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

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
     * 头像URL
     */
    private String avatar;

    /**
     * 状态（active/inactive/locked）
     */
    private String status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
