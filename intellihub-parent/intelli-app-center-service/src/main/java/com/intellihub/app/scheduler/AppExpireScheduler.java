package com.intellihub.app.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.app.entity.AppApiSubscription;
import com.intellihub.app.entity.AppInfo;
import com.intellihub.app.mapper.AppApiSubscriptionMapper;
import com.intellihub.app.mapper.AppInfoMapper;
import com.intellihub.enums.AppStatus;
import com.intellihub.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用和订阅过期状态处理定时任务
 * <p>
 * 功能：
 * 1. 每小时检查应用是否过期，自动更新状态为expired
 * 2. 每小时检查订阅是否过期，自动更新状态为expired
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppExpireScheduler {

    private final AppInfoMapper appInfoMapper;
    private final AppApiSubscriptionMapper subscriptionMapper;

    /**
     * 每小时检查应用过期状态
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkAppExpiration() {
        log.info("[过期检查] 开始检查应用过期状态");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询所有未删除且状态为active的应用
            LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AppInfo::getDeleted, 0)
                    .eq(AppInfo::getStatus, AppStatus.ACTIVE.getCode())
                    .isNotNull(AppInfo::getExpireTime)
                    .lt(AppInfo::getExpireTime, now);
            
            List<AppInfo> expiredApps = appInfoMapper.selectList(queryWrapper);
            
            int expiredCount = 0;
            for (AppInfo app : expiredApps) {
                app.setStatus(AppStatus.EXPIRED.getCode());
                app.setUpdatedAt(now);
                appInfoMapper.updateById(app);
                expiredCount++;
                
                log.info("[过期检查] 应用已过期 - AppId: {}, AppName: {}, ExpireTime: {}", 
                        app.getId(), app.getName(), app.getExpireTime());
            }
            
            log.info("[过期检查] 应用过期检查完成，共标记 {} 个应用为过期", expiredCount);
        } catch (Exception e) {
            log.error("[过期检查] 应用过期检查失败", e);
        }
    }
    
    /**
     * 每小时检查订阅过期状态
     */
    @Scheduled(cron = "0 15 * * * ?")
    public void checkSubscriptionExpiration() {
        log.info("[过期检查] 开始检查订阅过期状态");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询所有状态为active且已过期的订阅
            LambdaQueryWrapper<AppApiSubscription> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AppApiSubscription::getStatus, SubscriptionStatus.ACTIVE.getCode())
                    .isNotNull(AppApiSubscription::getExpireTime)
                    .lt(AppApiSubscription::getExpireTime, now);
            
            List<AppApiSubscription> expiredSubscriptions = subscriptionMapper.selectList(queryWrapper);
            
            int expiredCount = 0;
            for (AppApiSubscription subscription : expiredSubscriptions) {
                subscription.setStatus(SubscriptionStatus.EXPIRED.getCode());
                subscription.setUpdatedAt(now);
                subscriptionMapper.updateById(subscription);
                expiredCount++;
                
                log.info("[过期检查] 订阅已过期 - SubscriptionId: {}, AppId: {}, ApiId: {}, ExpireTime: {}", 
                        subscription.getId(), subscription.getAppId(), 
                        subscription.getApiId(), subscription.getExpireTime());
            }
            
            log.info("[过期检查] 订阅过期检查完成，共标记 {} 个订阅为过期", expiredCount);
        } catch (Exception e) {
            log.error("[过期检查] 订阅过期检查失败", e);
        }
    }
    
    /**
     * 每天凌晨1点检查订阅生效状态
     * 将到达生效时间但还未生效的订阅激活
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkSubscriptionEffective() {
        log.info("[生效检查] 开始检查订阅生效状态");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询所有状态为disabled且已到生效时间的订阅
            LambdaQueryWrapper<AppApiSubscription> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AppApiSubscription::getStatus, 0)
                    .isNotNull(AppApiSubscription::getEffectiveTime)
                    .le(AppApiSubscription::getEffectiveTime, now);
            
            List<AppApiSubscription> effectiveSubscriptions = subscriptionMapper.selectList(queryWrapper);
            
            int effectiveCount = 0;
            for (AppApiSubscription subscription : effectiveSubscriptions) {
                // 检查是否已过期
                if (subscription.getExpireTime() != null && 
                    subscription.getExpireTime().isBefore(now)) {
                    subscription.setStatus(SubscriptionStatus.EXPIRED.getCode());
                } else {
                    subscription.setStatus(SubscriptionStatus.ACTIVE.getCode());
                }
                subscription.setUpdatedAt(now);
                subscriptionMapper.updateById(subscription);
                effectiveCount++;
                
                log.info("[生效检查] 订阅已生效 - SubscriptionId: {}, AppId: {}, ApiId: {}, Status: {}", 
                        subscription.getId(), subscription.getAppId(), 
                        subscription.getApiId(), subscription.getStatus());
            }
            
            log.info("[生效检查] 订阅生效检查完成，共激活 {} 个订阅", effectiveCount);
        } catch (Exception e) {
            log.error("[生效检查] 订阅生效检查失败", e);
        }
    }
}

