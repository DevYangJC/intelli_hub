package com.intellihub.aigc.service.impl;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.service.AigcService;
import com.intellihub.aigc.service.AigcStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AIGC流式服务实现
 * 将完整响应分块发送，模拟流式效果
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AigcStreamServiceImpl implements AigcStreamService {

    private final AigcService aigcService;

    private static final long TIMEOUT = 5 * 60 * 1000L; // 5分钟超时
    private static final int CHUNK_SIZE = 10; // 每次发送的字符数

    @Override
    public SseEmitter streamGenerateText(TextGenerationRequest request) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // 异步处理
        processStreamGenerateText(request, emitter);
        
        return emitter;
    }

    @Override
    public SseEmitter streamChat(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // 异步处理
        processStreamChat(request, emitter);
        
        return emitter;
    }

    /**
     * 异步处理流式文本生成
     */
    @Async
    public void processStreamGenerateText(TextGenerationRequest request, SseEmitter emitter) {
        try {
            // 调用生成服务获取完整响应
            TextGenerationResponse response = aigcService.generateText(request);
            String text = response.getText();
            
            // 分块发送
            sendTextInChunks(emitter, text);
            
            // 发送完成标记
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data("[DONE]"));
            
            emitter.complete();
            
            log.info("流式文本生成完成 - model: {}, length: {}", request.getModel(), text.length());
            
        } catch (Exception e) {
            log.error("流式文本生成失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("生成失败: " + e.getMessage()));
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
            emitter.completeWithError(e);
        }
    }

    /**
     * 异步处理流式对话
     */
    @Async
    public void processStreamChat(ChatRequest request, SseEmitter emitter) {
        try {
            // 调用对话服务获取完整响应
            ChatResponse response = aigcService.chat(request);
            String message = response.getMessage();
            
            // 分块发送
            sendTextInChunks(emitter, message);
            
            // 发送完成标记
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data("[DONE]"));
            
            emitter.complete();
            
            log.info("流式对话完成 - model: {}, length: {}", request.getModel(), message.length());
            
        } catch (Exception e) {
            log.error("流式对话失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("对话失败: " + e.getMessage()));
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
            emitter.completeWithError(e);
        }
    }

    /**
     * 分块发送文本
     */
    private void sendTextInChunks(SseEmitter emitter, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        int length = text.length();
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, length);
            String chunk = text.substring(i, end);
            
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunk));
            
            // 模拟延迟，让流式效果更明显
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("发送中断", e);
                break;
            }
        }
    }
}
