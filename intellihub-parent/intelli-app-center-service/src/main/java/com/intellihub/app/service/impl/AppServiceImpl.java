package com.intellihub.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.app.dto.request.*;
import com.intellihub.app.dto.response.*;
import com.intellihub.app.entity.AppApiSubscription;
import com.intellihub.app.entity.AppInfo;
import com.intellihub.app.mapper.AppApiSubscriptionMapper;
import com.intellihub.app.mapper.AppInfoMapper;
import com.intellihub.app.service.AppService;
import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.dubbo.ApiRouteDTO;
import com.intellihub.enums.AppStatus;
import com.intellihub.enums.AppType;
import com.intellihub.enums.SubscriptionStatus;
import com.intellihub.util.AppKeyGenerator;
import com.intellihub.constants.ResponseStatus;
import com.intellihub.exception.BusinessException;
import com.intellihub.page.PageData;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {

    private final AppInfoMapper appInfoMapper;
    private final AppApiSubscriptionMapper subscriptionMapper;
    
    @DubboReference(check = false, timeout = 5000)
    private ApiPlatformDubboService apiPlatformDubboService;

    @Override
    @Transactional
    public AppCreateResponse createApp(String userId, String username, CreateAppRequest request) {
        // 获取当前租户ID（用于日志和实体设置）
        String tenantId = UserContextHolder.getCurrentTenantId();
        
        // 检查应用编码是否已存在（租户条件由拦截器自动添加）
        LambdaQueryWrapper<AppInfo> codeQuery = new LambdaQueryWrapper<>();
        codeQuery.eq(AppInfo::getCode, request.getCode());
        if (appInfoMapper.selectCount(codeQuery) > 0) {
            throw new BusinessException(ResponseStatus.BAD_REQUEST.getCode(), "应用编码已存在");
        }

        // 生成AppKey和AppSecret
        String appKey = AppKeyGenerator.generateAppKey();
        String appSecret = AppKeyGenerator.generateAppSecret();

        // 创建应用实体
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(request, appInfo);
        // 设置租户ID（INSERT 时需要手动设置，拦截器只在 WHERE 条件添加）
        appInfo.setTenantId(tenantId);
        appInfo.setAppKey(appKey);
        appInfo.setAppSecret(appSecret);
        appInfo.setStatus(AppStatus.ACTIVE.getCode());
        appInfo.setQuotaLimit(request.getQuotaLimit() != null ? request.getQuotaLimit() : 10000L);
        appInfo.setQuotaUsed(0L);
        appInfo.setAppType(request.getAppType() != null ? AppType.fromCode(request.getAppType()).getCode() : AppType.EXTERNAL.getCode());
        appInfo.setCreatedBy(userId);
        appInfo.setCreatedByName(username);
        appInfo.setCreatedAt(LocalDateTime.now());
        appInfo.setUpdatedAt(LocalDateTime.now());
        appInfo.setDeleted(0);

        appInfoMapper.insert(appInfo);

        log.info("应用创建成功: appId={}, appKey={}, tenantId={}", appInfo.getId(), appKey, tenantId);

        // 构建响应
        AppCreateResponse response = new AppCreateResponse();
        response.setId(appInfo.getId());
        response.setName(appInfo.getName());
        response.setCode(appInfo.getCode());
        response.setAppKey(appKey);
        response.setAppSecret(appSecret);
        response.setStatus(appInfo.getStatus());
        response.setQuotaLimit(appInfo.getQuotaLimit());
        response.setExpireTime(appInfo.getExpireTime());
        response.setCreatedAt(appInfo.getCreatedAt());

        return response;
    }

    @Override
    public PageData<AppInfoResponse> listApps(AppQueryRequest request) {
        Page<AppInfo> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 租户条件由 MyBatis-Plus 多租户拦截器自动添加
        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getName())) {
            queryWrapper.like(AppInfo::getName, request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            queryWrapper.like(AppInfo::getCode, request.getCode());
        }
        if (StringUtils.hasText(request.getAppType())) {
            queryWrapper.eq(AppInfo::getAppType, request.getAppType());
        }
        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq(AppInfo::getStatus, request.getStatus());
        }

        queryWrapper.orderByDesc(AppInfo::getCreatedAt);

        Page<AppInfo> pageResult = appInfoMapper.selectPage(page, queryWrapper);

        List<AppInfoResponse> records = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageData<AppInfoResponse> pageData = new PageData<>(request.getPageNum(), request.getPageSize());
        pageData.setTotal(pageResult.getTotal());
        pageData.setRecords(records);
        return pageData;
    }

    @Override
    public AppInfoResponse getAppById(String appId) {
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }
        return convertToResponse(appInfo);
    }

    @Override
    public AppInfoResponse getAppByAppKey(String appKey) {
        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getAppKey, appKey);
        AppInfo appInfo = appInfoMapper.selectOne(queryWrapper);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }
        return convertToResponse(appInfo);
    }

    @Override
    @Transactional
    public AppInfoResponse updateApp(String appId, UpdateAppRequest request) {
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }

        // 更新非空字段
        if (StringUtils.hasText(request.getName())) {
            appInfo.setName(request.getName());
        }
        if (request.getDescription() != null) {
            appInfo.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getAppType())) {
            appInfo.setAppType(request.getAppType());
        }
        if (request.getQuotaLimit() != null) {
            appInfo.setQuotaLimit(request.getQuotaLimit());
        }
        if (request.getCallbackUrl() != null) {
            appInfo.setCallbackUrl(request.getCallbackUrl());
        }
        if (request.getIpWhitelist() != null) {
            appInfo.setIpWhitelist(request.getIpWhitelist());
        }
        if (request.getExpireTime() != null) {
            appInfo.setExpireTime(request.getExpireTime());
        }
        if (request.getContactName() != null) {
            appInfo.setContactName(request.getContactName());
        }
        if (request.getContactEmail() != null) {
            appInfo.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            appInfo.setContactPhone(request.getContactPhone());
        }

        appInfo.setUpdatedAt(LocalDateTime.now());
        appInfoMapper.updateById(appInfo);

        log.info("应用更新成功: appId={}", appId);
        return convertToResponse(appInfo);
    }

    @Override
    @Transactional
    public void deleteApp(String appId) {
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }

        // 删除订阅关系
        LambdaQueryWrapper<AppApiSubscription> subQuery = new LambdaQueryWrapper<>();
        subQuery.eq(AppApiSubscription::getAppId, appId);
        subscriptionMapper.delete(subQuery);

        // 删除应用（逻辑删除）
        appInfoMapper.deleteById(appId);

        log.info("应用删除成功: appId={}", appId);
    }

    @Override
    @Transactional
    public void enableApp(String appId) {
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }

        appInfo.setStatus(AppStatus.ACTIVE.getCode());
        appInfo.setUpdatedAt(LocalDateTime.now());
        appInfoMapper.updateById(appInfo);

        log.info("应用启用成功: appId={}", appId);
    }

    @Override
    @Transactional
    public void disableApp(String appId) {
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }

        appInfo.setStatus(AppStatus.DISABLED.getCode());
        appInfo.setUpdatedAt(LocalDateTime.now());
        appInfoMapper.updateById(appInfo);

        log.info("应用禁用成功: appId={}", appId);
    }

    @Override
    @Transactional
    public String resetAppSecret(String appId) {
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }

        String newSecret = AppKeyGenerator.generateAppSecret();
        appInfo.setAppSecret(newSecret);
        appInfo.setUpdatedAt(LocalDateTime.now());
        appInfoMapper.updateById(appInfo);

        log.info("应用密钥重置成功: appId={}", appId);
        return newSecret;
    }

    @Override
    @Transactional
    public AppSubscriptionResponse subscribeApi(String appId, SubscribeApiRequest request) {
        // 检查应用是否存在
        AppInfo appInfo = appInfoMapper.selectById(appId);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }

        // 检查是否已订阅
        LambdaQueryWrapper<AppApiSubscription> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(AppApiSubscription::getAppId, appId)
                .eq(AppApiSubscription::getApiId, request.getApiId());
        if (subscriptionMapper.selectCount(existQuery) > 0) {
            throw new BusinessException(ResponseStatus.BAD_REQUEST.getCode(), "已订阅该API");
        }

        // 从 API 平台服务获取 API 信息
        ApiRouteDTO apiRoute = null;
        try {
            apiRoute = apiPlatformDubboService.getRouteByApiId(request.getApiId());
        } catch (Exception e) {
            log.warn("获取API信息失败: apiId={}", request.getApiId(), e);
        }

        // 创建订阅
        AppApiSubscription subscription = new AppApiSubscription();
        subscription.setAppId(appId);
        subscription.setApiId(request.getApiId());
        
        // 保存 API 名称和路径
        if (apiRoute != null) {
            subscription.setApiName(apiRoute.getApiName());
            subscription.setApiPath(apiRoute.getPath());
        }
        
        subscription.setStatus(SubscriptionStatus.ACTIVE.getCode());
        subscription.setQuotaLimit(request.getQuotaLimit());
        subscription.setEffectiveTime(LocalDateTime.now());
        subscription.setExpireTime(request.getExpireTime());
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        subscriptionMapper.insert(subscription);

        log.info("API订阅成功: appId={}, apiId={}, apiName={}, apiPath={}", 
                appId, request.getApiId(), subscription.getApiName(), subscription.getApiPath());

        return convertToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public void unsubscribeApi(String appId, String apiId) {
        LambdaQueryWrapper<AppApiSubscription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppApiSubscription::getAppId, appId)
                .eq(AppApiSubscription::getApiId, apiId);

        int deleted = subscriptionMapper.delete(queryWrapper);
        if (deleted == 0) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "订阅关系不存在");
        }

        log.info("API取消订阅成功: appId={}, apiId={}", appId, apiId);
    }

    @Override
    public List<AppSubscriptionResponse> listSubscriptions(String appId) {
        LambdaQueryWrapper<AppApiSubscription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppApiSubscription::getAppId, appId)
                .orderByDesc(AppApiSubscription::getCreatedAt);

        List<AppApiSubscription> subscriptions = subscriptionMapper.selectList(queryWrapper);

        return subscriptions.stream()
                .map(this::convertToSubscriptionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkSubscription(String appKey, String apiId) {
        // 先根据appKey查找应用
        LambdaQueryWrapper<AppInfo> appQuery = new LambdaQueryWrapper<>();
        appQuery.eq(AppInfo::getAppKey, appKey)
                .eq(AppInfo::getStatus, AppStatus.ACTIVE);
        AppInfo appInfo = appInfoMapper.selectOne(appQuery);

        if (appInfo == null) {
            return false;
        }

        // 检查订阅关系
        LambdaQueryWrapper<AppApiSubscription> subQuery = new LambdaQueryWrapper<>();
        subQuery.eq(AppApiSubscription::getAppId, appInfo.getId())
                .eq(AppApiSubscription::getApiId, apiId)
                .eq(AppApiSubscription::getStatus, SubscriptionStatus.ACTIVE.getCode());

        return subscriptionMapper.selectCount(subQuery) > 0;
    }

    @Override
    public boolean validateCredentials(String appKey, String appSecret) {
        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getAppKey, appKey)
                .eq(AppInfo::getAppSecret, appSecret)
                .eq(AppInfo::getStatus, AppStatus.ACTIVE);

        AppInfo appInfo = appInfoMapper.selectOne(queryWrapper);

        if (appInfo == null) {
            return false;
        }

        // 检查是否过期
        if (appInfo.getExpireTime() != null && appInfo.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    @Override
    public AppInternalResponse getAppInternalByAppKey(String appKey) {
        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getAppKey, appKey);
        AppInfo appInfo = appInfoMapper.selectOne(queryWrapper);
        if (appInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "应用不存在");
        }
        return convertToInternalResponse(appInfo);
    }

    @Override
    public boolean checkSubscriptionByPath(String appId, String path) {
        // 查询应用的所有订阅
        LambdaQueryWrapper<AppApiSubscription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppApiSubscription::getAppId, appId)
                .eq(AppApiSubscription::getStatus, SubscriptionStatus.ACTIVE.getCode());
        
        List<AppApiSubscription> subscriptions = subscriptionMapper.selectList(queryWrapper);
        
        if (subscriptions.isEmpty()) {
            return false;
        }
        
        // 检查是否有匹配的API路径
        for (AppApiSubscription sub : subscriptions) {
            if (matchPath(path, sub.getApiPath())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 路径匹配（支持通配符）
     */
    private boolean matchPath(String requestPath, String apiPath) {
        if (apiPath == null || requestPath == null) {
            return false;
        }
        
        // 精确匹配
        if (requestPath.equals(apiPath)) {
            return true;
        }
        
        // 路径参数匹配，如 /api/users/{id} 匹配 /api/users/123
        String regex = apiPath
                .replaceAll("\\{[^}]+\\}", "[^/]+")  // {id} -> [^/]+
                .replaceAll("\\*\\*", ".*")          // ** -> .*
                .replaceAll("\\*", "[^/]*");         // * -> [^/]*
        
        return requestPath.matches("^" + regex + "$");
    }

    /**
     * 转换为内部响应DTO（包含AppSecret）
     */
    private AppInternalResponse convertToInternalResponse(AppInfo appInfo) {
        AppInternalResponse response = new AppInternalResponse();
        response.setId(appInfo.getId());
        response.setTenantId(appInfo.getTenantId());
        response.setName(appInfo.getName());
        response.setCode(appInfo.getCode());
        response.setAppKey(appInfo.getAppKey());
        response.setAppSecret(appInfo.getAppSecret());
        response.setStatus(appInfo.getStatus());
        response.setExpireTime(appInfo.getExpireTime());
        return response;
    }

    /**
     * 转换为响应DTO
     */
    private AppInfoResponse convertToResponse(AppInfo appInfo) {
        AppInfoResponse response = new AppInfoResponse();
        BeanUtils.copyProperties(appInfo, response);

        // 计算配额使用百分比
        if (appInfo.getQuotaLimit() != null && appInfo.getQuotaLimit() > 0) {
            long used = appInfo.getQuotaUsed() != null ? appInfo.getQuotaUsed() : 0;
            response.setQuotaUsagePercent((double) used / appInfo.getQuotaLimit() * 100);
        }

        // 查询订阅的API数量
        LambdaQueryWrapper<AppApiSubscription> subQuery = new LambdaQueryWrapper<>();
        subQuery.eq(AppApiSubscription::getAppId, appInfo.getId());
        int count = Math.toIntExact(subscriptionMapper.selectCount(subQuery));
        response.setSubscribedApiCount(count);

        return response;
    }

    /**
     * 转换为订阅响应DTO
     */
    private AppSubscriptionResponse convertToSubscriptionResponse(AppApiSubscription subscription) {
        AppSubscriptionResponse response = new AppSubscriptionResponse();
        BeanUtils.copyProperties(subscription, response);
        return response;
    }
}
