package com.intellihub.app.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.app.entity.AppInfo;
import com.intellihub.app.mapper.AppInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配额重置定时任务
 * <p>
 * 功能：
 * 1. 每天凌晨0点重置每日配额
 * 2. 每小时同步Redis配额到MySQL
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaResetScheduler {

    private final AppInfoMapper appInfoMapper;
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * 每天凌晨0点重置配额
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyQuota() {
        log.info("[配额重置] 开始重置每日配额");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 1. 重置MySQL中的配额
            LambdaQueryWrapper<AppInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AppInfo::getDeleted, 0);
            
            List<AppInfo> apps = appInfoMapper.selectList(queryWrapper);
            
            int resetCount = 0;
            for (AppInfo app : apps) {
                app.setQuotaUsed(0L);
                app.setQuotaResetTime(now);
                appInfoMapper.updateById(app);
                resetCount++;
            }
            
            // 2. 清空Redis中的配额计数
            String pattern = "app:quota:*";
            Long deletedCount = redisTemplate.keys(pattern)
                    .flatMap(key -> redisTemplate.delete(key))
                    .reduce(0L, Long::sum)
                    .block();
            
            log.info("[配额重置] 配额重置完成，MySQL重置 {} 个应用，Redis删除 {} 个key", 
                    resetCount, deletedCount);
        } catch (Exception e) {
            log.error("[配额重置] 配额重置失败", e);
        }
    }
    
    /**
     * 每小时同步Redis配额到MySQL
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncQuotaToDatabase() {
        log.info("[配额同步] 开始同步Redis配额到MySQL");
        
        try {
            String pattern = "app:quota:*";
            
            List<QuotaPair> quotaList = redisTemplate.keys(pattern)
                    .flatMap(key -> 
                        redisTemplate.opsForValue().get(key)
                            .map(value -> {
                                String appId = key.replace("app:quota:", "");
                                return new QuotaPair(appId, Long.parseLong(value));
                            })
                    )
                    .collectList()
                    .block();
            
            if (quotaList == null || quotaList.isEmpty()) {
                log.info("[配额同步] 没有需要同步的配额数据");
                return;
            }
            
            int syncCount = 0;
            for (QuotaPair pair : quotaList) {
                AppInfo app = appInfoMapper.selectById(pair.getAppId());
                if (app != null) {
                    app.setQuotaUsed(pair.getQuotaUsed());
                    appInfoMapper.updateById(app);
                    syncCount++;
                }
            }
            
            log.info("[配额同步] 同步完成，共同步 {} 个应用", syncCount);
        } catch (Exception e) {
            log.error("[配额同步] 同步失败", e);
        }
    }
    
    /**
     * 配额数据对
     */
    private static class QuotaPair {
        private final String appId;
        private final Long quotaUsed;
        
        public QuotaPair(String appId, Long quotaUsed) {
            this.appId = appId;
            this.quotaUsed = quotaUsed;
        }
        
        public String getAppId() {
            return appId;
        }
        
        public Long getQuotaUsed() {
            return quotaUsed;
        }
    }
}

