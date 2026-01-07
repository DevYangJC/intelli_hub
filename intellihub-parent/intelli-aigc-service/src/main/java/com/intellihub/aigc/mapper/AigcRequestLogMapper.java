package com.intellihub.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.aigc.entity.AigcRequestLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * AIGC请求日志Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface AigcRequestLogMapper extends BaseMapper<AigcRequestLog> {
}
