package com.intellihub.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    /**
     * 角色ID
     */
    private String id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否系统内置
     */
    private Boolean isSystem;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 权限编码列表
     */
    private List<String> permissions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
