package com.intellihub.api.controller;

import com.intellihub.ApiResponse;
import com.intellihub.api.service.SysConfigService;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/platform/v1/settings")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping
    public ApiResponse<Map<String, Object>> getAllConfigs() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        return ApiResponse.success(sysConfigService.getAllConfigs(tenantId));
    }

    @GetMapping("/{key}")
    public ApiResponse<String> getConfig(@PathVariable String key) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String value = sysConfigService.getConfig(tenantId, key);
        return ApiResponse.success(value);
    }

    @PostMapping
    public ApiResponse<Void> batchSetConfigs(@RequestBody Map<String, Object> configs) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        sysConfigService.batchSetConfigs(tenantId, configs);
        return ApiResponse.success();
    }

    @PutMapping("/{key}")
    public ApiResponse<Void> setConfig(
            @PathVariable String key,
            @RequestBody ConfigRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        sysConfigService.setConfig(tenantId, key, request.getValue(), request.getType(), request.getDescription());
        return ApiResponse.success();
    }

    @DeleteMapping("/{key}")
    public ApiResponse<Void> deleteConfig(@PathVariable String key) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        sysConfigService.deleteConfig(tenantId, key);
        return ApiResponse.success();
    }

    @lombok.Data
    public static class ConfigRequest {
        private String value;
        private String type;
        private String description;
    }
}
