package com.intellihub.api.service;

import com.intellihub.api.dto.request.ApiParamRequest;
import com.intellihub.api.dto.response.ApiParamResponse;

import java.util.List;

/**
 * API参数服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ApiParamService {

    /**
     * 获取API的请求参数列表
     */
    List<ApiParamResponse> listRequestParams(String apiId);

    /**
     * 获取API的响应参数列表
     */
    List<ApiParamResponse> listResponseParams(String apiId);

    /**
     * 批量保存请求参数
     */
    void saveRequestParams(String apiId, List<ApiParamRequest> params);

    /**
     * 批量保存响应参数
     */
    void saveResponseParams(String apiId, List<ApiParamRequest> params);

    /**
     * 添加请求参数
     */
    ApiParamResponse addRequestParam(String apiId, ApiParamRequest request);

    /**
     * 添加响应参数
     */
    ApiParamResponse addResponseParam(String apiId, ApiParamRequest request);

    /**
     * 更新请求参数
     */
    ApiParamResponse updateRequestParam(String paramId, ApiParamRequest request);

    /**
     * 更新响应参数
     */
    ApiParamResponse updateResponseParam(String paramId, ApiParamRequest request);

    /**
     * 删除请求参数
     */
    void deleteRequestParam(String paramId);

    /**
     * 删除响应参数
     */
    void deleteResponseParam(String paramId);
}
