package com.intellihub.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.aigc.entity.AigcConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AIGC对话历史Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AigcConversationMapper extends BaseMapper<AigcConversation> {
}
