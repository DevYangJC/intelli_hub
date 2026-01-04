package com.intellihub.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.ApiResponse;
import com.intellihub.api.constant.ApiConstants;
import com.intellihub.api.constant.ApiResponseMessage;
import com.intellihub.api.dto.AnnouncementDTO;
import com.intellihub.api.service.AnnouncementService;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 * 提供公告的增删改查、发布、下线等功能
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/platform/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页查询公告列表
     * 根据租户ID获取该租户下的所有公告，支持分页
     *
     * @param page 页码，默认第1页
     * @param size 每页大小，默认10条
     * @return 分页后的公告列表
     */
    @GetMapping
    public ApiResponse<Page<AnnouncementDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        return ApiResponse.success(announcementService.list(tenantId, page, size));
    }

    /**
     * 获取已发布的公告列表
     * 用于前台展示，只返回已发布状态的公告
     *
     * @param limit 返回数量限制，默认10条
     * @return 已发布的公告列表
     */
    @GetMapping("/published")
    public ApiResponse<List<AnnouncementDTO>> getPublished(
            @RequestParam(defaultValue = "10") int limit) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        return ApiResponse.success(announcementService.getPublishedList(tenantId, limit));
    }

    /**
     * 根据ID查询公告详情
     *
     * @param id 公告ID
     * @return 公告详细信息，如果不存在则返回404错误
     */
    @GetMapping("/{id}")
    public ApiResponse<AnnouncementDTO> getById(@PathVariable String id) {
        AnnouncementDTO dto = announcementService.getById(id);
        if (dto == null) {
            return ApiResponse.error(ApiConstants.HttpStatus.NOT_FOUND, ApiResponseMessage.ANNOUNCEMENT_NOT_FOUND);
        }
        return ApiResponse.success(dto);
    }

    /**
     * 创建新公告
     * 从上下文中获取租户ID和用户ID，创建公告草稿
     *
     * @param dto 公告信息
     * @return 创建后的公告信息
     */
    @PostMapping
    public ApiResponse<AnnouncementDTO> create(@RequestBody AnnouncementDTO dto) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        return ApiResponse.success(announcementService.create(tenantId, dto, userId));
    }

    /**
     * 更新公告信息
     * 只能更新草稿状态的公告，已发布的公告需要先下线
     *
     * @param id 公告ID
     * @param dto 更新的公告信息
     * @return 更新后的公告信息，如果不存在则返回404错误
     */
    @PutMapping("/{id}")
    public ApiResponse<AnnouncementDTO> update(@PathVariable String id, @RequestBody AnnouncementDTO dto) {
        String userId = UserContextHolder.getCurrentUserId();
        AnnouncementDTO result = announcementService.update(id, dto, userId);
        if (result == null) {
            return ApiResponse.error(ApiConstants.HttpStatus.NOT_FOUND, ApiResponseMessage.ANNOUNCEMENT_NOT_FOUND);
        }
        return ApiResponse.success(result);
    }

    /**
     * 发布公告
     * 将草稿状态的公告发布，发布后用户可见
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable String id) {
        String userId = UserContextHolder.getCurrentUserId();
        if (announcementService.publish(id, userId)) {
            return ApiResponse.success();
        }
        return ApiResponse.error(ApiConstants.HttpStatus.INTERNAL_SERVER_ERROR, ApiResponseMessage.PUBLISH_FAILED);
    }

    /**
     * 下线公告
     * 将已发布的公告下线，下线后用户不可见
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @PostMapping("/{id}/unpublish")
    public ApiResponse<Void> unpublish(@PathVariable String id) {
        String userId = UserContextHolder.getCurrentUserId();
        if (announcementService.unpublish(id, userId)) {
            return ApiResponse.success();
        }
        return ApiResponse.error(ApiConstants.HttpStatus.INTERNAL_SERVER_ERROR, ApiResponseMessage.UNPUBLISH_FAILED);
    }

    /**
     * 删除公告
     * 物理删除公告记录，谨慎操作
     *
     * @param id 公告ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        if (announcementService.delete(id)) {
            return ApiResponse.success();
        }
        return ApiResponse.error(ApiConstants.HttpStatus.INTERNAL_SERVER_ERROR, ApiResponseMessage.DELETE_FAILED);
    }
}
