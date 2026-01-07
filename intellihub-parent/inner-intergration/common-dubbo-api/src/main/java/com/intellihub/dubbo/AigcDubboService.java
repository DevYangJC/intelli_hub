package com.intellihub.dubbo;

/**
 * AIGC Dubbo服务接口
 * 供其他服务内部调用AI能力
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AigcDubboService {

    /**
     * 文本生成（简化版）
     *
     * @param prompt 提示词
     * @param model 模型名称
     * @return 生成的文本
     */
    String generateText(String prompt, String model);

    /**
     * 智能对话（简化版）
     *
     * @param message 用户消息
     * @param model 模型名称
     * @param conversationId 会话ID（可选）
     * @return AI回复
     */
    String chat(String message, String model, String conversationId);

    /**
     * 检查租户配额
     *
     * @param tenantId 租户ID
     * @return 是否有足够配额
     */
    boolean checkQuota(String tenantId);

    /**
     * 获取会话历史
     *
     * @param conversationId 会话ID
     * @param limit 返回条数
     * @return 历史消息列表
     */
    java.util.List<ChatMessageDTO> getConversationHistory(String conversationId, int limit);

    /**
     * 清空会话历史
     *
     * @param conversationId 会话ID
     */
    void clearConversationHistory(String conversationId);
}
