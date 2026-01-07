package com.intellihub.aigc.service;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AIGC流式服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AigcStreamService {

    /**
     * 流式文本生成
     *
     * @param request 生成请求
     * @return SSE发射器
     */
    SseEmitter streamGenerateText(TextGenerationRequest request);

    /**
     * 流式智能对话
     *
     * @param request 对话请求
     * @return SSE发射器
     */
    SseEmitter streamChat(ChatRequest request);
}
