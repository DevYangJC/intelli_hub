package com.intellihub.aigc.service.impl;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.entity.AigcRequestLog;
import com.intellihub.aigc.enums.RequestStatus;
import com.intellihub.aigc.monitor.PerformanceMonitor;
import com.intellihub.aigc.provider.ModelProviderService;
import com.intellihub.aigc.service.AigcService;
import com.intellihub.aigc.service.QuotaService;
import com.intellihub.context.UserContextHolder;
import com.intellihub.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * AIGC服务实现类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
public class AigcServiceImpl implements AigcService {

    @Autowired
    @Qualifier("aliyunQwenProvider")
    private ModelProviderService aliyunQwenProvider;

    @Autowired
    @Qualifier("baiduErnieProvider")
    private ModelProviderService baiduErnieProvider;

    @Autowired
    @Qualifier("tencentHunyuanProvider")
    private ModelProviderService tencentHunyuanProvider;

    @Autowired
    private QuotaService quotaService;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    @Override
    public TextGenerationResponse generateText(TextGenerationRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();

        log.info("文本生成请求 - 租户: {}, 用户: {}, 模型: {}, Prompt长度: {}",
                tenantId, userId, request.getModel(), request.getPrompt().length());

        // 检查配额
        if (!quotaService.checkQuota(tenantId, request.getMaxTokens())) {
            throw new BusinessException("配额不足，请联系管理员");
        }

        // 选择合适的Provider
        ModelProviderService provider = selectProvider(request.getProvider(), request.getModel());

        TextGenerationResponse response = null;
        AigcRequestLog requestLog = null;
        long startTime = System.currentTimeMillis();

        try {
            // 调用生成
            response = provider.generateText(request);

            // 扣减配额
            quotaService.deductQuota(tenantId, response.getTokensUsed());

            // 记录性能监控
            long costMs = System.currentTimeMillis() - startTime;
            performanceMonitor.recordRequest(request.getModel(), true, costMs, response.getTokensUsed());

            // 构建请求日志
            requestLog = AigcRequestLog.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .modelName(request.getModel())
                    .provider(provider.getProviderName())
                    .prompt(request.getPrompt())
                    .response(response.getText())
                    .tokensUsed(response.getTokensUsed())
                    .cost(calculateCost(request.getModel(), response.getTokensUsed()))
                    .duration(response.getDuration())
                    .status(RequestStatus.SUCCESS.getCode())
                    .requestId(response.getRequestId())
                    .build();

            log.info("文本生成完成 - 模型: {}, Token消耗: {}, 耗时: {}ms",
                    response.getModel(), response.getTokensUsed(), response.getDuration());

        } catch (Exception e) {
            // 记录性能监控（失败）
            long costMs = System.currentTimeMillis() - startTime;
            performanceMonitor.recordRequest(request.getModel(), false, costMs, 0);

            // 记录失败日志
            requestLog = AigcRequestLog.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .modelName(request.getModel())
                    .provider(provider.getProviderName())
                    .prompt(request.getPrompt())
                    .status(RequestStatus.FAILED.getCode())
                    .errorMessage(e.getMessage())
                    .build();
            throw e;
        } finally {
            // 异步记录日志
            if (requestLog != null) {
                quotaService.recordRequestLog(requestLog);
            }
        }

        return response;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();

        log.info("对话请求 - 租户: {}, 用户: {}, 模型: {}, 消息: {}",
                tenantId, userId, request.getModel(), request.getMessage());

        // 检查配额
        if (!quotaService.checkQuota(tenantId, request.getMaxTokens())) {
            throw new BusinessException("配额不足，请联系管理员");
        }

        // 选择合适的Provider
        ModelProviderService provider = selectProvider(request.getProvider(), request.getModel());

        ChatResponse response = null;
        AigcRequestLog requestLog = null;
        long startTime = System.currentTimeMillis();

        try {
            // 调用对话
            response = provider.chat(request);

            // 扣减配额
            quotaService.deductQuota(tenantId, response.getTokensUsed());

            // 记录性能监控
            long costMs = System.currentTimeMillis() - startTime;
            performanceMonitor.recordRequest(request.getModel(), true, costMs, response.getTokensUsed());

            // 构建请求日志
            requestLog = AigcRequestLog.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .modelName(request.getModel())
                    .provider(provider.getProviderName())
                    .prompt(request.getMessage())
                    .response(response.getMessage())
                    .tokensUsed(response.getTokensUsed())
                    .cost(calculateCost(request.getModel(), response.getTokensUsed()))
                    .duration(response.getDuration())
                    .status(RequestStatus.SUCCESS.getCode())
                    .requestId(response.getRequestId())
                    .build();

            log.info("对话完成 - 模型: {}, Token消耗: {}, 耗时: {}ms",
                    response.getModel(), response.getTokensUsed(), response.getDuration());

        } catch (Exception e) {
            // 记录性能监控（失败）
            long costMs = System.currentTimeMillis() - startTime;
            performanceMonitor.recordRequest(request.getModel(), false, costMs, 0);

            // 记录失败日志
            requestLog = AigcRequestLog.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .modelName(request.getModel())
                    .provider(provider.getProviderName())
                    .prompt(request.getMessage())
                    .status(RequestStatus.FAILED.getCode())
                    .errorMessage(e.getMessage())
                    .build();
            throw e;
        } finally {
            // 异步记录日志
            if (requestLog != null) {
                quotaService.recordRequestLog(requestLog);
            }
        }

        return response;
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList(
                // 阿里通义千问
                "qwen-turbo",
                "qwen-plus",
                "qwen-max",
                "qwen-max-longcontext",
                // 百度文心一言（2025最新模型）
                "ernie-3.5-8k",
                "ernie-3.5-128k",
                "ernie-4.0-8k",
                "ernie-4.0-turbo-8k",
                "ernie-speed-8k",
                "ernie-lite-8k",
                // 腾讯混元
                "hunyuan-lite",
                "hunyuan-standard",
                "hunyuan-standard-256K",
                "hunyuan-pro",
                "hunyuan-turbo",
                "hunyuan-turbo-latest"
        );
    }

