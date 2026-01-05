package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.ApiResponseParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * API响应参数Mapper
 * 跳过租户拦截器：api_response_param 表无 tenant_id
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface ApiResponseParamMapper extends BaseMapper<ApiResponseParam> {
}
