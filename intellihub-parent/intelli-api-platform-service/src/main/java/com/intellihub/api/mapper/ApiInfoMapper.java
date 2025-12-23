package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * API信息Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiInfoMapper extends BaseMapper<ApiInfo> {
}
