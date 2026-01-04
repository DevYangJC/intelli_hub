package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.RatelimitPolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 限流策略Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface RatelimitPolicyMapper extends BaseMapper<RatelimitPolicy> {
}
