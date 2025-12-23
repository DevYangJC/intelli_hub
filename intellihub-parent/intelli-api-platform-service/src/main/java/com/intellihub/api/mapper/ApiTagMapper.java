package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * API标签Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiTagMapper extends BaseMapper<ApiTag> {
}
