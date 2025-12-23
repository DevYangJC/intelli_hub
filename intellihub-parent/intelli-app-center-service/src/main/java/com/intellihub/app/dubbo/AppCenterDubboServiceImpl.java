package com.intellihub.app.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.app.entity.AppApiSubscription;
import com.intellihub.app.entity.AppInfo;
import com.intellihub.app.mapper.AppApiSubscriptionMapper;
import com.intellihub.app.mapper.AppInfoMapper;
import com.intellihub.common.dubbo.AppCenterDubboService;
import com.intellihub.common.dubbo.AppKeyInfoDTO;
import com.intellihub.common.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 应用中心Dubbo服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService
@RequiredArgsConstructor
public class AppCenterDubboServiceImpl implements AppCenterDubboService {

    private final AppInfoMapper appInfoMapper;
    private final AppApiSubscriptionMapper subscriptionMapper;

    @Override
    public AppKeyInfoDTO getAppKeyInfo(String appKey) {
        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getAppKey, appKey);
        AppInfo appInfo = appInfoMapper.selectOne(queryWrapper);
        
        if (appInfo == null) {
            log.debug("AppKey不存在: {}", appKey);
            return null;
        }
        
        return convertToDTO(appInfo);
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

    @Override
    public boolean validateCredentials(String appKey, String appSecret) {
        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getAppKey, appKey)
                .eq(AppInfo::getAppSecret, appSecret);
        AppInfo appInfo = appInfoMapper.selectOne(queryWrapper);
        
        if (appInfo == null) {
            return false;
        }
        
        // 检查状态
        if (!"active".equals(appInfo.getStatus())) {
            return false;
        }
        
        // 检查是否过期
        if (appInfo.getExpireTime() != null && appInfo.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean checkSubscriptionByApiId(String appId, String apiId) {
        log.debug("检查应用订阅关系 - appId: {}, apiId: {}", appId, apiId);
        
        LambdaQueryWrapper<AppApiSubscription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppApiSubscription::getAppId, appId)
                .eq(AppApiSubscription::getApiId, apiId)
                .eq(AppApiSubscription::getStatus, SubscriptionStatus.ACTIVE.getCode());
        
        Long count = subscriptionMapper.selectCount(queryWrapper);
        boolean hasSubscription = count != null && count > 0;
        
        log.debug("订阅检查结果 - appId: {}, apiId: {}, hasSubscription: {}", appId, apiId, hasSubscription);
        return hasSubscription;
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
                .replaceAll("\\{[^}]+\\}", "[^/]+")
                .replaceAll("\\*\\*", ".*")
                .replaceAll("\\*", "[^/]*");
        
        return requestPath.matches("^" + regex + "$");
    }

    private AppKeyInfoDTO convertToDTO(AppInfo appInfo) {
        AppKeyInfoDTO dto = new AppKeyInfoDTO();
        dto.setAppId(appInfo.getId());
        dto.setTenantId(appInfo.getTenantId());
        dto.setAppName(appInfo.getName());
        dto.setAppCode(appInfo.getCode());
        dto.setAppKey(appInfo.getAppKey());
        dto.setAppSecret(appInfo.getAppSecret());
        dto.setStatus(appInfo.getStatus());
        
        if (appInfo.getExpireTime() != null) {
            dto.setExpireTime(appInfo.getExpireTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }
        
        return dto;
    }
}
