package com.intellihub.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.api.dto.request.ApiParamRequest;
import com.intellihub.api.dto.response.ApiParamResponse;
import com.intellihub.api.entity.ApiRequestParam;
import com.intellihub.api.entity.ApiResponseParam;
import com.intellihub.api.mapper.ApiRequestParamMapper;
import com.intellihub.api.mapper.ApiResponseParamMapper;
import com.intellihub.api.service.ApiParamService;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API参数服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ApiParamServiceImpl implements ApiParamService {

    private final ApiRequestParamMapper requestParamMapper;
    private final ApiResponseParamMapper responseParamMapper;

    @Override
    public List<ApiParamResponse> listRequestParams(String apiId) {
        LambdaQueryWrapper<ApiRequestParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiRequestParam::getApiId, apiId)
                .orderByAsc(ApiRequestParam::getSort);
        return requestParamMapper.selectList(wrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiParamResponse> listResponseParams(String apiId) {
        LambdaQueryWrapper<ApiResponseParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiResponseParam::getApiId, apiId)
                .orderByAsc(ApiResponseParam::getSort);
        return responseParamMapper.selectList(wrapper).stream()
                .map(this::convertResponseParamToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRequestParams(String apiId, List<ApiParamRequest> params) {
        // 先删除原有参数
        LambdaQueryWrapper<ApiRequestParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiRequestParam::getApiId, apiId);
        requestParamMapper.delete(wrapper);

        // 批量插入新参数
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                ApiParamRequest param = params.get(i);
                ApiRequestParam entity = new ApiRequestParam();
                BeanUtils.copyProperties(param, entity);
                entity.setApiId(apiId);
                entity.setSort(param.getSort() != null ? param.getSort() : i);
                requestParamMapper.insert(entity);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveResponseParams(String apiId, List<ApiParamRequest> params) {
        // 先删除原有参数
        LambdaQueryWrapper<ApiResponseParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiResponseParam::getApiId, apiId);
        responseParamMapper.delete(wrapper);

        // 批量插入新参数
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                ApiParamRequest param = params.get(i);
                ApiResponseParam entity = new ApiResponseParam();
                entity.setApiId(apiId);
                entity.setName(param.getName());
                entity.setType(param.getType());
                entity.setDescription(param.getDescription());
                entity.setExample(param.getExample());
                entity.setSort(param.getSort() != null ? param.getSort() : i);
                responseParamMapper.insert(entity);
            }
        }
    }

    @Override
    public ApiParamResponse addRequestParam(String apiId, ApiParamRequest request) {
        ApiRequestParam entity = new ApiRequestParam();
        BeanUtils.copyProperties(request, entity);
        entity.setApiId(apiId);
        requestParamMapper.insert(entity);
        return convertToResponse(entity);
    }

    @Override
    public ApiParamResponse addResponseParam(String apiId, ApiParamRequest request) {
        ApiResponseParam entity = new ApiResponseParam();
        entity.setApiId(apiId);
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setDescription(request.getDescription());
        entity.setExample(request.getExample());
        entity.setSort(request.getSort());
        responseParamMapper.insert(entity);
        return convertResponseParamToResponse(entity);
    }

    @Override
    public ApiParamResponse updateRequestParam(String paramId, ApiParamRequest request) {
        ApiRequestParam entity = requestParamMapper.selectById(paramId);
        if (entity == null) {
            throw new BusinessException("请求参数不存在");
        }
        BeanUtils.copyProperties(request, entity);
        requestParamMapper.updateById(entity);
        return convertToResponse(entity);
    }

    @Override
    public ApiParamResponse updateResponseParam(String paramId, ApiParamRequest request) {
        ApiResponseParam entity = responseParamMapper.selectById(paramId);
        if (entity == null) {
            throw new BusinessException("响应参数不存在");
        }
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setDescription(request.getDescription());
        entity.setExample(request.getExample());
        entity.setSort(request.getSort());
        responseParamMapper.updateById(entity);
        return convertResponseParamToResponse(entity);
    }

    @Override
    public void deleteRequestParam(String paramId) {
        requestParamMapper.deleteById(paramId);
    }

    @Override
    public void deleteResponseParam(String paramId) {
        responseParamMapper.deleteById(paramId);
    }

    private ApiParamResponse convertToResponse(ApiRequestParam entity) {
        ApiParamResponse response = new ApiParamResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }

    private ApiParamResponse convertResponseParamToResponse(ApiResponseParam entity) {
        ApiParamResponse response = new ApiParamResponse();
        response.setId(entity.getId());
        response.setApiId(entity.getApiId());
        response.setName(entity.getName());
        response.setType(entity.getType());
        response.setDescription(entity.getDescription());
        response.setExample(entity.getExample());
        response.setSort(entity.getSort());
        return response;
    }
}
