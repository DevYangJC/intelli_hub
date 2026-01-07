package com.intellihub.event.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.ApiResponse;
import com.intellihub.constants.ResultCode;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.mapper.EventSubscriptionMapper;
import com.intellihub.event.service.EventSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 事件订阅管理 API
 *
 * @author IntelliHub
 */
@RestController
@RequestMapping("/event/v1/event-subscriptions")
@RequiredArgsConstructor
public class EventSubscriptionController {

    private final EventSubscriptionService subscriptionService;
    private final EventSubscriptionMapper subscriptionMapper;

    @GetMapping("/list")
    public ApiResponse<Page<EventSubscription>> list(
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // 租户ID由多租户拦截器自动处理
        LambdaQueryWrapper<EventSubscription> queryWrapper = new LambdaQueryWrapper<>();
        if (eventCode != null) {
            queryWrapper.eq(EventSubscription::getEventCode, eventCode);
        }
        if (status != null) {
            queryWrapper.eq(EventSubscription::getStatus, status);
        }
        queryWrapper.orderByDesc(EventSubscription::getCreatedAt);

        Page<EventSubscription> pageResult = subscriptionMapper.selectPage(
                new Page<>(page, size), queryWrapper);
        return ApiResponse.success(pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventSubscription> getById(@PathVariable String id) {
        EventSubscription subscription = subscriptionService.getSubscription(id);
        if (subscription == null) {
            return ApiResponse.error(ResultCode.SUBSCRIPTION_NOT_FOUND);
        }
        return ApiResponse.success(subscription);
    }

    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody EventSubscription subscription) {
        String id = subscriptionService.createSubscription(subscription);
        return ApiResponse.success(id);
    }

    @PostMapping("/{id}/update")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody EventSubscription subscription) {
        subscription.setId(id);
        subscriptionService.updateSubscription(subscription);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        subscriptionService.deleteSubscription(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<Void> pause(@PathVariable String id) {
        subscriptionService.pauseSubscription(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/resume")
    public ApiResponse<Void> resume(@PathVariable String id) {
        subscriptionService.resumeSubscription(id);
        return ApiResponse.success();
    }
}
