package com.intellihub.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.api.dto.response.ApiGroupResponse;
import com.intellihub.api.entity.ApiGroup;
import com.intellihub.api.entity.ApiInfo;
import com.intellihub.api.mapper.ApiGroupMapper;
import com.intellihub.api.mapper.ApiInfoMapper;
import com.intellihub.api.service.ApiGroupService;
import com.intellihub.constants.ResponseStatus;
import com.intellihub.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API分组服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGroupServiceImpl implements ApiGroupService {

    private final ApiGroupMapper apiGroupMapper;
    private final ApiInfoMapper apiInfoMapper;

    @Override
    public List<ApiGroupResponse> listGroups(String tenantId) {
        List<ApiGroup> groups = apiGroupMapper.selectList(
                new LambdaQueryWrapper<ApiGroup>()
                        .eq(ApiGroup::getTenantId, tenantId)
                        .orderByAsc(ApiGroup::getSort)
        );

        return groups.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApiGroupResponse getGroupById(String id) {
        ApiGroup group = apiGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "分组不存在");
        }
        return convertToResponse(group);
    }

    @Override
    public ApiGroupResponse createGroup(String tenantId, String userId, String name, String code, String description, String color, String status) {
        // 检查编码是否已存在
        ApiGroup existing = apiGroupMapper.selectOne(
                new LambdaQueryWrapper<ApiGroup>()
                        .eq(ApiGroup::getTenantId, tenantId)
                        .eq(ApiGroup::getCode, code)
        );
        if (existing != null) {
            throw new BusinessException(ResponseStatus.DATA_EXISTS.getCode(), "分组编码已存在");
        }

        // 获取最大排序值
        Integer maxSort = apiGroupMapper.selectList(
                new LambdaQueryWrapper<ApiGroup>()
                        .eq(ApiGroup::getTenantId, tenantId)
                        .orderByDesc(ApiGroup::getSort)
                        .last("LIMIT 1")
        ).stream().findFirst().map(ApiGroup::getSort).orElse(0);

        ApiGroup group = ApiGroup.builder()
                .tenantId(tenantId)
                .name(name)
                .code(code)
                .description(description)
                .sort(maxSort + 1)
                .color(color != null ? color : "#409EFF")
                .status(status != null ? status : "active")
                .createdBy(userId)
                .build();

        apiGroupMapper.insert(group);
        return convertToResponse(group);
    }

    @Override
    public ApiGroupResponse updateGroup(String id, String name, String description, Integer sort, String color, String status) {
        ApiGroup group = apiGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "分组不存在");
        }

        if (name != null) {
            group.setName(name);
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (sort != null) {
            group.setSort(sort);
        }
        if (color != null) {
            group.setColor(color);
        }
        if (status != null) {
            group.setStatus(status);
        }

        apiGroupMapper.updateById(group);
        return convertToResponse(group);
    }

    @Override
    public void deleteGroup(String id) {
        ApiGroup group = apiGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "分组不存在");
        }

        // 检查分组下是否有API
        Long apiCount = apiInfoMapper.selectCount(
                new LambdaQueryWrapper<ApiInfo>().eq(ApiInfo::getGroupId, id)
        );
        if (apiCount > 0) {
            throw new BusinessException(ResponseStatus.OPERATION_FAILED.getCode(), "分组下存在API，无法删除");
        }

        apiGroupMapper.deleteById(id);
    }

    private ApiGroupResponse convertToResponse(ApiGroup group) {
        ApiGroupResponse response = new ApiGroupResponse();
        BeanUtils.copyProperties(group, response);

        // 统计分组下的API数量
        Long apiCount = apiInfoMapper.selectCount(
                new LambdaQueryWrapper<ApiInfo>().eq(ApiInfo::getGroupId, group.getId())
        );
        response.setApiCount(apiCount.intValue());

        return response;
    }
}
