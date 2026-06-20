package com.intellihub.aigc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话响应模型
 * 兼容百度千帆API格式
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

    /**
     * 请求唯一标识
     */
    private String id;

    /**
     * 对象类型：chat.completion
     */
    private String object;

    /**
     * 创建时间戳
     */
    private Long created;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 响应选项列表
     */
    private List<Choice> choices;

    /**
     * Token使用统计
     */
    private Usage usage;

    /**
     * 搜索结果（联网搜索时返回）
     */
    @JsonProperty("search_results")
    private List<SearchResult> searchResults;

    /**
     * 错误码（请求失败时返回）
     */
    private String code;

    /**
     * 错误信息（请求失败时返回）
     */
    private String message;

    /**
     * 是否请求成功
     */
    public boolean isSuccess() {
        return code == null && choices != null && !choices.isEmpty();
    }

    /**
     * 获取第一个响应内容
     */
    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Choice choice = choices.get(0);
        if (choice.getMessage() != null) {
            return choice.getMessage().getContent();
        }
        if (choice.getDelta() != null) {
            return choice.getDelta().getContent();
        }
        return null;
    }

    /**
     * 获取思维链内容
     */
    public String getReasoningContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Choice choice = choices.get(0);
        if (choice.getMessage() != null) {
            return choice.getMessage().getReasoningContent();
        }
        return null;
    }

    /**
     * 选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {
        private Integer index;
        private ResponseMessage message;
        private Delta delta;

        @JsonProperty("finish_reason")
        private String finishReason;

        private Integer flag;

        @JsonProperty("ban_round")
        private Integer banRound;
    }

    /**
     * 响应消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMessage {
        private String role;
        private String content;
        private String name;

        @JsonProperty("tool_calls")
        private List<Message.ToolCall> toolCalls;

        @JsonProperty("tool_call_id")
        private String toolCallId;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    /**
     * 流式增量响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Delta {
        private String role;
        private String content;

        @JsonProperty("tool_calls")
        private List<Message.ToolCall> toolCalls;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    /**
     * Token使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;

        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;
    }

    /**
     * Prompt Token详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptTokensDetails {
        @JsonProperty("search_tokens")
        private Integer searchTokens;

        @JsonProperty("cached_tokens")
        private Integer cachedTokens;
    }

    /**
     * 搜索结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private Integer index;
        private String url;
        private String title;
    }
}
