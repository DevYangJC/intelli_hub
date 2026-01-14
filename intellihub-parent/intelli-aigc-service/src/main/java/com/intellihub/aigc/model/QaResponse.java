package com.intellihub.aigc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 智能问答响应
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaResponse {

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 相关 API 列表（如果问题涉及具体 API）
     */
    private List<RelatedApi> relatedApis;

    /**
     * 代码示例（如果适用）
     */
    private String codeExample;

    /**
     * 参考链接
     */
    private List<String> references;

    /**
     * 后续建议问题
     */
    private List<String> suggestedQuestions;

    /**
     * 回答置信度（0-1）
     */
    private Double confidence;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 消耗的 token 数
     */
    private Integer tokensUsed;

    /**
     * 响应耗时（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 相关 API 信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedApi {
        private String id;
        private String name;
        private String path;
        private String method;
        private String description;
    }
}
