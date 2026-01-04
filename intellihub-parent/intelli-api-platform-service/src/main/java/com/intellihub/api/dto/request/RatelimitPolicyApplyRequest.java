package com.intellihub.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 限流策略应用请求DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class RatelimitPolicyApplyRequest {

    @NotEmpty(message = "路由ID列表不能为空")
    private List<String> routeIds;
}
