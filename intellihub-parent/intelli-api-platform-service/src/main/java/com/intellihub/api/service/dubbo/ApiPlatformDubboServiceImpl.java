package com.intellihub.api.service.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intellihub.api.entity.ApiBackend;
import com.intellihub.api.entity.ApiInfo;
import com.intellihub.api.mapper.ApiBackendMapper;
import com.intellihub.api.mapper.ApiInfoMapper;
import com.intellihub.dubbo.ApiCallCountDTO;
import com.intellihub.dubbo.ApiPlatformDubboService;
import com.intellihub.dubbo.ApiRouteDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * API平台Dubbo服务实现
 * <p>
 * 提供API路由配置查询功能，供网关动态路由使用
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@DubboService
@RequiredArgsConstructor
public class ApiPlatformDubboServiceImpl implements ApiPlatformDubboService {

    private final ApiInfoMapper apiInfoMapper;
    private final ApiBackendMapper apiBackendMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String STATUS_PUBLISHED = "published";

    @Override
    public ApiRouteDTO getRouteByPath(String path, String method) {
        log.debug("查询API路由配置 - path: {}, method: {}", path, method);

        // 查询已发布的API
        LambdaQueryWrapper<ApiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiInfo::getPath, path)
                .eq(ApiInfo::getMethod, method.toUpperCase())
                .eq(ApiInfo::getStatus, STATUS_PUBLISHED)
                .isNull(ApiInfo::getDeletedAt);

        ApiInfo apiInfo = apiInfoMapper.selectOne(queryWrapper);
        if (apiInfo == null) {
            log.debug("未找到API路由配置 - path: {}, method: {}", path, method);
            return null;
        }

