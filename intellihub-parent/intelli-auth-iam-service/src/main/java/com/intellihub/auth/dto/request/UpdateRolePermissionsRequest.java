package com.intellihub.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新角色权限请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UpdateRolePermissionsRequest {

    @NotNull(message = "权限编码列表不能为空")
    private List<String> permissionCodes;
}
