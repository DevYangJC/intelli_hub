package com.intellihub.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.aigc.entity.AigcPromptTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * Prompt模板Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AigcPromptTemplateMapper extends BaseMapper<AigcPromptTemplate> {
}
