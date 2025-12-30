package com.intellihub.event.controller;

import com.intellihub.ApiResponse;
import com.intellihub.constants.ResultCode;
import com.intellihub.event.model.EventMessage;
import com.intellihub.event.service.EventPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 事件发布 API
 *
 * @author IntelliHub
 */
@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventPublishController {

    private final EventPublishService publishService;

    @PostMapping("/publish")
    public ApiResponse<String> publishEvent(@RequestBody EventMessage eventMessage) {
        boolean success = publishService.publish(eventMessage);
        if (success) {
            return ApiResponse.success(eventMessage.getEventId());
        } else {
            return ApiResponse.error(ResultCode.EVENT_PUBLISH_FAILED);
        }
    }

    @PostMapping("/publish-async")
    public ApiResponse<String> publishEventAsync(@RequestBody EventMessage eventMessage) {
        publishService.publishAsync(eventMessage);
        return ApiResponse.success(eventMessage.getEventId());
    }
}
