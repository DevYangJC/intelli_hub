package com.intellihub.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.api.dto.request.ApiBackendRequest;
import com.intellihub.api.dto.response.ApiBackendResponse;
import com.intellihub.api.entity.ApiBackend;
import com.intellihub.api.mapper.ApiBackendMapper;
import com.intellihub.api.service.ApiBackendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * API后端配置服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiBackendServiceImpl implements ApiBackendService {

    private final ApiBackendMapper backendMapper;

    @Override
    public ApiBackendResponse getBackendByApiId(String apiId) {
        LambdaQueryWrapper<ApiBackend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiBackend::getApiId, apiId);
        ApiBackend backend = backendMapper.selectOne(wrapper);
        if (backend == null) {
            return null;
        }
        return convertToResponse(backend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiBackendResponse saveBackend(String apiId, ApiBackendRequest request) {
        LambdaQueryWrapper<ApiBackend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiBackend::getApiId, apiId);
        ApiBackend existing = backendMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新
            BeanUtils.copyProperties(request, existing);
            backendMapper.updateById(existing);
            return convertToResponse(existing);
        } else {
            // 新增
            ApiBackend backend = new ApiBackend();
            BeanUtils.copyProperties(request, backend);
            backend.setApiId(apiId);
            backendMapper.insert(backend);
            return convertToResponse(backend);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBackend(String apiId) {
        LambdaQueryWrapper<ApiBackend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiBackend::getApiId, apiId);
        backendMapper.delete(wrapper);
    }

    @Override
    public boolean testConnection(ApiBackendRequest request) {
        try {
            String urlStr = request.getProtocol().toLowerCase() + "://" + request.getHost() + request.getPath();
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(request.getConnectTimeout() != null ? request.getConnectTimeout() : 5000);
            connection.setReadTimeout(request.getTimeout() != null ? request.getTimeout() : 30000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode >= 200 && responseCode < 500;
        } catch (Exception e) {
            log.warn("测试后端连接失败: {}", e.getMessage());
            return false;
        }
    }

    private ApiBackendResponse convertToResponse(ApiBackend entity) {
        ApiBackendResponse response = new ApiBackendResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
