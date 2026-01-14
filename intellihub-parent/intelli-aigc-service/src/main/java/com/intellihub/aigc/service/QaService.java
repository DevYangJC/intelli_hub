package com.intellihub.aigc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.aigc.client.QianfanClient;
import com.intellihub.aigc.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 智能问答服务
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaService {

    private final QianfanClient qianfanClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = 
        "你是 IntelliHub API 管理平台的智能助手，专门帮助用户解答 API 相关问题。\n\n" +
        "你的职责包括：\n" +
        "1. 解答 API 使用方法和调用示例\n" +
        "2. 帮助排查 API 调用错误\n" +
        "3. 提供 API 开发最佳实践建议\n" +
        "4. 解释平台功能和配置\n\n" +
        "回答要求：\n" +
        "- 回答要简洁、准确、专业\n" +
        "- 如果涉及代码示例，使用 Markdown 代码块格式\n" +
        "- 如果不确定答案，请诚实告知\n" +
        "- 可以推荐相关问题帮助用户深入了解\n\n" +
        "平台信息：\n" +
        "- IntelliHub 是一个企业级 API 管理平台\n" +
        "- 支持 API 发布、调用统计、限流、告警等功能\n" +
        "- 认证方式：无认证(none)、Token认证(token)、签名认证(signature)\n" +
        "- API 状态：草稿(draft)、已发布(published)、已下线(offline)、已废弃(deprecated)";

    /**
     * 处理问答请求
     */
    public QaResponse ask(QaRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("收到问答请求: question={}", truncate(request.getQuestion(), 50));
            
            // 构建消息列表
            List<Message> messages = buildMessages(request);
            
            ChatRequest chatRequest = ChatRequest.builder()
                    .model(qianfanClient.getDefaultModel())
                    .messages(messages)
                    .temperature(0.7)
                    .build();
            
            ChatResponse chatResponse = qianfanClient.chat(chatRequest);
            
            if (!chatResponse.isSuccess()) {
                log.error("问答失败: {}", chatResponse.getMessage());
                return buildErrorResponse("AI 服务暂时不可用，请稍后重试");
            }
            
            String content = chatResponse.getContent();
            QaResponse response = parseResponse(content, request);
            
            long endTime = System.currentTimeMillis();
            response.setResponseTimeMs(endTime - startTime);
            response.setModel(qianfanClient.getDefaultModel());
            if (chatResponse.getUsage() != null) {
                response.setTokensUsed(chatResponse.getUsage().getTotalTokens());
            }
            
            // 生成建议问题
            response.setSuggestedQuestions(generateSuggestedQuestions(request.getQuestion()));
            
            log.info("问答完成: 耗时={}ms", response.getResponseTimeMs());
            return response;
            
        } catch (Exception e) {
            log.error("问答异常", e);
            return buildErrorResponse("处理问题时发生异常: " + e.getMessage());
        }
    }

    /**
     * 构建消息列表
     */
    private List<Message> buildMessages(QaRequest request) {
        List<Message> messages = new ArrayList<>();
        
        // 系统提示
        messages.add(Message.system(SYSTEM_PROMPT));
        
        // 对话历史
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (QaRequest.ChatHistory history : request.getHistory()) {
                if ("user".equals(history.getRole())) {
                    messages.add(Message.user(history.getContent()));
                } else if ("assistant".equals(history.getRole())) {
                    messages.add(Message.assistant(history.getContent()));
                }
            }
        }
        
        // 当前问题
        String userPrompt = buildUserPrompt(request);
        messages.add(Message.user(userPrompt));
        
        return messages;
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(QaRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getQuestion());
        
        // 添加问题类型上下文
        if (request.getQuestionType() != null) {
            switch (request.getQuestionType()) {
                case "usage":
                    sb.append("\n\n（请重点介绍使用方法和调用示例）");
                    break;
                case "error":
                    sb.append("\n\n（请帮助分析可能的错误原因和解决方案）");
                    break;
                case "best_practice":
                    sb.append("\n\n（请提供最佳实践建议）");
                    break;
            }
        }
        
        return sb.toString();
    }

    /**
     * 解析响应
     */
    private QaResponse parseResponse(String content, QaRequest request) {
        QaResponse response = QaResponse.builder()
                .answer(content)
                .confidence(0.8)
                .build();
        
        // 提取代码示例（如果有）
        String codeExample = extractCodeExample(content);
        if (codeExample != null) {
            response.setCodeExample(codeExample);
        }
        
        return response;
    }

    /**
     * 提取代码示例
     */
    private String extractCodeExample(String content) {
        // 简单提取 Markdown 代码块
        int start = content.indexOf("```");
        if (start >= 0) {
            int end = content.indexOf("```", start + 3);
            if (end > start) {
                return content.substring(start, end + 3);
            }
        }
        return null;
    }

    /**
     * 生成建议问题
     */
    private List<String> generateSuggestedQuestions(String question) {
        // 基于问题类型生成相关问题
        List<String> suggestions = new ArrayList<>();
        
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("调用") || lowerQuestion.contains("请求")) {
            suggestions.add("如何处理 API 调用超时？");
            suggestions.add("如何查看 API 调用日志？");
        }
        
        if (lowerQuestion.contains("认证") || lowerQuestion.contains("token")) {
            suggestions.add("Token 认证和签名认证有什么区别？");
            suggestions.add("如何刷新过期的 Token？");
        }
        
        if (lowerQuestion.contains("限流") || lowerQuestion.contains("qps")) {
            suggestions.add("如何配置 API 限流策略？");
            suggestions.add("被限流后如何处理？");
        }
        
        if (lowerQuestion.contains("错误") || lowerQuestion.contains("失败")) {
            suggestions.add("常见的错误码有哪些？");
            suggestions.add("如何配置告警规则？");
        }
        
        // 默认建议
        if (suggestions.isEmpty()) {
            suggestions.add("如何创建和发布 API？");
            suggestions.add("平台支持哪些认证方式？");
            suggestions.add("如何查看 API 调用统计？");
        }
        
        return suggestions.subList(0, Math.min(3, suggestions.size()));
    }

    /**
     * 构建错误响应
     */
    private QaResponse buildErrorResponse(String errorMessage) {
        return QaResponse.builder()
                .answer(errorMessage)
                .confidence(0.0)
                .suggestedQuestions(Arrays.asList(
                        "如何创建和发布 API？",
                        "平台支持哪些认证方式？"
                ))
                .build();
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}
