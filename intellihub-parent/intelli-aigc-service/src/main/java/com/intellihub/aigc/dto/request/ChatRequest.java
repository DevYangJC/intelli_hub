package com.intellihub.aigc.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 对话请求DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ChatRequest {

    /**
     * 用户消息
     */
    @NotBlank(message = "用户消息不能为空")
    private String message;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String model;

    /**
     * Provider名称（可选，如果不指定则根据model自动选择）
     * aliyunQwenProvider / baiduErnieProvider / tencentHunyuanProvider
     */
    private String provider;

    /**
     * 会话ID（用于多轮对话）
     */
    private String conversationId;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 历史消息列表
     */
    private List<ChatMessage> history;

    /**
     * 最大生成Token数
     */
    private Integer maxTokens = 2000;

    /**
     * 温度参数
     */
    private Float temperature = 0.7f;

    /**
     * 是否启用流式输出
     */
    private Boolean stream = false;

    @Data
    public static class ChatMessage {
        private String role;  // user, assistant, system
        private String content;
    }
}
