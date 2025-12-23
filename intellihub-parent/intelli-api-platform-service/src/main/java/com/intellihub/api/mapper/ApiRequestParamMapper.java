package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiRequestParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * API请求参数Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiRequestParamMapper extends BaseMapper<ApiRequestParam> {
}
