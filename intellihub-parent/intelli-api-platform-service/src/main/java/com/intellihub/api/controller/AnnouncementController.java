package com.intellihub.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.ApiResponse;
import com.intellihub.api.dto.AnnouncementDTO;
import com.intellihub.api.service.AnnouncementService;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platform/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ApiResponse<Page<AnnouncementDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        return ApiResponse.success(announcementService.list(tenantId, page, size));
    }

    @GetMapping("/published")
    public ApiResponse<List<AnnouncementDTO>> getPublished(
            @RequestParam(defaultValue = "10") int limit) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        return ApiResponse.success(announcementService.getPublishedList(tenantId, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnouncementDTO> getById(@PathVariable String id) {
        AnnouncementDTO dto = announcementService.getById(id);
        if (dto == null) {
            return ApiResponse.error(404, "公告不存在");
        }
        return ApiResponse.success(dto);
    }

    @PostMapping
    public ApiResponse<AnnouncementDTO> create(@RequestBody AnnouncementDTO dto) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        return ApiResponse.success(announcementService.create(tenantId, dto, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnnouncementDTO> update(@PathVariable String id, @RequestBody AnnouncementDTO dto) {
        String userId = UserContextHolder.getCurrentUserId();
        AnnouncementDTO result = announcementService.update(id, dto, userId);
        if (result == null) {
            return ApiResponse.error(404, "公告不存在");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable String id) {
        String userId = UserContextHolder.getCurrentUserId();
        if (announcementService.publish(id, userId)) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "发布失败");
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<Void> unpublish(@PathVariable String id) {
        String userId = UserContextHolder.getCurrentUserId();
        if (announcementService.unpublish(id, userId)) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "下线失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        if (announcementService.delete(id)) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "删除失败");
    }
}
