package com.intellihub.aigc.dubbo;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.entity.AigcConversation;
import com.intellihub.aigc.service.AigcService;
import com.intellihub.aigc.service.ConversationService;
import com.intellihub.aigc.service.QuotaService;
import com.intellihub.context.UserContextHolder;
import com.intellihub.dubbo.AigcDubboService;
import com.intellihub.dubbo.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AIGC Dubbo服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService
@RequiredArgsConstructor
public class AigcDubboServiceImpl implements AigcDubboService {

    private final AigcService aigcService;
    private final QuotaService quotaService;
    private final ConversationService conversationService;

    @Override
    public String generateText(String prompt, String model) {
        log.info("Dubbo调用 - generateText: model={}, promptLength={}", model, prompt.length());

        TextGenerationRequest request = new TextGenerationRequest();
        request.setPrompt(prompt);
        request.setModel(model);
        request.setMaxTokens(2000);
        request.setTemperature(0.7f);

        TextGenerationResponse response = aigcService.generateText(request);
        return response.getText();
    }

    @Override
    public String chat(String message, String model, String conversationId) {
        log.info("Dubbo调用 - chat: model={}, conversationId={}", model, conversationId);

        // 如果没有提供conversationId，生成一个新的
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        // 获取历史消息
        List<AigcConversation> history = conversationService.getHistory(conversationId, 10);

        // 构建请求
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setModel(model);
        request.setConversationId(conversationId);
        request.setMaxTokens(2000);
        request.setTemperature(0.7f);

        // 转换历史消息
        if (!history.isEmpty()) {
            List<ChatRequest.ChatMessage> historyMessages = history.stream()
                    .map(h -> {
                        ChatRequest.ChatMessage msg = new ChatRequest.ChatMessage();
                        msg.setRole(h.getRole());
                        msg.setContent(h.getContent());
                        return msg;
                    })
                    .collect(Collectors.toList());
            request.setHistory(historyMessages);
        }

        // 调用对话服务
        ChatResponse response = aigcService.chat(request);

        // 保存用户消息
        String tenantId = UserContextHolder.getCurrentTenantId();
        String userId = UserContextHolder.getCurrentUserId();
        
        saveConversationMessage(tenantId, conversationId, userId, "user", message, 0);

        // 保存AI回复
        saveConversationMessage(tenantId, conversationId, userId, "assistant", 
                response.getMessage(), response.getTokensUsed());

        return response.getMessage();
    }

    @Override
    public boolean checkQuota(String tenantId) {
        // 检查配额（假设需要2000 tokens）
        return quotaService.checkQuota(tenantId, 2000);
    }

    @Override
    public List<ChatMessageDTO> getConversationHistory(String conversationId, int limit) {
        log.info("Dubbo调用 - getConversationHistory: conversationId={}, limit={}", 
                conversationId, limit);

        List<AigcConversation> history = conversationService.getHistory(conversationId, limit);

        return history.stream()
                .map(h -> ChatMessageDTO.builder()
                        .role(h.getRole())
                        .content(h.getContent())
                        .tokens(h.getTokens())
                        .createdAt(h.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void clearConversationHistory(String conversationId) {
        log.info("Dubbo调用 - clearConversationHistory: conversationId={}", conversationId);
        conversationService.clearHistory(conversationId);
    }

    /**
     * 保存对话消息
     */
    private void saveConversationMessage(String tenantId, String conversationId, 
                                        String userId, String role, String content, int tokens) {
        AigcConversation conversation = AigcConversation.builder()
                .tenantId(tenantId)
                .conversationId(conversationId)
                .userId(userId)
                .role(role)
                .content(content)
                .tokens(tokens)
                .build();
        
        conversationService.saveMessage(conversation);
    }
}
