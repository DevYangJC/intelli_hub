package com.intellihub.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.aigc.entity.AigcModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AIGC模型配置Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AigcModelConfigMapper extends BaseMapper<AigcModelConfig> {
}
