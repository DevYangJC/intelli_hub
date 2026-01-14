package com.intellihub.aigc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话消息模型
 * 兼容百度千帆API格式
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    /**
     * 角色
     * user: 用户
     * assistant: 助手
     * system: 系统人设
     * tool: 工具调用结果
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息名称（可选）
     */
    private String name;

    /**
     * 函数调用列表（assistant角色返回）
     */
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    /**
     * 工具调用ID（tool角色必填）
     */
    @JsonProperty("tool_call_id")
    private String toolCallId;

    /**
     * 快速构建用户消息
     */
    public static Message user(String content) {
        return Message.builder()
                .role("user")
                .content(content)
                .build();
    }

    /**
     * 快速构建系统消息
     */
    public static Message system(String content) {
        return Message.builder()
                .role("system")
                .content(content)
                .build();
    }

    /**
     * 快速构建助手消息
     */
    public static Message assistant(String content) {
        return Message.builder()
                .role("assistant")
                .content(content)
                .build();
    }

    /**
     * 工具调用模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;
    }

    /**
     * 函数调用详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Function {
        private String name;
        private String arguments;
    }
}
