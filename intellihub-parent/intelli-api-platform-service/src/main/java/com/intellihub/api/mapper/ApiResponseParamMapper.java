package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiResponseParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * API响应参数Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiResponseParamMapper extends BaseMapper<ApiResponseParam> {
}
