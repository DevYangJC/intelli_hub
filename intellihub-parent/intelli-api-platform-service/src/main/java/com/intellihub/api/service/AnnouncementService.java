package com.intellihub.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.api.dto.AnnouncementDTO;
import com.intellihub.api.entity.Announcement;
import com.intellihub.api.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public Page<AnnouncementDTO> list(String tenantId, int page, int size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        wrapper.eq(Announcement::getTenantId, tenantId)
               .orderByDesc(Announcement::getPublishTime);
        
        Page<Announcement> result = announcementMapper.selectPage(pageParam, wrapper);
        
        Page<AnnouncementDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    public List<AnnouncementDTO> getPublishedList(String tenantId, int limit) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        // 如果有租户ID则按租户筛选，否则返回所有已发布公告（公开访问场景）
        if (tenantId != null && !tenantId.isEmpty()) {
            wrapper.eq(Announcement::getTenantId, tenantId);
        }
        wrapper.eq(Announcement::getStatus, "published")
               .orderByDesc(Announcement::getPublishTime)
               .last("LIMIT " + limit);
        
        return announcementMapper.selectList(wrapper).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AnnouncementDTO getById(String id) {
        Announcement entity = announcementMapper.selectById(id);
        return entity != null ? toDTO(entity) : null;
    }

    public AnnouncementDTO create(String tenantId, AnnouncementDTO dto, String userId) {
        Announcement entity = new Announcement();
        BeanUtils.copyProperties(dto, entity);
        entity.setTenantId(tenantId);
        entity.setStatus("draft");
        entity.setCreatedBy(userId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setDeleted(0);
        
        if (dto.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
        
        announcementMapper.insert(entity);
        return toDTO(entity);
    }

    public AnnouncementDTO update(String id, AnnouncementDTO dto, String userId) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setMeta(dto.getMeta());
        entity.setPublishTime(dto.getPublishTime());
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());
        
        announcementMapper.updateById(entity);
        return toDTO(entity);
    }

    public boolean publish(String id, String userId) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            return false;
        }
        entity.setStatus("published");
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(entity);
        return true;
    }

    public boolean unpublish(String id, String userId) {
        Announcement entity = announcementMapper.selectById(id);
        if (entity == null) {
            return false;
        }
        entity.setStatus("draft");
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(entity);
        return true;
    }

    public boolean delete(String id) {
        return announcementMapper.deleteById(id) > 0;
    }

    private AnnouncementDTO toDTO(Announcement entity) {
        AnnouncementDTO dto = new AnnouncementDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
