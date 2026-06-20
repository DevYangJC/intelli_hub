package com.intellihub.aigc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对话请求模型
 * 兼容百度千帆API格式
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {

    /**
     * 模型ID
     * 预置服务：ernie-4.0-8k, ernie-3.5-8k, deepseek-v3 等
     */
    @NotBlank(message = "模型ID不能为空")
    private String model;

    /**
     * 对话消息列表
     */
    @NotEmpty(message = "消息列表不能为空")
    private List<Message> messages;

    /**
     * 是否流式返回
     */
    private Boolean stream;

    /**
     * 流式选项
     */
    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    /**
     * 温度参数（0-2）
     * 值越高输出越随机
     */
    private Double temperature;

    /**
     * Top-P采样（0-1）
     * 影响输出多样性
     */
    @JsonProperty("top_p")
    private Double topP;

    /**
     * 最大输出token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 随机种子
     */
    private Integer seed;

    /**
     * 停止词列表
     */
    private List<String> stop;

    /**
     * 频率惩罚
     */
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    /**
     * 存在惩罚
     */
    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    /**
     * 重复惩罚
     */
    @JsonProperty("penalty_score")
    private Double penaltyScore;

    /**
     * 工具列表（Function Calling）
     */
    private List<Tool> tools;

    /**
     * 工具选择策略
     */
    @JsonProperty("tool_choice")
    private Object toolChoice;

    /**
     * 联网搜索选项
     */
    @JsonProperty("web_search")
    private WebSearch webSearch;

    /**
     * 响应格式
     */
    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    /**
     * 是否开启思考模式
     */
    @JsonProperty("enable_thinking")
    private Boolean enableThinking;

    /**
     * 思维链最大长度
     */
    @JsonProperty("thinking_budget")
    private Integer thinkingBudget;

    /**
     * 用户标识
     */
    private String user;

    /**
     * 自定义元数据
     */
    private Map<String, String> metadata;

    /**
     * 流式选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamOptions {
        @JsonProperty("include_usage")
        private Boolean includeUsage;

        @JsonProperty("chunk_include_usage")
        private Boolean chunkIncludeUsage;
    }

    /**
     * 工具定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tool {
        private String type;
        private ToolFunction function;
    }

    /**
     * 工具函数定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolFunction {
        private String name;
        private String description;
        private Object parameters;
    }

    /**
     * 联网搜索选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSearch {
        private Boolean enable;

        @JsonProperty("enable_citation")
        private Boolean enableCitation;

        @JsonProperty("enable_trace")
        private Boolean enableTrace;

        @JsonProperty("search_mode")
        private String searchMode;
    }

    /**
     * 响应格式
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseFormat {
        private String type;

        @JsonProperty("json_schema")
        private Object jsonSchema;
    }

    /**
     * 快速构建简单请求
     */
    public static ChatRequest simple(String model, String userMessage) {
        return ChatRequest.builder()
                .model(model)
                .messages(Collections.singletonList(Message.user(userMessage)))
                .build();
    }

    /**
     * 快速构建带系统提示的请求
     */
    public static ChatRequest withSystem(String model, String systemPrompt, String userMessage) {
        return ChatRequest.builder()
                .model(model)
                .messages(Arrays.asList(
                        Message.system(systemPrompt),
                        Message.user(userMessage)
                ))
                .build();
    }
}
