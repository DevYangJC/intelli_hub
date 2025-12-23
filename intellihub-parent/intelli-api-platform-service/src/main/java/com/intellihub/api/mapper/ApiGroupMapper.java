package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * API分组Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiGroupMapper extends BaseMapper<ApiGroup> {
}
