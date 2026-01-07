package com.intellihub.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.aigc.entity.AigcConversation;
import com.intellihub.aigc.mapper.AigcConversationMapper;
import com.intellihub.aigc.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话历史管理服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final AigcConversationMapper conversationMapper;

    @Override
    public void saveMessage(AigcConversation conversation) {
        conversationMapper.insert(conversation);
        log.debug("保存对话消息 - conversationId: {}, role: {}", 
                conversation.getConversationId(), conversation.getRole());
    }

    @Override
    public List<AigcConversation> getHistory(String conversationId, int limit) {
        LambdaQueryWrapper<AigcConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcConversation::getConversationId, conversationId)
                .orderByDesc(AigcConversation::getCreatedAt)
                .last("LIMIT " + limit);
        
        List<AigcConversation> history = conversationMapper.selectList(wrapper);
        
        // 反转顺序，使最早的消息在前
        java.util.Collections.reverse(history);
        
        log.debug("获取会话历史 - conversationId: {}, count: {}", conversationId, history.size());
        return history;
    }

    @Override
    @Transactional
    public void clearHistory(String conversationId) {
        LambdaQueryWrapper<AigcConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcConversation::getConversationId, conversationId);
        
        int count = conversationMapper.delete(wrapper);
        log.info("清空会话历史 - conversationId: {}, 删除数量: {}", conversationId, count);
    }

    @Override
    public long getMessageCount(String conversationId) {
        LambdaQueryWrapper<AigcConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AigcConversation::getConversationId, conversationId);
        return conversationMapper.selectCount(wrapper);
    }
}
