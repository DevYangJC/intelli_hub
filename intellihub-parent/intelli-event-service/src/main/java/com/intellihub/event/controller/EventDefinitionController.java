package com.intellihub.event.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.ApiResponse;
import com.intellihub.constants.ResultCode;
import com.intellihub.event.constant.EventStatus;
import com.intellihub.event.entity.EventDefinition;
import com.intellihub.event.mapper.EventDefinitionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 事件定义管理 API
 *
 * @author IntelliHub
 */
@RestController
@RequestMapping("/event/v1/event-definitions")
@RequiredArgsConstructor
public class EventDefinitionController {

    private final EventDefinitionMapper eventDefinitionMapper;

    @GetMapping("/list")
    public ApiResponse<Page<EventDefinition>> list(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // 租户ID由多租户拦截器自动处理
        LambdaQueryWrapper<EventDefinition> queryWrapper = new LambdaQueryWrapper<>();
        if (eventType != null) {
            queryWrapper.eq(EventDefinition::getEventType, eventType);
        }
        if (status != null) {
            queryWrapper.eq(EventDefinition::getStatus, status);
        }
        queryWrapper.orderByDesc(EventDefinition::getCreatedAt);

        Page<EventDefinition> pageResult = eventDefinitionMapper.selectPage(
                new Page<>(page, size), queryWrapper);
        return ApiResponse.success(pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDefinition> getById(@PathVariable String id) {
        EventDefinition definition = eventDefinitionMapper.selectById(id);
        if (definition == null) {
            return ApiResponse.error(ResultCode.EVENT_NOT_FOUND);
        }
        return ApiResponse.success(definition);
    }

    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody EventDefinition definition) {
        definition.setStatus(EventStatus.ACTIVE.getCode());
        definition.setCreatedAt(LocalDateTime.now());
        definition.setUpdatedAt(LocalDateTime.now());
        eventDefinitionMapper.insert(definition);
        return ApiResponse.success(definition.getId());
    }

    @PostMapping("/{id}/update")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody EventDefinition definition) {
        definition.setId(id);
        definition.setUpdatedAt(LocalDateTime.now());
        eventDefinitionMapper.updateById(definition);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        eventDefinitionMapper.deleteById(id);
        return ApiResponse.success();
    }
}
