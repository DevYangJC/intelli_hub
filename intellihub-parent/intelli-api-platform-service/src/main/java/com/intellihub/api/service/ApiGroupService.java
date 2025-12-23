package com.intellihub.api.service;

import com.intellihub.api.dto.response.ApiGroupResponse;

import java.util.List;

/**
 * API分组服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiGroupService {

    /**
     * 获取分组列表
     */
    List<ApiGroupResponse> listGroups(String tenantId);

    /**
     * 获取分组详情
     */
    ApiGroupResponse getGroupById(String id);

    /**
     * 创建分组
     */
    ApiGroupResponse createGroup(String tenantId, String userId, String name, String code, String description, String color, String status);

    /**
     * 更新分组
     */
    ApiGroupResponse updateGroup(String id, String name, String description, Integer sort, String color, String status);

    /**
     * 删除分组
     */
    void deleteGroup(String id);
}
