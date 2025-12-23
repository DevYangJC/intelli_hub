package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * 更新角色请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UpdateRoleRequest {

    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 权限编码列表
     */
    private List<String> permissions;
}