        return buildRouteDTO(apiInfo);
    }

    @Override
    public List<ApiRouteDTO> getAllPublishedRoutes() {
        log.debug("获取所有已发布的API路由配置");

        LambdaQueryWrapper<ApiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiInfo::getStatus, STATUS_PUBLISHED)
                .isNull(ApiInfo::getDeletedAt);

        List<ApiInfo> apiInfoList = apiInfoMapper.selectList(queryWrapper);
        List<ApiRouteDTO> routes = new ArrayList<>();

        for (ApiInfo apiInfo : apiInfoList) {
            ApiRouteDTO route = buildRouteDTO(apiInfo);
            if (route != null) {
                routes.add(route);
            }
        }

        log.info("加载已发布API路由配置，共 {} 条", routes.size());
        return routes;
    }

    @Override
    public ApiRouteDTO getRouteByApiId(String apiId) {
        log.debug("根据API ID查询路由配置 - apiId: {}", apiId);

        ApiInfo apiInfo = apiInfoMapper.selectById(apiId);
        if (apiInfo == null || !STATUS_PUBLISHED.equals(apiInfo.getStatus())) {
            return null;
        }

        return buildRouteDTO(apiInfo);
    }

    @Override
    public boolean isApiPublished(String path, String method) {
        LambdaQueryWrapper<ApiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiInfo::getPath, path)
                .eq(ApiInfo::getMethod, method.toUpperCase())
                .eq(ApiInfo::getStatus, STATUS_PUBLISHED)
                .isNull(ApiInfo::getDeletedAt);

        return apiInfoMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public ApiRouteDTO matchRouteByPath(String requestPath, String method) {
        log.debug("匹配API路由配置 - requestPath: {}, method: {}", requestPath, method);

        // 1. 先尝试精确匹配
        ApiRouteDTO exactMatch = getRouteByPath(requestPath, method);
        if (exactMatch != null) {
            return exactMatch;
        }

        // 2. 获取所有已发布的API，进行通配符匹配
        LambdaQueryWrapper<ApiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiInfo::getStatus, STATUS_PUBLISHED)
                .isNull(ApiInfo::getDeletedAt);

        // 方法匹配
        queryWrapper.and(w -> w.eq(ApiInfo::getMethod, method.toUpperCase())
                .or().eq(ApiInfo::getMethod, "ALL"));

        List<ApiInfo> apiInfoList = apiInfoMapper.selectList(queryWrapper);

        for (ApiInfo apiInfo : apiInfoList) {
            // 使用AntPathMatcher匹配路径（支持 {id}、* 等通配符）
            if (pathMatcher.match(apiInfo.getPath(), requestPath)) {
                log.debug("通配符匹配成功 - requestPath: {}, pattern: {}", requestPath, apiInfo.getPath());
                return buildRouteDTO(apiInfo);
            }
        }

        log.debug("未找到匹配的API路由 - requestPath: {}, method: {}", requestPath, method);
        return null;
    }

    /**
     * 构建路由DTO
     */
    private ApiRouteDTO buildRouteDTO(ApiInfo apiInfo) {
        // 获取后端配置
        LambdaQueryWrapper<ApiBackend> backendQuery = new LambdaQueryWrapper<>();
        backendQuery.eq(ApiBackend::getApiId, apiInfo.getId());
        ApiBackend backend = apiBackendMapper.selectOne(backendQuery);

        ApiRouteDTO dto = new ApiRouteDTO();
        dto.setApiId(apiInfo.getId());
        dto.setTenantId(apiInfo.getTenantId());
        dto.setApiName(apiInfo.getName());
        dto.setPath(apiInfo.getPath());
        dto.setMethod(apiInfo.getMethod());
        dto.setAuthType(apiInfo.getAuthType());
        dto.setTimeout(apiInfo.getTimeout());
        dto.setMockEnabled(apiInfo.getMockEnabled());
        dto.setMockResponse(apiInfo.getMockResponse());
        dto.setRateLimitEnabled(apiInfo.getRateLimitEnabled());
        dto.setRateLimitQps(apiInfo.getRateLimitQps());

        // 设置后端配置
        if (backend != null) {
            dto.setBackendType(backend.getType());
            dto.setBackendProtocol(backend.getProtocol());
            dto.setBackendHost(backend.getHost());
            dto.setBackendPath(backend.getPath());
            dto.setBackendMethod(backend.getMethod());

            // Dubbo配置
            if ("dubbo".equalsIgnoreCase(backend.getType())) {
                dto.setDubboInterface(backend.getInterfaceName());
                dto.setDubboMethod(backend.getMethodName());
                dto.setDubboVersion(backend.getDubboVersion());
                dto.setDubboGroup(backend.getDubboGroup());
            }

            // 覆盖超时配置
            if (backend.getTimeout() != null) {
                dto.setTimeout(backend.getTimeout());
            }
        } else {
            log.warn("API {} 没有后端配置", apiInfo.getId());
        }

        return dto;
    }

    /**
     * 批量更新API调用次数
     * <p>
     * 根据传入的调用统计列表，更新 api_info 表的 today_calls 和 total_calls 字段
     * </p>
     *
     * @param callCounts API调用次数统计列表
     * @return 更新成功的记录数
     */
    @Override
    public int batchUpdateApiCallCounts(List<ApiCallCountDTO> callCounts) {
        if (callCounts == null || callCounts.isEmpty()) {
            log.info("[调用次数同步] 无需更新的API调用次数");
            return 0;
        }

        log.info("[调用次数同步] 开始批量更新API调用次数，共 {} 条", callCounts.size());
        int successCount = 0;

        for (ApiCallCountDTO dto : callCounts) {
            try {
                // 查询API是否存在
                ApiInfo apiInfo = apiInfoMapper.selectById(dto.getApiId());
                if (apiInfo == null) {
                    log.warn("[调用次数同步] API不存在，跳过: apiId={}", dto.getApiId());
                    continue;
                }

                // 更新调用次数
                apiInfo.setTodayCalls(dto.getTodayCalls() != null ? dto.getTodayCalls() : 0L);
                apiInfo.setTotalCalls(dto.getTotalCalls() != null ? dto.getTotalCalls() : 0L);
                
                int affected = apiInfoMapper.updateById(apiInfo);
                if (affected > 0) {
                    successCount++;
                    log.debug("[调用次数同步] API调用次数更新成功: apiId={}, todayCalls={}, totalCalls={}", 
                            dto.getApiId(), dto.getTodayCalls(), dto.getTotalCalls());
                }
            } catch (Exception e) {
                log.error("[调用次数同步] 更新API调用次数失败: apiId={}", dto.getApiId(), e);
            }
        }

        log.info("[调用次数同步] API调用次数批量更新完成，成功 {} / {} 条", successCount, callCounts.size());
        return successCount;
    }
}
