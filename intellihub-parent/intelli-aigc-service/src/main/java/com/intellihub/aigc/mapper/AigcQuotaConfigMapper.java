package com.intellihub.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.aigc.entity.AigcQuotaConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AIGC配额配置Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AigcQuotaConfigMapper extends BaseMapper<AigcQuotaConfig> {
}
