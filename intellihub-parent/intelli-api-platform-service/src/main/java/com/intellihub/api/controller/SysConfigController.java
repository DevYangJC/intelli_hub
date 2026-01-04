package com.intellihub.api.controller;

import com.intellihub.ApiResponse;
import com.intellihub.api.service.SysConfigService;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置管理控制器
 * 提供租户级别的系统配置管理功能，支持配置的增删改查
 * 配置项支持多种类型（字符串、数字、布尔值等）
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/platform/v1/settings")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 获取当前租户的所有系统配置
     * 返回租户的所有配置项键值对，用于系统设置页面展示
     *
     * @return 所有配置项的键值对Map
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getAllConfigs() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        return ApiResponse.success(sysConfigService.getAllConfigs(tenantId));
    }

    /**
     * 根据配置键获取配置值
     * 查询指定配置项的值，如果配置不存在则返回null
     *
     * @param key 配置键
     * @return 配置值
     */
    @GetMapping("/{key}")
    public ApiResponse<String> getConfig(@PathVariable String key) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String value = sysConfigService.getConfig(tenantId, key);
        return ApiResponse.success(value);
    }

    /**
     * 批量设置系统配置
     * 一次性设置多个配置项，如果配置已存在则更新，不存在则创建
     * 适用于系统设置页面的批量保存场景
     *
     * @param configs 配置项键值对Map
     * @return 操作结果
     */
    @PostMapping
    public ApiResponse<Void> batchSetConfigs(@RequestBody Map<String, Object> configs) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        sysConfigService.batchSetConfigs(tenantId, configs);
        return ApiResponse.success();
    }

    /**
     * 设置单个系统配置
     * 设置指定配置项的值、类型和描述，如果配置已存在则更新，不存在则创建
     *
     * @param key 配置键
     * @param request 配置请求，包含配置值、类型、描述等
     * @return 操作结果
     */
    @PutMapping("/{key}")
    public ApiResponse<Void> setConfig(
            @PathVariable String key,
            @RequestBody ConfigRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        sysConfigService.setConfig(tenantId, key, request.getValue(), request.getType(), request.getDescription());
        return ApiResponse.success();
    }

    /**
     * 删除系统配置
     * 删除指定的配置项，删除后将使用系统默认值
     *
     * @param key 配置键
     * @return 操作结果
     */
    @DeleteMapping("/{key}")
    public ApiResponse<Void> deleteConfig(@PathVariable String key) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        sysConfigService.deleteConfig(tenantId, key);
        return ApiResponse.success();
    }

    /**
     * 配置请求对象
     * 用于接收配置设置请求的参数
     */
    @lombok.Data
    public static class ConfigRequest {
        /**
         * 配置值
         */
        private String value;
        
        /**
         * 配置类型（如：string、number、boolean等）
         */
        private String type;
        
        /**
         * 配置描述
         */
        private String description;
    }
}
