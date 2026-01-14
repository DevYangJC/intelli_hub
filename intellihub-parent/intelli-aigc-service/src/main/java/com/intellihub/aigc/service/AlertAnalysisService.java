package com.intellihub.aigc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.aigc.client.QianfanClient;
import com.intellihub.aigc.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警智能分析服务
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertAnalysisService {

    private final QianfanClient qianfanClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = 
        "你是一个专业的API网关运维专家，擅长分析告警信息并提供根因分析和处理建议。\n" +
        "请基于提供的告警信息进行分析，并以JSON格式返回分析结果。\n\n" +
        "返回格式要求：\n" +
        "{\n" +
        "  \"severityAssessment\": \"严重程度评估（低/中/高/紧急）\",\n" +
        "  \"rootCauseAnalysis\": \"根因分析，描述问题的可能原因\",\n" +
        "  \"impactScope\": \"影响范围，描述该问题可能影响的业务范围\",\n" +
        "  \"recommendations\": [\"处理建议1\", \"处理建议2\", ...],\n" +
        "  \"preventiveMeasures\": [\"预防措施1\", \"预防措施2\", ...],\n" +
        "  \"relatedKnowledge\": \"相关知识或参考信息\",\n" +
        "  \"summary\": \"简短的分析摘要，不超过100字\"\n" +
        "}\n\n" +
        "注意事项：\n" +
        "1. 根因分析要具体，结合告警类型和请求详情\n" +
        "2. 处理建议要可操作，按优先级排序\n" +
        "3. 预防措施要有针对性\n" +
        "4. 只返回JSON，不要有其他文字";

    /**
     * 分析告警
     *
     * @param request 告警分析请求
     * @return 告警分析响应
     */
    public AlertAnalysisResponse analyzeAlert(AlertAnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String userPrompt = buildUserPrompt(request);
            log.info("开始分析告警: alertId={}, ruleType={}", request.getAlertId(), request.getRuleType());
            
            ChatRequest chatRequest = ChatRequest.builder()
                    .model(qianfanClient.getDefaultModel())
                    .messages(Arrays.asList(
                            Message.system(SYSTEM_PROMPT),
                            Message.user(userPrompt)
                    ))
                    .temperature(0.3)
                    .build();
            
            ChatResponse chatResponse = qianfanClient.chat(chatRequest);
            
            if (!chatResponse.isSuccess()) {
                log.error("告警分析失败: {}", chatResponse.getMessage());
                return buildErrorResponse(request.getAlertId(), "AI分析失败: " + chatResponse.getMessage());
            }
            
            String content = chatResponse.getContent();
            AlertAnalysisResponse response = parseResponse(content, request.getAlertId());
            
            long endTime = System.currentTimeMillis();
            response.setAnalysisTimeMs(endTime - startTime);
            response.setModel(qianfanClient.getDefaultModel());
            if (chatResponse.getUsage() != null) {
                response.setTokensUsed(chatResponse.getUsage().getTotalTokens());
            }
            
            log.info("告警分析完成: alertId={}, 耗时={}ms", request.getAlertId(), response.getAnalysisTimeMs());
            return response;
            
        } catch (Exception e) {
            log.error("告警分析异常: alertId={}", request.getAlertId(), e);
            return buildErrorResponse(request.getAlertId(), "分析异常: " + e.getMessage());
        }
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(AlertAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 告警信息\n\n");
        sb.append("- **告警ID**: ").append(request.getAlertId()).append("\n");
        sb.append("- **规则名称**: ").append(request.getRuleName()).append("\n");
        sb.append("- **规则类型**: ").append(getRuleTypeDesc(request.getRuleType())).append("\n");
        sb.append("- **API路径**: ").append(request.getApiPath() != null ? request.getApiPath() : "全局").append("\n");
        sb.append("- **告警级别**: ").append(getAlertLevelDesc(request.getAlertLevel())).append("\n");
        sb.append("- **告警消息**: ").append(request.getAlertMessage()).append("\n");
        sb.append("- **当前值**: ").append(request.getCurrentValue()).append("\n");
        sb.append("- **阈值**: ").append(request.getThresholdValue()).append("\n");
        
        if (request.getRequestDetails() != null && !request.getRequestDetails().isEmpty()) {
            sb.append("\n## 触发告警的请求详情（最近").append(request.getRequestDetails().size()).append("条）\n\n");
            sb.append("| 请求ID | 方法 | 路径 | 状态码 | 延迟(ms) | 错误信息 |\n");
            sb.append("|--------|------|------|--------|----------|----------|\n");
            
            for (AlertAnalysisRequest.RequestDetail detail : request.getRequestDetails()) {
                sb.append("| ").append(truncate(detail.getRequestId(), 8)).append(" | ");
                sb.append(detail.getMethod()).append(" | ");
                sb.append(truncate(detail.getApiPath(), 30)).append(" | ");
                sb.append(detail.getStatusCode()).append(" | ");
                sb.append(detail.getLatency()).append(" | ");
                sb.append(detail.getErrorMessage() != null ? truncate(detail.getErrorMessage(), 30) : "-").append(" |\n");
            }
        }
        
        sb.append("\n请根据以上信息进行分析，返回JSON格式的分析结果。");
        return sb.toString();
    }

    /**
     * 解析AI响应
     */
    private AlertAnalysisResponse parseResponse(String content, String alertId) {
        try {
            // 尝试提取JSON部分
            String jsonContent = extractJson(content);
            AlertAnalysisResponse response = objectMapper.readValue(jsonContent, AlertAnalysisResponse.class);
            response.setAlertId(alertId);
            return response;
        } catch (JsonProcessingException e) {
            log.warn("解析AI响应失败，使用原始内容: {}", e.getMessage());
            // 如果解析失败，将原始内容作为摘要返回
            return AlertAnalysisResponse.builder()
                    .alertId(alertId)
                    .severityAssessment("中")
                    .rootCauseAnalysis(content)
                    .impactScope("需要进一步确认")
                    .recommendations(Collections.singletonList("请查看详细分析内容"))
                    .preventiveMeasures(Collections.emptyList())
                    .summary(truncate(content, 100))
                    .build();
        }
    }

    /**
     * 从内容中提取JSON
     */
    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        // 查找第一个 { 和最后一个 }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    /**
     * 构建错误响应
     */
    private AlertAnalysisResponse buildErrorResponse(String alertId, String errorMessage) {
        return AlertAnalysisResponse.builder()
                .alertId(alertId)
                .severityAssessment("未知")
                .rootCauseAnalysis("分析失败: " + errorMessage)
                .impactScope("无法评估")
                .recommendations(Collections.singletonList("请手动分析告警"))
                .preventiveMeasures(Collections.emptyList())
                .summary("AI分析失败，请手动处理")
                .build();
    }

    /**
     * 获取规则类型描述
     */
    private String getRuleTypeDesc(String ruleType) {
        if (ruleType == null) return "未知";
        switch (ruleType) {
            case "error_rate": return "错误率监控";
            case "latency": return "延迟监控";
            case "qps": return "QPS监控";
            default: return ruleType;
        }
    }

    /**
     * 获取告警级别描述
     */
    private String getAlertLevelDesc(String level) {
        if (level == null) return "未知";
        switch (level) {
            case "critical": return "严重 🔴";
            case "warning": return "警告 🟡";
            case "info": return "信息 🔵";
            default: return level;
        }
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
