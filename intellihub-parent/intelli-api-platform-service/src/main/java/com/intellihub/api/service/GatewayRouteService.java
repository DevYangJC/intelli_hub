package com.intellihub.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.api.dto.request.GatewayRouteCreateRequest;
import com.intellihub.api.dto.request.GatewayRouteQueryRequest;
import com.intellihub.api.dto.request.GatewayRouteUpdateRequest;
import com.intellihub.api.dto.response.GatewayRouteDetailResponse;
import com.intellihub.api.dto.response.GatewayRouteListResponse;

import java.util.List;

/**
 * 网关路由管理服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface GatewayRouteService {

    /**
     * 分页查询路由列表
     */
    Page<GatewayRouteListResponse> listRoutes(GatewayRouteQueryRequest request);

    /**
     * 查询路由详情
     */
    GatewayRouteDetailResponse getRouteDetail(String id);

    /**
     * 创建路由
     */
    String createRoute(GatewayRouteCreateRequest request);

    /**
     * 更新路由
     */
    void updateRoute(String id, GatewayRouteUpdateRequest request);

    /**
     * 删除路由
     */
    void deleteRoute(String id);

    /**
     * 发布路由
     */
    void publishRoute(String id);

    /**
     * 下线路由
     */
    void offlineRoute(String id);

    /**
     * 批量操作
     */
    void batchOperation(String action, List<String> ids);

    /**
     * 校验路由唯一性
     */
    void validateRouteUniqueness(String path, String method, String excludeId);
}
