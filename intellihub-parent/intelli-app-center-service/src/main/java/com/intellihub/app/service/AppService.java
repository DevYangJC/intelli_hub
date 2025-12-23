package com.intellihub.app.service;

import com.intellihub.app.dto.request.*;
import com.intellihub.app.dto.response.*;
import com.intellihub.page.PageData;

import java.util.List;

/**
 * 应用服务接口
 * <p>
 * 提供应用的创建、查询、更新、删除等功能
 * 以及AppKey管理和API订阅管理
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AppService {

    /**
     * 创建应用
     *
     * @param tenantId  租户ID
     * @param userId    创建人ID
     * @param username  创建人姓名
     * @param request   创建请求
     * @return 创建结果，包含AppKey和AppSecret
     */
    AppCreateResponse createApp(String tenantId, String userId, String username, CreateAppRequest request);

    /**
     * 分页查询应用列表
     *
     * @param tenantId 租户ID
     * @param request  查询条件
     * @return 应用分页列表
     */
    PageData<AppInfoResponse> listApps(String tenantId, AppQueryRequest request);

    /**
     * 获取应用详情
     *
     * @param appId 应用ID
     * @return 应用详情
     */
    AppInfoResponse getAppById(String appId);

    /**
     * 根据AppKey获取应用信息
     *
     * @param appKey AppKey
     * @return 应用详情
     */
    AppInfoResponse getAppByAppKey(String appKey);

    /**
     * 更新应用信息
     *
     * @param appId   应用ID
     * @param request 更新请求
     * @return 更新后的应用信息
     */
    AppInfoResponse updateApp(String appId, UpdateAppRequest request);

    /**
     * 删除应用
     *
     * @param appId 应用ID
     */
    void deleteApp(String appId);

    /**
     * 启用应用
     *
     * @param appId 应用ID
     */
    void enableApp(String appId);

    /**
     * 禁用应用
     *
     * @param appId 应用ID
     */
    void disableApp(String appId);

    /**
     * 重置AppSecret
     * <p>
     * 生成新的AppSecret，旧的立即失效
     * </p>
     *
     * @param appId 应用ID
     * @return 新的AppSecret
     */
    String resetAppSecret(String appId);

    /**
     * 订阅API
     *
     * @param appId   应用ID
     * @param request 订阅请求
     * @return 订阅信息
     */
    AppSubscriptionResponse subscribeApi(String appId, SubscribeApiRequest request);

    /**
     * 取消订阅API
     *
     * @param appId 应用ID
     * @param apiId API ID
     */
    void unsubscribeApi(String appId, String apiId);

    /**
     * 获取应用订阅的API列表
     *
     * @param appId 应用ID
     * @return 订阅列表
     */
    List<AppSubscriptionResponse> listSubscriptions(String appId);

    /**
     * 检查应用是否订阅了指定API
     *
     * @param appKey AppKey
     * @param apiId  API ID
     * @return 是否已订阅
     */
    boolean checkSubscription(String appKey, String apiId);

    /**
     * 验证AppKey和AppSecret
     *
     * @param appKey    AppKey
     * @param appSecret AppSecret
     * @return 是否验证通过
     */
    boolean validateCredentials(String appKey, String appSecret);

    /**
     * 根据AppKey获取应用内部信息（包含AppSecret）
     * <p>
     * 仅供内部服务调用，用于网关签名验证
     * </p>
     *
     * @param appKey AppKey
     * @return 应用内部信息
     */
    AppInternalResponse getAppInternalByAppKey(String appKey);

    /**
     * 检查应用是否订阅了指定路径的API
     * <p>
     * 通过API路径匹配检查订阅关系
     * </p>
     *
     * @param appId 应用ID
     * @param path  API路径
     * @return 是否已订阅
     */
    boolean checkSubscriptionByPath(String appId, String path);
}
