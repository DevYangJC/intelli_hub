package com.intellihub.aigc.controller;

import com.intellihub.aigc.client.QianfanClient;
import com.intellihub.aigc.model.ChatRequest;
import com.intellihub.aigc.model.ChatResponse;
import com.intellihub.aigc.model.Message;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AIGC对话接口
 *
 * @author IntelliHub
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/aigc")
@RequiredArgsConstructor
@Validated
@Tag(name = "AIGC对话", description = "AI对话生成接口")
public class AigcController {

    private final QianfanClient qianfanClient;

    /**
     * 对话接口
     */
    @PostMapping("/chat")
    @Operation(summary = "对话", description = "发起AI对话请求")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到对话请求: model={}, messageCount={}", 
                request.getModel(), request.getMessages().size());
        
        ChatResponse response = qianfanClient.chat(request);
        
        if (response.isSuccess()) {
            log.info("对话成功: id={}, tokens={}", 
                    response.getId(), 
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : 0);
            return ApiResponse.success(response);
        } else {
            log.warn("对话失败: code={}, message={}", response.getCode(), response.getMessage());
            return ApiResponse.error(500, response.getMessage());
        }
    }

    /**
     * 简单对话接口
     */
    @PostMapping("/chat/simple")
    @Operation(summary = "简单对话", description = "快速发起单轮对话")
    public ApiResponse<String> simpleChat(
            @RequestParam(defaultValue = "ernie-4.0-8k") String model,
            @RequestParam @NotBlank(message = "消息不能为空") String message) {
        log.info("收到简单对话请求: model={}, message={}", model, message);
        
        ChatRequest request = ChatRequest.builder()
                .model(model)
                .messages(Collections.singletonList(Message.user(message)))
                .build();
        
        ChatResponse response = qianfanClient.chat(request);
        
        if (response.isSuccess()) {
            return ApiResponse.success(response.getContent());
        } else {
            return ApiResponse.error(500, response.getMessage());
        }
    }

    /**
     * 带系统提示的对话接口
     */
    @PostMapping("/chat/with-system")
    @Operation(summary = "带系统提示的对话", description = "使用系统提示发起对话")
    public ApiResponse<String> chatWithSystem(
            @RequestParam(defaultValue = "ernie-4.0-8k") String model,
            @RequestParam @NotBlank(message = "系统提示不能为空") String systemPrompt,
            @RequestParam @NotBlank(message = "消息不能为空") String message) {
        log.info("收到系统提示对话请求: model={}", model);
        
        ChatRequest request = ChatRequest.builder()
                .model(model)
                .messages(Arrays.asList(
                        Message.system(systemPrompt),
                        Message.user(message)
                ))
                .build();
        
        ChatResponse response = qianfanClient.chat(request);
        
        if (response.isSuccess()) {
            return ApiResponse.success(response.getContent());
        } else {
            return ApiResponse.error(500, response.getMessage());
        }
    }

    /**
     * 流式对话接口
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话", description = "发起流式AI对话请求")
    public Flux<ChatResponse> chatStream(@Valid @RequestBody ChatRequest request) {
        log.info("收到流式对话请求: model={}, messageCount={}", 
                request.getModel(), request.getMessages().size());
        
        return qianfanClient.chatStreamFlux(request);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查AIGC服务状态")
    public ApiResponse<String> health() {
        return ApiResponse.success("AIGC服务运行正常");
    }
}
