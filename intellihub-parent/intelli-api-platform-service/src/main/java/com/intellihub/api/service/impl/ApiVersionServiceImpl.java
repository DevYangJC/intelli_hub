package com.intellihub.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.api.dto.request.CreateApiVersionRequest;
import com.intellihub.api.dto.response.ApiVersionResponse;
import com.intellihub.api.entity.ApiInfo;
import com.intellihub.api.entity.ApiVersion;
import com.intellihub.api.mapper.ApiInfoMapper;
import com.intellihub.api.mapper.ApiVersionMapper;
import com.intellihub.api.service.ApiVersionService;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API版本服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiVersionServiceImpl implements ApiVersionService {

    private final ApiVersionMapper versionMapper;
    private final ApiInfoMapper apiInfoMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ApiVersionResponse> listVersions(String apiId) {
        LambdaQueryWrapper<ApiVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiVersion::getApiId, apiId)
                .orderByDesc(ApiVersion::getCreatedAt);
        return versionMapper.selectList(wrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApiVersionResponse getVersion(String versionId) {
        ApiVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }
        return convertToResponse(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiVersionResponse createVersion(String apiId, String userId, CreateApiVersionRequest request) {
        // 获取当前API信息
        ApiInfo apiInfo = apiInfoMapper.selectById(apiId);
        if (apiInfo == null) {
            throw new BusinessException("API不存在");
        }

        // 检查版本号是否已存在
        LambdaQueryWrapper<ApiVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiVersion::getApiId, apiId)
                .eq(ApiVersion::getVersion, request.getVersion());
        if (versionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("版本号已存在");
        }

        // 创建版本快照
        String snapshot;
        try {
            snapshot = objectMapper.writeValueAsString(apiInfo);
        } catch (JsonProcessingException e) {
            log.error("序列化API信息失败", e);
            throw new BusinessException("创建版本快照失败");
        }

        ApiVersion version = ApiVersion.builder()
                .apiId(apiId)
                .version(request.getVersion())
                .snapshot(snapshot)
                .changeLog(request.getChangeLog())
                .createdBy(userId)
                .build();

        versionMapper.insert(version);
        return convertToResponse(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackToVersion(String apiId, String versionId) {
        ApiVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException("版本不存在");
        }

        if (!version.getApiId().equals(apiId)) {
            throw new BusinessException("版本与API不匹配");
        }

        // 从快照恢复API信息
        try {
            ApiInfo snapshotApi = objectMapper.readValue(version.getSnapshot(), ApiInfo.class);
            ApiInfo currentApi = apiInfoMapper.selectById(apiId);
            if (currentApi == null) {
                throw new BusinessException("API不存在");
            }

            // 恢复字段（保留ID和时间戳）
            snapshotApi.setId(currentApi.getId());
            snapshotApi.setCreatedAt(currentApi.getCreatedAt());
            snapshotApi.setDeletedAt(null);
            apiInfoMapper.updateById(snapshotApi);

            log.info("API {} 已回滚到版本 {}", apiId, version.getVersion());
        } catch (JsonProcessingException e) {
            log.error("反序列化API快照失败", e);
            throw new BusinessException("回滚版本失败");
        }
    }

    @Override
    public void deleteVersion(String versionId) {
        versionMapper.deleteById(versionId);
    }

    @Override
    public String compareVersions(String versionId1, String versionId2) {
        ApiVersion v1 = versionMapper.selectById(versionId1);
        ApiVersion v2 = versionMapper.selectById(versionId2);

        if (v1 == null || v2 == null) {
            throw new BusinessException("版本不存在");
        }

        // 简单返回两个版本的快照，前端可以做diff展示
        return String.format("{\"version1\": %s, \"version2\": %s}", v1.getSnapshot(), v2.getSnapshot());
    }

    private ApiVersionResponse convertToResponse(ApiVersion entity) {
        ApiVersionResponse response = new ApiVersionResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
