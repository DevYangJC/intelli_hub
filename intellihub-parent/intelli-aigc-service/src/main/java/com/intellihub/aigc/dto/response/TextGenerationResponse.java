package com.intellihub.aigc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本生成响应DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextGenerationResponse {

    /**
     * 生成的文本内容
     */
    private String text;

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
     * 完成原因（stop/length/error）
     */
    private String finishReason;
}