    @Override
    public List<com.intellihub.aigc.dto.response.ModelInfo> getModelInfoList() {
        return Arrays.asList(
                // 阿里通义千问
                buildModelInfo("qwen-turbo", "通义千问 Turbo", "aliyun", "阿里云", "高性价比，适合一般场景", 8000, true, 0.002),
                buildModelInfo("qwen-plus", "通义千问 Plus", "aliyun", "阿里云", "能力均衡，适合复杂任务", 32000, true, 0.004),
                buildModelInfo("qwen-max", "通义千问 Max", "aliyun", "阿里云", "顶级能力，适合专业场景", 32000, true, 0.012),
                buildModelInfo("qwen-max-longcontext", "通义千问 Max 长文本", "aliyun", "阿里云", "支持超长上下文", 128000, true, 0.012),
                // 百度文心一言
                buildModelInfo("ernie-3.5-8k", "文心 3.5 (8K)", "baidu", "百度", "ERNIE 3.5版本，8K上下文", 8000, true, 0.002),
                buildModelInfo("ernie-3.5-128k", "文心 3.5 (128K)", "baidu", "百度", "ERNIE 3.5版本，128K长上下文", 128000, true, 0.004),
                buildModelInfo("ernie-4.0-8k", "文心 4.0 (8K)", "baidu", "百度", "ERNIE 4.0旗舰版", 8000, true, 0.012),
                buildModelInfo("ernie-4.0-turbo-8k", "文心 4.0 Turbo", "baidu", "百度", "ERNIE 4.0 Turbo高速版", 8000, true, 0.008),
                buildModelInfo("ernie-speed-8k", "文心 Speed", "baidu", "百度", "高速推理，低延迟", 8000, true, 0.001),
                buildModelInfo("ernie-lite-8k", "文心 Lite", "baidu", "百度", "轻量版，成本最低", 8000, true, 0.0005),
                // 腾讯混元
                buildModelInfo("hunyuan-lite", "混元 Lite", "tencent", "腾讯", "轻量版，适合简单任务", 4000, true, 0.001),
                buildModelInfo("hunyuan-standard", "混元 Standard", "tencent", "腾讯", "标准版，能力均衡", 32000, true, 0.004),
                buildModelInfo("hunyuan-standard-256K", "混元 Standard (256K)", "tencent", "腾讯", "超长上下文版本", 256000, true, 0.006),
                buildModelInfo("hunyuan-pro", "混元 Pro", "tencent", "腾讯", "专业版，能力更强", 32000, true, 0.01),
                buildModelInfo("hunyuan-turbo", "混元 Turbo", "tencent", "腾讯", "高速版，低延迟", 32000, true, 0.008),
                buildModelInfo("hunyuan-turbo-latest", "混元 Turbo Latest", "tencent", "腾讯", "最新Turbo版本", 32000, true, 0.008)
        );
    }

    private com.intellihub.aigc.dto.response.ModelInfo buildModelInfo(
            String id, String name, String provider, String providerName,
            String description, Integer maxContextLength, Boolean supportStream, Double price) {
        return com.intellihub.aigc.dto.response.ModelInfo.builder()
                .id(id)
                .name(name)
                .provider(provider)
                .providerName(providerName)
                .description(description)
                .maxContextLength(maxContextLength)
                .supportStream(supportStream)
                .pricePerThousandTokens(price)
                .build();
    }

    /**
     * 根据指定的Provider名称或模型名称选择Provider
     */
    private ModelProviderService selectProvider(String providerName, String modelName) {
        // 如果指定了Provider，直接使用
        if (providerName != null && !providerName.isEmpty()) {
            switch (providerName) {
                case "aliyunQwenProvider":
                    return aliyunQwenProvider;
                case "baiduErnieProvider":
                    return baiduErnieProvider;
                case "tencentHunyuanProvider":
                    return tencentHunyuanProvider;
                default:
                    throw new BusinessException("不支持的Provider: " + providerName);
            }
        }

        // 否则根据模型名称自动选择
        // 阿里通义千问
        if (aliyunQwenProvider.supportsModel(modelName)) {
            return aliyunQwenProvider;
        }

        // 百度文心一言
        if (baiduErnieProvider.supportsModel(modelName)) {
            return baiduErnieProvider;
        }

        // 腾讯混元
        if (tencentHunyuanProvider.supportsModel(modelName)) {
            return tencentHunyuanProvider;
        }

        throw new BusinessException("不支持的模型: " + modelName);
    }

    /**
     * 计算成本（简化版本，实际应根据各厂商定价）
     */
    private BigDecimal calculateCost(String modelName, int tokensUsed) {
        // 示例：每1000 token 价格
        double pricePerThousand = 0.002; // 0.002元/1000 token

        if (modelName.contains("max") || modelName.contains("pro") || modelName.contains("4")) {
            pricePerThousand = 0.01; // 高级模型更贵
        }

        double cost = (tokensUsed / 1000.0) * pricePerThousand;
        return BigDecimal.valueOf(cost).setScale(4, BigDecimal.ROUND_HALF_UP);
    }
}
