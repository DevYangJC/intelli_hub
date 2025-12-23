package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiBackend;
import org.apache.ibatis.annotations.Mapper;

/**
 * API后端配置Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface ApiBackendMapper extends BaseMapper<ApiBackend> {
}
