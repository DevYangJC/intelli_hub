package com.intellihub.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.api.dto.request.ApiParamRequest;
import com.intellihub.api.dto.request.ApiQueryRequest;
import com.intellihub.api.dto.request.CreateApiRequest;
import com.intellihub.api.dto.request.UpdateApiRequest;
import com.intellihub.api.dto.response.ApiBackendResponse;
import com.intellihub.api.dto.response.ApiInfoResponse;
import com.intellihub.api.dto.response.ApiParamResponse;
import com.intellihub.api.dto.response.ApiRouteResponse;
import com.intellihub.api.entity.*;
import com.intellihub.api.mapper.ApiBackendMapper;
import com.intellihub.api.mapper.ApiGroupMapper;
import com.intellihub.api.mapper.ApiInfoMapper;
import com.intellihub.api.mapper.ApiRequestParamMapper;
import com.intellihub.api.mapper.ApiTagMapper;
import com.intellihub.api.service.ApiEventPublisher;
import com.intellihub.api.service.ApiInfoService;
import com.intellihub.api.service.ApiRouteEventPublisher;
import com.intellihub.constants.ResponseStatus;
import com.intellihub.exception.BusinessException;
import com.intellihub.page.PageData;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API信息服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiInfoServiceImpl implements ApiInfoService {

    private final ApiInfoMapper apiInfoMapper;
    private final ApiGroupMapper apiGroupMapper;
    private final ApiRequestParamMapper apiRequestParamMapper;
    private final ApiBackendMapper apiBackendMapper;
    private final ApiTagMapper apiTagMapper;
    private final ApiRouteEventPublisher routeEventPublisher;
    private final ApiEventPublisher apiEventPublisher;

    @Override
    public PageData<ApiInfoResponse> listApis(ApiQueryRequest request) {
        log.info("查询API列表 - request: {}", request);

        // 租户条件由 MyBatis-Plus 多租户拦截器自动添加
        LambdaQueryWrapper<ApiInfo> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(ApiInfo::getName, request.getKeyword())
                    .or()
                    .like(ApiInfo::getCode, request.getKeyword())
                    .or()
                    .like(ApiInfo::getPath, request.getKeyword())
            );
        }

        // 分组筛选
        if (StringUtils.hasText(request.getGroupId())) {
            wrapper.eq(ApiInfo::getGroupId, request.getGroupId());
        }

        // 状态筛选
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ApiInfo::getStatus, request.getStatus());
        }

        // 请求方法筛选
        if (StringUtils.hasText(request.getMethod())) {
            wrapper.eq(ApiInfo::getMethod, request.getMethod());
        }

        wrapper.orderByDesc(ApiInfo::getUpdatedAt);

        IPage<ApiInfo> page = apiInfoMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                wrapper
        );

        List<ApiInfoResponse> records = page.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageData<ApiInfoResponse> pageData = new PageData<>(page.getCurrent(), page.getSize());
        pageData.setTotal(page.getTotal());
        pageData.setRecords(records);
        return pageData;
    }

    @Override
    public ApiInfoResponse getApiById(String id) {
        ApiInfo apiInfo = apiInfoMapper.selectById(id);
        if (apiInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "API不存在");
        }

        ApiInfoResponse response = convertToResponse(apiInfo);

        // 获取请求参数
        List<ApiRequestParam> params = apiRequestParamMapper.selectList(
                new LambdaQueryWrapper<ApiRequestParam>()
                        .eq(ApiRequestParam::getApiId, id)
                        .orderByAsc(ApiRequestParam::getSort)
        );
        response.setRequestParams(params.stream()
                .map(this::convertParamToResponse)
                .collect(Collectors.toList()));

        // 获取后端配置
        ApiBackend backend = apiBackendMapper.selectOne(
                new LambdaQueryWrapper<ApiBackend>().eq(ApiBackend::getApiId, id)
        );
        if (backend != null) {
            response.setBackend(convertBackendToResponse(backend));
        }

        // 获取标签
        List<ApiTag> tags = apiTagMapper.selectList(
                new LambdaQueryWrapper<ApiTag>().eq(ApiTag::getApiId, id)
        );
        response.setTags(tags.stream()
                .map(ApiTag::getTagName)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public ApiInfoResponse createApi(String userId, String username, CreateApiRequest request) {
        // 获取当前租户ID（用于实体设置）
        String tenantId = UserContextHolder.getCurrentTenantId();
        
        // 检查编码是否已存在（租户条件由拦截器自动添加）
        ApiInfo existing = apiInfoMapper.selectOne(
                new LambdaQueryWrapper<ApiInfo>()
                        .eq(ApiInfo::getCode, request.getCode())
        );
        if (existing != null) {
            throw new BusinessException(ResponseStatus.DATA_EXISTS.getCode(), "API编码已存在");
        }

        // 创建API（手动设置 tenantId 用于 INSERT）
        ApiInfo apiInfo = ApiInfo.builder()
                .tenantId(tenantId)
                .groupId(request.getGroupId())
                .name(request.getName())
                .code(request.getCode())
                .version(request.getVersion())
                .description(request.getDescription())
                .method(request.getMethod())
                .path(request.getPath())
                .protocol(request.getProtocol())
                .contentType(request.getContentType())
                .status("draft")
                .authType(request.getAuthType())
                .timeout(request.getTimeout())
                .retryCount(request.getRetryCount())
                .cacheEnabled(request.getCacheEnabled())
                .cacheTtl(request.getCacheTtl())
                .rateLimitEnabled(request.getRateLimitEnabled())
                .rateLimitQps(request.getRateLimitQps())
                .mockEnabled(request.getMockEnabled())
                .mockResponse(request.getMockResponse())
                .successResponse(request.getSuccessResponse())
                .errorResponse(request.getErrorResponse())
                .ipWhitelistEnabled(request.getIpWhitelistEnabled())
                .ipWhitelist(request.getIpWhitelist())
                .todayCalls(0L)
                .totalCalls(0L)
                .createdBy(userId)
                .creatorName(username)
                .build();

        apiInfoMapper.insert(apiInfo);

        // 保存请求参数
        if (request.getRequestParams() != null && !request.getRequestParams().isEmpty()) {
            saveRequestParams(apiInfo.getId(), request.getRequestParams());
        }

        // 保存后端配置
        if (request.getBackend() != null) {
            saveBackend(apiInfo.getId(), request.getBackend());
        }

        // 保存标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            saveTags(apiInfo.getId(), request.getTags());
        }

        return getApiById(apiInfo.getId());
    }

    @Override
    @Transactional
    public ApiInfoResponse updateApi(String id, UpdateApiRequest request) {
        ApiInfo apiInfo = apiInfoMapper.selectById(id);
        if (apiInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "API不存在");
        }

        // 使用BeanUtils复制非空属性
        copyNonNullProperties(request, apiInfo);

        apiInfoMapper.updateById(apiInfo);

        // 更新请求参数
        if (request.getRequestParams() != null) {
            // 删除旧参数
            apiRequestParamMapper.delete(new LambdaQueryWrapper<ApiRequestParam>().eq(ApiRequestParam::getApiId, id));
            // 保存新参数
            if (!request.getRequestParams().isEmpty()) {
                saveRequestParams(id, request.getRequestParams());
            }
        }

        // 更新后端配置
        if (request.getBackend() != null) {
            // 删除旧配置
            apiBackendMapper.delete(new LambdaQueryWrapper<ApiBackend>().eq(ApiBackend::getApiId, id));
            // 保存新配置
            saveBackend(id, request.getBackend());
        }

        // 更新标签
        if (request.getTags() != null) {
            // 删除旧标签
            apiTagMapper.delete(new LambdaQueryWrapper<ApiTag>().eq(ApiTag::getApiId, id));
            // 保存新标签
            if (!request.getTags().isEmpty()) {
                saveTags(id, request.getTags());
            }
        }

        return getApiById(id);
    }

    @Override
    @Transactional
    public void deleteApi(String id) {
        ApiInfo apiInfo = apiInfoMapper.selectById(id);
        if (apiInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "API不存在");
        }

        if ("published".equals(apiInfo.getStatus())) {
            throw new BusinessException(ResponseStatus.OPERATION_FAILED.getCode(), "已发布的API不能删除，请先下线");
        }

        // 删除关联数据
        apiRequestParamMapper.delete(new LambdaQueryWrapper<ApiRequestParam>().eq(ApiRequestParam::getApiId, id));
        apiBackendMapper.delete(new LambdaQueryWrapper<ApiBackend>().eq(ApiBackend::getApiId, id));
        apiTagMapper.delete(new LambdaQueryWrapper<ApiTag>().eq(ApiTag::getApiId, id));

        // 删除API（软删除）
        apiInfoMapper.deleteById(id);

        // 发布API删除事件到事件中心（Kafka）
        apiEventPublisher.publishApiDeleted(id, apiInfo.getPath(), apiInfo.getMethod(), apiInfo.getTenantId());
    }

    @Override
    public void publishApi(String id) {
        ApiInfo apiInfo = apiInfoMapper.selectById(id);
        if (apiInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "API不存在");
        }

        if ("published".equals(apiInfo.getStatus())) {
            throw new BusinessException(ResponseStatus.OPERATION_FAILED.getCode(), "API已发布");
        }

        apiInfo.setStatus("published");
        apiInfo.setPublishedAt(LocalDateTime.now());
        apiInfoMapper.updateById(apiInfo);

        // 发布路由变更事件，通知网关刷新（Redis）
        routeEventPublisher.publishApiPublished(id, apiInfo.getPath(), apiInfo.getMethod(), apiInfo.getTenantId());
        
        // 发布API发布事件到事件中心（Kafka）
        apiEventPublisher.publishApiPublished(id, apiInfo.getName(), apiInfo.getPath(), apiInfo.getMethod(), apiInfo.getTenantId());
    }

    @Override
    public void offlineApi(String id) {
        ApiInfo apiInfo = apiInfoMapper.selectById(id);
        if (apiInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "API不存在");
        }

        if (!"published".equals(apiInfo.getStatus())) {
            throw new BusinessException(ResponseStatus.OPERATION_FAILED.getCode(), "只有已发布的API才能下线");
        }

        apiInfo.setStatus("offline");
        apiInfoMapper.updateById(apiInfo);

        // 发布路由变更事件，通知网关移除路由（Redis）
        routeEventPublisher.publishApiOffline(id, apiInfo.getPath(), apiInfo.getMethod(), apiInfo.getTenantId());
        
        // 发布API下线事件到事件中心（Kafka）
        apiEventPublisher.publishApiOffline(id, apiInfo.getPath(), apiInfo.getMethod(), apiInfo.getTenantId());
    }

    @Override
    public void deprecateApi(String id) {
        ApiInfo apiInfo = apiInfoMapper.selectById(id);
        if (apiInfo == null) {
            throw new BusinessException(ResponseStatus.DATA_NOT_FOUND.getCode(), "API不存在");
        }

        apiInfo.setStatus("deprecated");
        apiInfoMapper.updateById(apiInfo);
    }

    @Override
    @Transactional
    public ApiInfoResponse copyApi(String id, String userId, String username) {
        ApiInfoResponse source = getApiById(id);

        CreateApiRequest request = new CreateApiRequest();
        request.setGroupId(source.getGroupId());
        request.setName(source.getName() + " - 副本");
        request.setCode(source.getCode() + "-copy-" + System.currentTimeMillis());
        request.setVersion(source.getVersion());
        request.setDescription(source.getDescription());
        request.setMethod(source.getMethod());
        request.setPath(source.getPath());
        request.setProtocol(source.getProtocol());
        request.setContentType(source.getContentType());
        request.setAuthType(source.getAuthType());
        request.setTimeout(source.getTimeout());
        request.setRetryCount(source.getRetryCount());
        request.setCacheEnabled(source.getCacheEnabled());
        request.setCacheTtl(source.getCacheTtl());
        request.setRateLimitEnabled(source.getRateLimitEnabled());
        request.setRateLimitQps(source.getRateLimitQps());
        request.setMockEnabled(source.getMockEnabled());
        request.setMockResponse(source.getMockResponse());

        return createApi(userId, username, request);
    }

    private void saveRequestParams(String apiId, List<ApiParamRequest> params) {
        int sort = 0;
        for (ApiParamRequest param : params) {
            ApiRequestParam entity = ApiRequestParam.builder()
                    .apiId(apiId)
                    .name(param.getName())
                    .type(param.getType())
                    .location(param.getLocation())
                    .required(param.getRequired())
                    .defaultValue(param.getDefaultValue())
                    .example(param.getExample())
                    .description(param.getDescription())
                    .sort(param.getSort() != null ? param.getSort() : sort++)
                    .build();
            apiRequestParamMapper.insert(entity);
        }
    }

    private void saveBackend(String apiId, com.intellihub.api.dto.request.ApiBackendRequest backendRequest) {
        ApiBackend backend = ApiBackend.builder()
                .apiId(apiId)
                .type(backendRequest.getType())
                .protocol(backendRequest.getProtocol())
                .method(backendRequest.getMethod())
                .host(backendRequest.getHost())
                .path(backendRequest.getPath())
                .timeout(backendRequest.getTimeout())
                .connectTimeout(backendRequest.getConnectTimeout())
                .registry(backendRequest.getRegistry())
                .interfaceName(backendRequest.getInterfaceName())
                .methodName(backendRequest.getMethodName())
                .dubboVersion(backendRequest.getDubboVersion())
                .dubboGroup(backendRequest.getDubboGroup())
                .refApiId(backendRequest.getRefApiId())
                .mockDelay(backendRequest.getMockDelay())
                .build();
        apiBackendMapper.insert(backend);
    }

    private void saveTags(String apiId, List<String> tags) {
        for (String tagName : tags) {
            ApiTag tag = ApiTag.builder()
                    .apiId(apiId)
                    .tagName(tagName)
                    .build();
            apiTagMapper.insert(tag);
        }
    }

    private ApiInfoResponse convertToResponse(ApiInfo apiInfo) {
        ApiInfoResponse response = new ApiInfoResponse();
        BeanUtils.copyProperties(apiInfo, response);

        // 获取分组名称
        if (StringUtils.hasText(apiInfo.getGroupId())) {
            ApiGroup group = apiGroupMapper.selectById(apiInfo.getGroupId());
            if (group != null) {
                response.setGroupName(group.getName());
            }
        }

        return response;
    }

    private ApiParamResponse convertParamToResponse(ApiRequestParam param) {
        ApiParamResponse response = new ApiParamResponse();
        BeanUtils.copyProperties(param, response);
        return response;
    }

    private ApiBackendResponse convertBackendToResponse(ApiBackend backend) {
        ApiBackendResponse response = new ApiBackendResponse();
        BeanUtils.copyProperties(backend, response);
        return response;
    }

    private void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        final org.springframework.beans.BeanWrapper src = new org.springframework.beans.BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        java.util.Set<String> emptyNames = new java.util.HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        // 排除不需要复制的字段
        emptyNames.add("requestParams");
        emptyNames.add("backend");
        emptyNames.add("tags");
        return emptyNames.toArray(new String[0]);
    }

    @Override
    public List<ApiRouteResponse> getPublishedApiRoutes() {
        // 查询所有已发布的API
        LambdaQueryWrapper<ApiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiInfo::getStatus, "published");
        List<ApiInfo> apis = apiInfoMapper.selectList(queryWrapper);

        return apis.stream().map(api -> {
            ApiRouteResponse route = new ApiRouteResponse();
            route.setApiId(api.getId());
            route.setApiName(api.getName());
            route.setMethod(api.getMethod());
            route.setPath(api.getPath());
            route.setStatus(api.getStatus());
            route.setAuthType(api.getAuthType());
            route.setTimeout(api.getTimeout());
            route.setMockEnabled(api.getMockEnabled());
            route.setMockResponse(api.getMockResponse());
            route.setRateLimitEnabled(api.getRateLimitEnabled());
            route.setRateLimitQps(api.getRateLimitQps());

            // 查询后端配置
            LambdaQueryWrapper<ApiBackend> backendQuery = new LambdaQueryWrapper<>();
            backendQuery.eq(ApiBackend::getApiId, api.getId());
            ApiBackend backend = apiBackendMapper.selectOne(backendQuery);
            if (backend != null) {
                route.setBackendType(backend.getType());
                route.setBackendHost(backend.getHost());
                route.setBackendPath(backend.getPath());
                route.setBackendProtocol(backend.getProtocol());
            }

            return route;
        }).collect(Collectors.toList());
    }

    @Override
    public PageData<ApiInfoResponse> listPublicApis(ApiQueryRequest request) {
        log.info("查询公开API列表（跨租户）- request: {}", request);

        LambdaQueryWrapper<ApiInfo> wrapper = new LambdaQueryWrapper<>();
        // 只查询已发布的API
        wrapper.eq(ApiInfo::getStatus, "published");

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(ApiInfo::getName, request.getKeyword())
                    .or()
                    .like(ApiInfo::getDescription, request.getKeyword())
                    .or()
                    .like(ApiInfo::getPath, request.getKeyword())
            );
        }

        // 分组筛选
        if (StringUtils.hasText(request.getGroupId())) {
            wrapper.eq(ApiInfo::getGroupId, request.getGroupId());
        }

        // 请求方法筛选
        if (StringUtils.hasText(request.getMethod())) {
            wrapper.eq(ApiInfo::getMethod, request.getMethod());
        }

        wrapper.orderByDesc(ApiInfo::getPublishedAt);

        IPage<ApiInfo> page = apiInfoMapper.selectPage(
                new Page<>(request.getPage(), request.getSize()),
                wrapper
        );

        List<ApiInfoResponse> records = page.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageData<ApiInfoResponse> pageData = new PageData<>(page.getCurrent(), page.getSize());
        pageData.setTotal(page.getTotal());
        pageData.setRecords(records);
        return pageData;
    }
}
