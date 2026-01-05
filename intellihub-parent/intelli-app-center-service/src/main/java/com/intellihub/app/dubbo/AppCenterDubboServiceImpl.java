package com.intellihub.app.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.app.entity.AppApiSubscription;
import com.intellihub.app.entity.AppInfo;
import com.intellihub.app.mapper.AppApiSubscriptionMapper;
import com.intellihub.app.mapper.AppInfoMapper;
import com.intellihub.dubbo.AppCallCountDTO;
import com.intellihub.dubbo.AppCenterDubboService;
import com.intellihub.dubbo.AppInfoDTO;
import com.intellihub.dubbo.AppKeyInfoDTO;
import com.intellihub.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .eq(AppApiSubscription::getStatus, SubscriptionStatus.ACTIVE);
        
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

    /**
     * 批量更新App配额使用
     * <p>
     * 根据传入的调用统计列表，更新 app_info 表的 quota_used 字段
     * </p>
     *
     * @param callCounts App调用次数统计列表
     * @return 更新成功的记录数
     */
    @Override
    public int batchUpdateAppQuotaUsed(List<AppCallCountDTO> callCounts) {
        if (callCounts == null || callCounts.isEmpty()) {
            log.info("[配额同步] 无需更新的App配额");
            return 0;
        }

        log.info("[配额同步] 开始批量更新App配额使用，共 {} 条", callCounts.size());
        int successCount = 0;

        for (AppCallCountDTO dto : callCounts) {
            try {
                // 查询App是否存在
                AppInfo appInfo = appInfoMapper.selectById(dto.getAppId());
                if (appInfo == null) {
                    log.warn("[配额同步] App不存在，跳过: appId={}", dto.getAppId());
                    continue;
                }

                // 更新配额使用
                appInfo.setQuotaUsed(dto.getQuotaUsed() != null ? dto.getQuotaUsed() : 0L);
                
                int affected = appInfoMapper.updateById(appInfo);
                if (affected > 0) {
                    successCount++;
                    log.debug("[配额同步] App配额更新成功: appId={}, quotaUsed={}", 
                            dto.getAppId(), dto.getQuotaUsed());
                }
            } catch (Exception e) {
                log.error("[配额同步] 更新App配额失败: appId={}", dto.getAppId(), e);
            }
        }

        log.info("[配额同步] App配额批量更新完成，成功 {} / {} 条", successCount, callCounts.size());
        return successCount;
    }

    @Override
    public List<AppInfoDTO> getAllAppInfoForSync(String tenantId) {
        log.info("[搜索同步] 获取所有应用信息，tenantId={}", tenantId);

        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getDeleted, 0);
        if (tenantId != null && !tenantId.isEmpty()) {
            queryWrapper.eq(AppInfo::getTenantId, tenantId);
        }

        List<AppInfo> appList = appInfoMapper.selectList(queryWrapper);
        List<AppInfoDTO> result = appList.stream()
                .map(this::convertToAppInfoDTO)
                .collect(Collectors.toList());

        log.info("[搜索同步] 获取应用信息完成，共 {} 条", result.size());
        return result;
    }

    @Override
    public List<AppInfoDTO> getAppInfoUpdatedAfter(String tenantId, LocalDateTime lastSyncTime) {
        log.info("[搜索同步] 获取增量应用信息，tenantId={}, lastSyncTime={}", tenantId, lastSyncTime);

        LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppInfo::getDeleted, 0);
        if (tenantId != null && !tenantId.isEmpty()) {
            queryWrapper.eq(AppInfo::getTenantId, tenantId);
        }
        if (lastSyncTime != null) {
            queryWrapper.gt(AppInfo::getUpdatedAt, lastSyncTime);
        }

        List<AppInfo> appList = appInfoMapper.selectList(queryWrapper);
        List<AppInfoDTO> result = appList.stream()
                .map(this::convertToAppInfoDTO)
                .collect(Collectors.toList());

        log.info("[搜索同步] 获取增量应用信息完成，共 {} 条", result.size());
        return result;
    }

    private AppInfoDTO convertToAppInfoDTO(AppInfo appInfo) {
        AppInfoDTO dto = new AppInfoDTO();
        dto.setId(appInfo.getId());
        dto.setTenantId(appInfo.getTenantId());
        dto.setName(appInfo.getName());
        dto.setCode(appInfo.getCode());
        dto.setDescription(appInfo.getDescription());
        dto.setAppType(appInfo.getAppType());
        dto.setAppKey(appInfo.getAppKey());
        dto.setStatus(appInfo.getStatus());
        dto.setContactName(appInfo.getContactName());
        dto.setContactEmail(appInfo.getContactEmail());
        dto.setCreatedBy(appInfo.getCreatedBy());
        dto.setCreatedByName(appInfo.getCreatedByName());
        dto.setCreatedAt(appInfo.getCreatedAt());
        dto.setUpdatedAt(appInfo.getUpdatedAt());
        return dto;
    }

    @Override
    public int countAppsByTenantId(String tenantId) {
        log.debug("[统计] 查询租户应用数量，tenantId={}", tenantId);
        
        // 使用跳过租户拦截器的方法，避免 SQL 被自动拼接 tenant_id 条件
        int result = appInfoMapper.countByTenantIdIgnoreTenant(tenantId);
        
        log.debug("[统计] 租户应用数量: tenantId={}, count={}", tenantId, result);
        return result;
    }
}
