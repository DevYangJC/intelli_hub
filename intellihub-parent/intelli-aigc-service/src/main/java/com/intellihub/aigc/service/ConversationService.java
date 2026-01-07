package com.intellihub.aigc.service;

import com.intellihub.aigc.entity.AigcConversation;

import java.util.List;

/**
 * 对话历史管理服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ConversationService {

    /**
     * 保存对话消息
     *
     * @param conversation 对话消息
     */
    void saveMessage(AigcConversation conversation);

    /**
     * 获取会话历史
     *
     * @param conversationId 会话ID
     * @param limit 返回条数
     * @return 历史消息列表
     */
    List<AigcConversation> getHistory(String conversationId, int limit);

    /**
     * 清空会话历史
     *
     * @param conversationId 会话ID
     */
    void clearHistory(String conversationId);

    /**
     * 获取会话消息总数
     *
     * @param conversationId 会话ID
     * @return 消息数量
     */
    long getMessageCount(String conversationId);
}
