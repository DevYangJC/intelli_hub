package com.intellihub.aigc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话响应DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 回复消息
     */
    private String message;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 消耗的Token数
     */
    private Integer tokensUsed;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 完成原因
     */
    private String finishReason;
}
