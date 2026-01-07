package com.intellihub.aigc.controller;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.service.AigcStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;

/**
 * AIGC流式响应控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/aigc/stream")
@RequiredArgsConstructor
@Tag(name = "AIGC流式服务", description = "提供流式响应的AI生成服务")
public class AigcStreamController {

    private final AigcStreamService streamService;

    /**
     * 流式文本生成
     */
    @PostMapping(value = "/text/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式文本生成", description = "以SSE方式返回文本生成结果")
    public SseEmitter streamGenerateText(@Valid @RequestBody TextGenerationRequest request) {
        log.info("收到流式文本生成请求 - model: {}, prompt: {}", 
                request.getModel(), 
                request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())));
        
        return streamService.streamGenerateText(request);
    }

    /**
     * 流式智能对话
     */
    @PostMapping(value = "/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式智能对话", description = "以SSE方式返回对话结果")
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("收到流式对话请求 - model: {}, message: {}", 
                request.getModel(), 
                request.getMessage().substring(0, Math.min(50, request.getMessage().length())));
        
        return streamService.streamChat(request);
    }
}
