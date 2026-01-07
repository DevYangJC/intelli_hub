package com.intellihub.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.aigc.entity.AigcQuotaConfig;
import com.intellihub.aigc.entity.AigcRequestLog;
import com.intellihub.aigc.mapper.AigcQuotaConfigMapper;
import com.intellihub.aigc.mapper.AigcRequestLogMapper;
import com.intellihub.aigc.service.QuotaService;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 配额管理服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final AigcQuotaConfigMapper quotaConfigMapper;
    private final AigcRequestLogMapper requestLogMapper;

    @Override
    public boolean checkQuota(String tenantId, int tokensNeeded) {
        AigcQuotaConfig config = getOrCreateQuotaConfig(tenantId);

        // 检查是否需要重置配额
        resetQuotaIfNeeded(config);

        // 检查配额是否充足
        long remaining = config.getDailyQuota() - config.getUsedQuota();
        if (remaining < tokensNeeded) {
            log.warn("租户配额不足 - tenantId: {}, 剩余: {}, 需要: {}", tenantId, remaining, tokensNeeded);
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public void deductQuota(String tenantId, int tokensUsed) {
        AigcQuotaConfig config = getOrCreateQuotaConfig(tenantId);

        // 检查是否需要重置配额
        resetQuotaIfNeeded(config);

        // 扣减配额
        config.setUsedQuota(config.getUsedQuota() + tokensUsed);
        quotaConfigMapper.updateById(config);

        log.info("配额扣减成功 - tenantId: {}, 扣减: {}, 剩余: {}",
                tenantId, tokensUsed, config.getDailyQuota() - config.getUsedQuota());
    }

    @Override
    @Async
    public void recordRequestLog(AigcRequestLog aigcRequestLog) {
        try {
            requestLogMapper.insert(aigcRequestLog);
            log.info("请求日志记录成功 - requestId: {}, model: {}, tokens: {}",
                    aigcRequestLog.getRequestId(), aigcRequestLog.getModelName(), aigcRequestLog.getTokensUsed());
        } catch (Exception e) {
            log.error("请求日志记录失败", e);
        }
    }

    @Override
    public Map<String, Object> getQuotaUsage(String tenantId) {
        AigcQuotaConfig config = getOrCreateQuotaConfig(tenantId);

        // 检查是否需要重置配额
        resetQuotaIfNeeded(config);

        Map<String, Object> usage = new HashMap<>();
        usage.put("tenantId", tenantId);
        usage.put("dailyQuota", config.getDailyQuota());
        usage.put("usedQuota", config.getUsedQuota());
        usage.put("remainingQuota", config.getDailyQuota() - config.getUsedQuota());
        usage.put("usagePercent", (config.getUsedQuota() * 100.0) / config.getDailyQuota());
        usage.put("totalCost", config.getTotalCost());
        usage.put("resetTime", config.getResetTime());

        return usage;
    }

    /**
     * 获取或创建配额配置
     */
    private AigcQuotaConfig getOrCreateQuotaConfig(String tenantId) {
        LambdaQueryWrapper<AigcQuotaConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcQuotaConfig::getTenantId, tenantId);
        AigcQuotaConfig config = quotaConfigMapper.selectOne(wrapper);

        if (config == null) {
            // 创建默认配额配置（每日10万Token）
            config = new AigcQuotaConfig();
            config.setTenantId(tenantId);
            config.setDailyQuota(100000L);
            config.setUsedQuota(0L);
            config.setTotalCost(BigDecimal.ZERO);
            config.setResetTime(getNextResetTime());
            quotaConfigMapper.insert(config);
            log.info("创建默认配额配置 - tenantId: {}, dailyQuota: {}", tenantId, config.getDailyQuota());
        }

        return config;
    }

    /**
     * 检查并重置配额
     */
    private void resetQuotaIfNeeded(AigcQuotaConfig config) {
        if (config.getResetTime() == null || LocalDateTime.now().isAfter(config.getResetTime())) {
            config.setUsedQuota(0L);
            config.setResetTime(getNextResetTime());
            quotaConfigMapper.updateById(config);
            log.info("配额已重置 - tenantId: {}, 下次重置时间: {}", config.getTenantId(), config.getResetTime());
        }
    }

    /**
     * 获取下次重置时间（次日凌晨）
     */
    private LocalDateTime getNextResetTime() {
        return LocalDateTime.now().plusDays(1).with(LocalTime.MIN);
    }
}
