package com.intellihub.aigc.provider.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.provider.ModelProviderService;
import com.intellihub.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * 百度文心一言模型提供商
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service("baiduErnieProvider")
public class BaiduErnieProvider implements ModelProviderService {

    @Value("${aigc.baidu.api-key:}")
    private String apiKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)  // AI响应可能较慢
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    // 千帆平台v2 API地址（2025最新版本）
    private static final String CHAT_API_URL = "https://qianfan.baidubce.com/v2/chat/completions";

    private static final List<String> SUPPORTED_MODELS = Arrays.asList(
            "ernie-3.5-8k",
            "ernie-3.5-128k",
            "ernie-4.0-8k",
            "ernie-4.0-turbo-8k",
            "ernie-speed-8k",
            "ernie-lite-8k"
    );

    @Override
    public TextGenerationResponse generateText(TextGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("user", request.getPrompt()));
            requestBody.put("messages", messages);
            
            if (request.getTemperature() != null) {
                requestBody.put("temperature", request.getTemperature());
            }
            if (request.getTopP() != null) {
                requestBody.put("top_p", request.getTopP());
            }

            String jsonBody = gson.toJson(requestBody);

            // 发送请求（使用新的Bearer Token认证）
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request httpRequest = new Request.Builder()
                    .url(CHAT_API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.error("文心一言文本生成API调用失败: code={}, errorBody={}", response.code(), errorBody);
                    throw new BusinessException("文心一言API调用失败[" + response.code() + "]: " + errorBody);
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                // 解析响应（新API返回格式）
                JsonObject firstChoice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                JsonObject usage = jsonResponse.getAsJsonObject("usage");

                long duration = System.currentTimeMillis() - startTime;

                return TextGenerationResponse.builder()
                        .text(message.get("content").getAsString())
                        .tokensUsed(usage.get("total_tokens").getAsInt())
                        .model(request.getModel())
                        .requestId(jsonResponse.has("id") ? jsonResponse.get("id").getAsString() : null)
                        .duration(duration)
                        .finishReason(firstChoice.get("finish_reason").getAsString())
                        .build();
            }

        } catch (IOException e) {
            log.error("调用文心一言API失败 - model: {}, prompt长度: {}, 错误: {}", 
                    request.getModel(), request.getPrompt().length(), e.getMessage(), e);
            throw new BusinessException("调用AI模型失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用文心一言API未知异常 - model: {}, prompt长度: {}, 错误类型: {}, 错误: {}", 
                    request.getModel(), request.getPrompt().length(), e.getClass().getName(), e.getMessage(), e);
            throw new BusinessException("调用AI模型失败: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel());
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();

            // 添加系统提示词（新API使用system role）
            if (request.getSystemPrompt() != null) {
                messages.add(createMessage("system", request.getSystemPrompt()));
            }

            // 添加历史消息
            if (request.getHistory() != null) {
                for (ChatRequest.ChatMessage msg : request.getHistory()) {
                    messages.add(createMessage(msg.getRole(), msg.getContent()));
                }
            }

            // 添加当前消息
            messages.add(createMessage("user", request.getMessage()));

            requestBody.put("messages", messages);
            
            if (request.getTemperature() != null) {
                requestBody.put("temperature", request.getTemperature());
            }

            String jsonBody = gson.toJson(requestBody);

            // 发送请求（使用新的Bearer Token认证）
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request httpRequest = new Request.Builder()
                    .url(CHAT_API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.error("文心一言对话API调用失败: code={}, errorBody={}", response.code(), errorBody);
                    throw new BusinessException("文心一言API调用失败[" + response.code() + "]: " + errorBody);
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                // 解析响应（新API返回格式）
                JsonObject firstChoice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                JsonObject usage = jsonResponse.getAsJsonObject("usage");

                long duration = System.currentTimeMillis() - startTime;

                return ChatResponse.builder()
                        .message(message.get("content").getAsString())
                        .conversationId(request.getConversationId())
                        .tokensUsed(usage.get("total_tokens").getAsInt())
                        .model(request.getModel())
                        .requestId(jsonResponse.has("id") ? jsonResponse.get("id").getAsString() : null)
                        .duration(duration)
                        .finishReason(firstChoice.get("finish_reason").getAsString())
                        .build();
            }

        } catch (IOException e) {
            log.error("调用文心一言对话API失败 - model: {}, message长度: {}, 错误: {}", 
                    request.getModel(), request.getMessage().length(), e.getMessage(), e);
            throw new BusinessException("调用AI对话失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用文心一言对话API未知异常 - model: {}, message长度: {}, 错误类型: {}, 错误: {}", 
                    request.getModel(), request.getMessage().length(), e.getClass().getName(), e.getMessage(), e);
            throw new BusinessException("调用AI对话失败: " + e.getMessage());
        }
    }

    /**
     * 创建消息对象
     */
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    @Override
    public String getProviderName() {
        return "百度文心一言";
    }

    @Override
    public boolean supportsModel(String modelName) {
        return SUPPORTED_MODELS.contains(modelName);
    }
}
