package com.intellihub.aigc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.aigc.config.QianfanProperties;
import com.intellihub.aigc.model.ChatRequest;
import com.intellihub.aigc.model.ChatResponse;
import com.intellihub.aigc.model.Message;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 百度千帆API客户端
 * 支持同步调用和流式调用
 *
 * @author IntelliHub
 */
@Slf4j
public class QianfanClient {

    private final QianfanProperties properties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String CHAT_ENDPOINT = "/v2/chat/completions";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public QianfanClient(QianfanProperties properties, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.properties = properties;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 同步对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    public ChatResponse chat(ChatRequest request) {
        if (request.getStream() != null && request.getStream()) {
            throw new IllegalArgumentException("同步调用不支持流式响应，请使用 chatStream 方法");
        }

        String url = properties.getBaseUrl() + CHAT_ENDPOINT;
        
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            log.debug("千帆请求: url={}, body={}", url, requestBody);

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .post(RequestBody.create(requestBody, JSON_MEDIA_TYPE))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                log.debug("千帆响应: status={}, body={}", response.code(), responseBody);

                if (!response.isSuccessful()) {
                    log.error("千帆API调用失败: status={}, body={}", response.code(), responseBody);
                    return ChatResponse.builder()
                            .code(String.valueOf(response.code()))
                            .message(responseBody)
                            .build();
                }

                return objectMapper.readValue(responseBody, ChatResponse.class);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return ChatResponse.builder()
                    .code("JSON_ERROR")
                    .message("JSON序列化失败: " + e.getMessage())
                    .build();
        } catch (IOException e) {
            log.error("HTTP请求失败", e);
            return ChatResponse.builder()
                    .code("HTTP_ERROR")
                    .message("HTTP请求失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 流式对话
     *
     * @param request  对话请求
     * @param onChunk  每个chunk的回调
     * @param onError  错误回调
     * @param onComplete 完成回调
     */
    public void chatStream(ChatRequest request, 
                           Consumer<ChatResponse> onChunk,
                           Consumer<Throwable> onError,
                           Runnable onComplete) {
        request.setStream(true);
        if (request.getStreamOptions() == null) {
            request.setStreamOptions(ChatRequest.StreamOptions.builder()
                    .includeUsage(true)
                    .build());
        }

        String url = properties.getBaseUrl() + CHAT_ENDPOINT;

        try {
            String requestBody = objectMapper.writeValueAsString(request);
            log.debug("千帆流式请求: url={}, body={}", url, requestBody);

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .post(RequestBody.create(requestBody, JSON_MEDIA_TYPE))
                    .build();

            httpClient.newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("千帆流式请求失败", e);
                    onError.accept(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        String errorBody = "";
                        try {
                            errorBody = response.body() != null ? response.body().string() : "";
                        } catch (IOException ignored) {}
                        log.error("千帆流式API调用失败: status={}, body={}", response.code(), errorBody);
                        onError.accept(new RuntimeException("API调用失败: " + response.code()));
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) {
                                    log.debug("千帆流式响应完成");
                                    onComplete.run();
                                    break;
                                }
                                try {
                                    ChatResponse chunk = objectMapper.readValue(data, ChatResponse.class);
                                    onChunk.accept(chunk);
                                } catch (JsonProcessingException e) {
                                    log.warn("解析流式响应chunk失败: {}", data, e);
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("读取流式响应失败", e);
                        onError.accept(e);
                    }
                }
            });
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            onError.accept(e);
        }
    }

    /**
     * 流式对话（Reactor Flux）
     *
     * @param request 对话请求
     * @return 响应流
     */
    public Flux<ChatResponse> chatStreamFlux(ChatRequest request) {
        return Flux.create(emitter -> {
            chatStream(request,
                    emitter::next,
                    emitter::error,
                    emitter::complete);
        });
    }

    /**
     * 简单对话（快捷方法）
     *
     * @param userMessage 用户消息
     * @return 助手回复内容
     */
    public String simpleChat(String userMessage) {
        ChatRequest request = ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(Collections.singletonList(Message.user(userMessage)))
                .build();
        
        ChatResponse response = chat(request);
        if (response.isSuccess()) {
            return response.getContent();
        }
        throw new RuntimeException("对话失败: " + response.getMessage());
    }

    /**
     * 带系统提示的对话（快捷方法）
     *
     * @param systemPrompt 系统提示
     * @param userMessage  用户消息
     * @return 助手回复内容
     */
    public String chatWithSystem(String systemPrompt, String userMessage) {
        ChatRequest request = ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(Arrays.asList(
                        Message.system(systemPrompt),
                        Message.user(userMessage)
                ))
                .build();

        ChatResponse response = chat(request);
        if (response.isSuccess()) {
            return response.getContent();
        }
        throw new RuntimeException("对话失败: " + response.getMessage());
    }

    /**
     * 使用指定模型对话
     *
     * @param model       模型ID
     * @param userMessage 用户消息
     * @return 助手回复内容
     */
    public String chatWithModel(String model, String userMessage) {
        ChatRequest request = ChatRequest.builder()
                .model(model)
                .messages(Collections.singletonList(Message.user(userMessage)))
                .build();

        ChatResponse response = chat(request);
        if (response.isSuccess()) {
            return response.getContent();
        }
        throw new RuntimeException("对话失败: " + response.getMessage());
    }

    /**
     * 获取配置的默认模型
     */
    public String getDefaultModel() {
        return properties.getDefaultModel();
    }
}
