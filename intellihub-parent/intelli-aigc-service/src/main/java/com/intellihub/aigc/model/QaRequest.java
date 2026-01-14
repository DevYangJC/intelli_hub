package com.intellihub.aigc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 智能问答请求
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaRequest {

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 对话历史（可选）
     */
    private List<ChatHistory> history;

    /**
     * 指定 API ID（可选，提供更精确的上下文）
     */
    private String apiId;

    /**
     * 问题类型（可选）
     * usage - API使用问题
     * error - 错误排查
     * best_practice - 最佳实践
     * general - 通用问题
     */
    private String questionType;

    /**
     * 对话历史项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistory {
        private String role; // user / assistant
        private String content;
    }
}
