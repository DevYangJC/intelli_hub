package com.intellihub.api.controller;

import com.intellihub.common.context.UserContextHolder;
import com.intellihub.api.dto.response.ApiGroupResponse;
import com.intellihub.api.service.ApiGroupService;
import com.intellihub.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API分组控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/api-groups")
@RequiredArgsConstructor
public class ApiGroupController {

    private final ApiGroupService apiGroupService;

    /**
     * 获取分组列表
     */
    @GetMapping("/list")
    public ApiResponse<List<ApiGroupResponse>> listGroups() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        List<ApiGroupResponse> groups = apiGroupService.listGroups(tenantId);
        return ApiResponse.success(groups);
    }

    /**
     * 获取分组详情
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ApiGroupResponse> getGroupById(@PathVariable String id) {
        ApiGroupResponse group = apiGroupService.getGroupById(id);
        return ApiResponse.success(group);
    }

    /**
     * 创建分组
     */
    @PostMapping("/create")
    public ApiResponse<ApiGroupResponse> createGroup(@RequestBody Map<String, String> request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        ApiGroupResponse group = apiGroupService.createGroup(
                tenantId,
                userId,
                request.get("name"),
                request.get("code"),
                request.get("description"),
                request.get("color"),
                request.get("status")
        );
        return ApiResponse.success("创建成功", group);
    }

    /**
     * 更新分组
     */
    @PostMapping("/{id}/update")
    public ApiResponse<ApiGroupResponse> updateGroup(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        ApiGroupResponse group = apiGroupService.updateGroup(
                id,
                (String) request.get("name"),
                (String) request.get("description"),
                request.get("sort") != null ? ((Number) request.get("sort")).intValue() : null,
                (String) request.get("color"),
                (String) request.get("status")
        );
        return ApiResponse.success("更新成功", group);
    }

    /**
     * 删除分组
     */
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> deleteGroup(@PathVariable String id) {
        apiGroupService.deleteGroup(id);
        return ApiResponse.success("删除成功", null);
    }
}
