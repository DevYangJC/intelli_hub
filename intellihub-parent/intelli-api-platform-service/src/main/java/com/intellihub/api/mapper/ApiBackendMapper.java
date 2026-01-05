package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiBackend;
import org.apache.ibatis.annotations.Mapper;

/**
 * API后端配置Mapper
 * 跳过租户拦截器：api_backend 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface ApiBackendMapper extends BaseMapper<ApiBackend> {
}
