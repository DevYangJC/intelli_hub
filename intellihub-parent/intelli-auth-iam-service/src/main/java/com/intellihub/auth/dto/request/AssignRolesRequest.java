package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 分配角色请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class AssignRolesRequest {

    @NotEmpty(message = "角色ID列表不能为空")
    private List<String> roleIds;
}
