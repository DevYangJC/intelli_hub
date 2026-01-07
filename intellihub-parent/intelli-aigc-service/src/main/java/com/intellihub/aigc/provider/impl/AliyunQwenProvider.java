package com.intellihub.aigc.provider.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.provider.ModelProviderService;
import com.intellihub.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 阿里通义千问模型提供商
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service("aliyunQwenProvider")
public class AliyunQwenProvider implements ModelProviderService {

    @Value("${aigc.aliyun.api-key:}")
    private String apiKey;

    // DashScope API Base URL（默认使用官方地址）
//    @Value("${aigc.aliyun.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;

    private static final List<String> SUPPORTED_MODELS = Arrays.asList(
            "qwen-turbo",
            "qwen-plus",
            "qwen-max",
            "qwen-max-longcontext"
    );

    @Override
    public TextGenerationResponse generateText(TextGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 设置DashScope API Base URL
            if (baseUrl != null && !baseUrl.isEmpty()) {
                Constants.baseHttpApiUrl = baseUrl;
            }
            
            Generation generation = new Generation();
            
            // 构建消息
            Message userMessage = Message.builder()
                    .role(Role.USER.getValue())
                    .content(request.getPrompt())
                    .build();

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .model(request.getModel())
                    .messages(Arrays.asList(userMessage))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .topP(request.getTopP().doubleValue())
                    .apiKey(apiKey)
                    .build();

            // 调用API
            GenerationResult result = generation.call(param);

            long duration = System.currentTimeMillis() - startTime;

            // 构建响应
            return TextGenerationResponse.builder()
                    .text(result.getOutput().getChoices().get(0).getMessage().getContent())
                    .tokensUsed(result.getUsage().getTotalTokens())
                    .model(request.getModel())
                    .requestId(result.getRequestId())
                    .duration(duration)
                    .finishReason(result.getOutput().getChoices().get(0).getFinishReason())
                    .build();

        } catch (NoApiKeyException e) {
            log.error("通义千问API Key未配置", e);
            throw new BusinessException("通义千问API Key未配置");
        } catch (InputRequiredException e) {
            log.error("通义千问请求参数错误", e);
            throw new BusinessException("请求参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用通义千问API失败", e);
            throw new BusinessException("调用AI模型失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 设置DashScope API Base URL
            if (baseUrl != null && !baseUrl.isEmpty()) {
                Constants.baseHttpApiUrl = baseUrl;
            }
            
            Generation generation = new Generation();

            // 构建消息列表
            List<Message> messages = new ArrayList<>();

            // 添加系统提示词
            if (request.getSystemPrompt() != null) {
                messages.add(Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(request.getSystemPrompt())
                        .build());
            }

            // 添加历史消息
            if (request.getHistory() != null) {
                for (ChatRequest.ChatMessage msg : request.getHistory()) {
                    messages.add(Message.builder()
                            .role(msg.getRole())
                            .content(msg.getContent())
                            .build());
                }
            }

            // 添加当前用户消息
            messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content(request.getMessage())
                    .build());

            // 构建请求参数
            GenerationParam param = GenerationParam.builder()
                    .model(request.getModel())
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .apiKey(apiKey)
                    .build();

            // 调用API
            GenerationResult result = generation.call(param);

            long duration = System.currentTimeMillis() - startTime;

            // 构建响应
            return ChatResponse.builder()
                    .message(result.getOutput().getChoices().get(0).getMessage().getContent())
                    .conversationId(request.getConversationId())
                    .tokensUsed(result.getUsage().getTotalTokens())
                    .model(request.getModel())
                    .requestId(result.getRequestId())
                    .duration(duration)
                    .finishReason(result.getOutput().getChoices().get(0).getFinishReason())
                    .build();

        } catch (NoApiKeyException e) {
            log.error("通义千问API Key未配置 - model: {}", request.getModel(), e);
            throw new BusinessException("通义千问API Key未配置");
        } catch (InputRequiredException e) {
            log.error("通义千问请求参数错误 - model: {}, message长度: {}, 错误: {}", 
                    request.getModel(), request.getMessage().length(), e.getMessage(), e);
            throw new BusinessException("请求参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用通义千问对话API失败 - model: {}, message长度: {}, 错误类型: {}, 错误: {}", 
                    request.getModel(), request.getMessage().length(), e.getClass().getName(), e.getMessage(), e);
            throw new BusinessException("调用AI对话失败: " + e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "阿里通义千问";
    }

    @Override
    public boolean supportsModel(String modelName) {
        return SUPPORTED_MODELS.contains(modelName);
    }
}
