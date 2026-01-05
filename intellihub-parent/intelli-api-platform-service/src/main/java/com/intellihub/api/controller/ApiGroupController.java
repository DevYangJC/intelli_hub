package com.intellihub.api.controller;

import com.intellihub.api.constant.ApiResponseMessage;
import com.intellihub.context.UserContextHolder;
import com.intellihub.api.dto.response.ApiGroupResponse;
import com.intellihub.api.service.ApiGroupService;
import com.intellihub.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API分组控制器
 * 提供API分组的管理功能，用于组织和分类API
 * 支持分组的增删改查、排序、颜色标记等功能
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
     * 获取当前租户的所有API分组列表
     * 返回按排序号排列的分组列表，用于分组选择器和导航菜单
     *
     * @return 分组列表
     */
    @GetMapping("/list")
    public ApiResponse<List<ApiGroupResponse>> listGroups() {
        // 租户ID由多租户拦截器自动处理
        List<ApiGroupResponse> groups = apiGroupService.listGroups();
        return ApiResponse.success(groups);
    }

    /**
     * 根据ID获取分组详情
     * 返回分组的完整信息，包括名称、编码、描述、颜色、状态等
     *
     * @param id 分组ID
     * @return 分组详细信息
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ApiGroupResponse> getGroupById(@PathVariable String id) {
        ApiGroupResponse group = apiGroupService.getGroupById(id);
        return ApiResponse.success(group);
    }

    /**
     * 创建新分组
     * 创建一个新的API分组，用于组织和分类API
     * 分组名称和编码在同一租户下必须唯一
     *
     * @param request 创建请求，包含分组名称、编码、描述、颜色、状态等
     * @return 创建成功后返回分组详细信息
     */
    @PostMapping("/create")
    public ApiResponse<ApiGroupResponse> createGroup(@RequestBody Map<String, String> request) {
        // 租户ID由多租户拦截器自动处理
        String userId = UserContextHolder.getCurrentUserId();
        ApiGroupResponse group = apiGroupService.createGroup(
                userId,
                request.get("name"),
                request.get("code"),
                request.get("description"),
                request.get("color"),
                request.get("status")
        );
        return ApiResponse.success(ApiResponseMessage.CREATE_SUCCESS, group);
    }

    /**
     * 更新分组信息
     * 修改分组的名称、描述、排序、颜色、状态等信息
     * 更新后影响该分组下的所有API
     *
     * @param id 分组ID
     * @param request 更新请求，包含需要修改的分组信息
     * @return 更新成功后返回分组详细信息
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
        return ApiResponse.success(ApiResponseMessage.UPDATE_SUCCESS, group);
    }

    /**
     * 删除分组
     * 删除前会检查分组下是否有API，如有则不允许删除
     * 需要先将分组下的API移动到其他分组或删除
     *
     * @param id 分组ID
     * @return 操作结果
     */
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> deleteGroup(@PathVariable String id) {
        apiGroupService.deleteGroup(id);
        return ApiResponse.success(ApiResponseMessage.DELETE_SUCCESS, null);
    }
}
