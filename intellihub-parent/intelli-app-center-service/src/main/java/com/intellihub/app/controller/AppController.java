package com.intellihub.app.controller;

import com.intellihub.app.dto.request.*;
import com.intellihub.app.dto.response.*;
import com.intellihub.app.service.AppService;
import com.intellihub.ApiResponse;
import com.intellihub.context.UserContextHolder;
import com.intellihub.page.PageData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 应用管理控制器
 * <p>
 * 提供应用的创建、查询、更新、删除等接口
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    /**
     * 获取应用分页列表
     *
     * @param request 查询条件
     * @return 应用分页列表
     */
    @GetMapping("/list")
    public ApiResponse<PageData<AppInfoResponse>> listApps(AppQueryRequest request) {
        // 租户ID由多租户拦截器自动处理
        PageData<AppInfoResponse> pageData = appService.listApps(request);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取应用详情
     *
     * @param id 应用ID
     * @return 应用详情
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<AppInfoResponse> getAppDetail(@PathVariable String id) {
        AppInfoResponse app = appService.getAppById(id);
        return ApiResponse.success(app);
    }

    /**
     * 根据AppKey获取应用信息
     *
     * @param appKey AppKey
     * @return 应用详情
     */
    @GetMapping("/by-appkey")
    public ApiResponse<AppInfoResponse> getAppByAppKey(@RequestParam String appKey) {
        AppInfoResponse app = appService.getAppByAppKey(appKey);
        return ApiResponse.success(app);
    }

    /**
     * 根据AppKey获取应用内部信息（包含AppSecret）
     * <p>
     * 仅供内部服务调用，用于网关签名验证
     * </p>
     *
     * @param appKey AppKey
     * @return 应用内部信息
     */
    @GetMapping("/by-appkey/{appKey}")
    public ApiResponse<AppInternalResponse> getAppInternalByAppKey(@PathVariable String appKey) {
        AppInternalResponse app = appService.getAppInternalByAppKey(appKey);
        return ApiResponse.success(app);
    }

    /**
     * 创建应用
     *
     * @param request 创建请求
     * @return 创建结果，包含AppKey和AppSecret
     */
    @PostMapping("/create")
    public ApiResponse<AppCreateResponse> createApp(@Valid @RequestBody CreateAppRequest request) {
        // 租户ID由多租户拦截器自动处理
        String userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        AppCreateResponse app = appService.createApp(userId, username, request);
        return ApiResponse.success("创建成功，请妥善保管AppSecret", app);
    }

    /**
     * 更新应用信息
     *
     * @param id      应用ID
     * @param request 更新请求
     * @return 更新后的应用信息
     */
    @PostMapping("/{id}/update")
    public ApiResponse<AppInfoResponse> updateApp(
            @PathVariable String id,
            @Valid @RequestBody UpdateAppRequest request) {
        AppInfoResponse app = appService.updateApp(id, request);
        return ApiResponse.success("更新成功", app);
    }

    /**
     * 删除应用
     *
     * @param id 应用ID
     * @return 操作结果
     */
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> deleteApp(@PathVariable String id) {
        appService.deleteApp(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 启用应用
     *
     * @param id 应用ID
     * @return 操作结果
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enableApp(@PathVariable String id) {
        appService.enableApp(id);
        return ApiResponse.success("启用成功", null);
    }

    /**
     * 禁用应用
     *
     * @param id 应用ID
     * @return 操作结果
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disableApp(@PathVariable String id) {
        appService.disableApp(id);
        return ApiResponse.success("禁用成功", null);
    }

    /**
     * 重置AppSecret
     * <p>
     * 生成新的AppSecret，旧的立即失效
     * </p>
     *
     * @param id 应用ID
     * @return 新的AppSecret
     */
    @PostMapping("/{id}/reset-secret")
    public ApiResponse<String> resetAppSecret(@PathVariable String id) {
        String newSecret = appService.resetAppSecret(id);
        return ApiResponse.success("密钥重置成功，请妥善保管新密钥", newSecret);
    }

    /**
     * 订阅API
     *
     * @param id      应用ID
     * @param request 订阅请求
     * @return 订阅信息
     */
    @PostMapping("/{id}/subscribe")
    public ApiResponse<AppSubscriptionResponse> subscribeApi(
            @PathVariable String id,
            @Valid @RequestBody SubscribeApiRequest request) {
        AppSubscriptionResponse subscription = appService.subscribeApi(id, request);
        return ApiResponse.success("订阅成功", subscription);
    }

    /**
     * 取消订阅API
     *
     * @param id    应用ID
     * @param apiId API ID
     * @return 操作结果
     */
    @PostMapping("/{id}/unsubscribe")
    public ApiResponse<Void> unsubscribeApi(
            @PathVariable String id,
            @RequestParam String apiId) {
        appService.unsubscribeApi(id, apiId);
        return ApiResponse.success("取消订阅成功", null);
    }

    /**
     * 获取应用订阅的API列表
     *
     * @param id 应用ID
     * @return 订阅列表
     */
    @GetMapping("/{id}/subscriptions")
    public ApiResponse<List<AppSubscriptionResponse>> listSubscriptions(@PathVariable String id) {
        List<AppSubscriptionResponse> subscriptions = appService.listSubscriptions(id);
        return ApiResponse.success(subscriptions);
    }

    /**
     * 检查应用是否订阅了指定API（内部接口）
     *
     * @param appKey AppKey
     * @param apiId  API ID
     * @return 是否已订阅
     */
    @GetMapping("/check-subscription")
    public ApiResponse<Boolean> checkSubscription(
            @RequestParam String appKey,
            @RequestParam String apiId) {
        boolean subscribed = appService.checkSubscription(appKey, apiId);
        return ApiResponse.success(subscribed);
    }

    /**
     * 检查应用是否订阅了指定路径的API（内部接口，供网关调用）
     *
     * @param id   应用ID
     * @param path API路径
     * @return 是否已订阅
     */
    @GetMapping("/{id}/check-subscription")
    public ApiResponse<Boolean> checkSubscriptionByPath(
            @PathVariable String id,
            @RequestParam String path) {
        boolean subscribed = appService.checkSubscriptionByPath(id, path);
        return ApiResponse.success(subscribed);
    }

    /**
     * 验证AppKey和AppSecret（内部接口）
     *
     * @param appKey    AppKey
     * @param appSecret AppSecret
     * @return 是否验证通过
     */
    @PostMapping("/validate-credentials")
    public ApiResponse<Boolean> validateCredentials(
            @RequestParam String appKey,
            @RequestParam String appSecret) {
        boolean valid = appService.validateCredentials(appKey, appSecret);
        return ApiResponse.success(valid);
    }
}
