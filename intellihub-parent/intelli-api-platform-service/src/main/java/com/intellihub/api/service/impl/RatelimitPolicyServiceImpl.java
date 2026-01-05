package com.intellihub.api.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intellihub.api.dto.request.RatelimitPolicyApplyRequest;
import com.intellihub.api.dto.request.RatelimitPolicyCreateRequest;
import com.intellihub.api.dto.response.RatelimitPolicyResponse;
import com.intellihub.api.entity.RatelimitPolicy;
import com.intellihub.api.entity.RouteRatelimit;
import com.intellihub.api.mapper.RatelimitPolicyMapper;
import com.intellihub.api.mapper.RouteRatelimitMapper;
import com.intellihub.api.service.ApiRouteEventPublisher;
import com.intellihub.api.service.RatelimitPolicyService;
import com.intellihub.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 限流策略服务实现
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RatelimitPolicyServiceImpl implements RatelimitPolicyService {

    private final RatelimitPolicyMapper policyMapper;
    private final RouteRatelimitMapper routeRatelimitMapper;
    private final ApiRouteEventPublisher eventPublisher;

    @Override
    public Page<RatelimitPolicyResponse> listPolicies(int page, int size, String keyword, String status) {
        Page<RatelimitPolicy> pageParam = new Page<>(page, size);
        
        // 租户条件由拦截器自动添加
        LambdaQueryWrapper<RatelimitPolicy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, RatelimitPolicy::getStatus, status)
               .and(keyword != null && !keyword.isEmpty(), w -> 
                   w.like(RatelimitPolicy::getName, keyword)
                    .or()
                    .like(RatelimitPolicy::getDescription, keyword)
               )
               .isNull(RatelimitPolicy::getDeletedAt)
               .orderByDesc(RatelimitPolicy::getCreatedAt);
        
        Page<RatelimitPolicy> result = policyMapper.selectPage(pageParam, wrapper);
        
        Page<RatelimitPolicyResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<RatelimitPolicyResponse> responses = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responses);
        
        return responsePage;
    }

    @Override
    public RatelimitPolicyResponse getPolicyById(String id) {
        RatelimitPolicy policy = policyMapper.selectById(id);
        if (policy == null || policy.getDeletedAt() != null) {
            throw new RuntimeException("限流策略不存在");
        }
        return toResponse(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPolicy(RatelimitPolicyCreateRequest request) {
        String userId = UserContextHolder.getCurrentUserId();
        String tenantId = UserContextHolder.getCurrentTenantId();
        
        RatelimitPolicy policy = new RatelimitPolicy();
        BeanUtils.copyProperties(request, policy);
        policy.setId(IdUtil.simpleUUID());
        policy.setTenantId(tenantId);
        policy.setStatus("active");
        policy.setCreatedBy(userId);
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());
        
        policyMapper.insert(policy);
        
        log.info("创建限流策略成功 - policyId: {}, name: {}", policy.getId(), policy.getName());
        return policy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicy(String id, RatelimitPolicyCreateRequest request) {
        RatelimitPolicy policy = policyMapper.selectById(id);
        if (policy == null || policy.getDeletedAt() != null) {
            throw new RuntimeException("限流策略不存在");
        }
        
        BeanUtils.copyProperties(request, policy);
        policy.setUpdatedAt(LocalDateTime.now());
        
        policyMapper.updateById(policy);
        
        // 发布路由刷新事件
        eventPublisher.publishRouteRefresh();
        
        log.info("更新限流策略成功 - policyId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePolicy(String id) {
        RatelimitPolicy policy = policyMapper.selectById(id);
        if (policy == null) {
            throw new RuntimeException("限流策略不存在");
        }
        
        // 软删除
        policy.setDeletedAt(LocalDateTime.now());
        policyMapper.updateById(policy);
        
        // 删除关联关系
        routeRatelimitMapper.deleteByPolicyId(id);
        
        // 发布路由刷新事件
        eventPublisher.publishRouteRefresh();
        
        log.info("删除限流策略成功 - policyId: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyPolicyToRoutes(String policyId, RatelimitPolicyApplyRequest request) {
        RatelimitPolicy policy = policyMapper.selectById(policyId);
        if (policy == null || policy.getDeletedAt() != null) {
            throw new RuntimeException("限流策略不存在");
        }
        
        // 删除旧的关联关系
        routeRatelimitMapper.deleteByPolicyId(policyId);
        
        // 创建新的关联关系
        for (String routeId : request.getRouteIds()) {
            RouteRatelimit relation = new RouteRatelimit();
            relation.setId(IdUtil.simpleUUID());
            relation.setRouteId(routeId);
            relation.setPolicyId(policyId);
            relation.setCreatedAt(LocalDateTime.now());
            routeRatelimitMapper.insert(relation);
        }
        
        // 发布路由刷新事件
        eventPublisher.publishRouteRefresh();
        
        log.info("应用限流策略成功 - policyId: {}, routeCount: {}", policyId, request.getRouteIds().size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePolicyFromRoute(String policyId, String routeId) {
        LambdaQueryWrapper<RouteRatelimit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteRatelimit::getPolicyId, policyId)
               .eq(RouteRatelimit::getRouteId, routeId);
        
        routeRatelimitMapper.delete(wrapper);
        
        // 发布路由刷新事件
        eventPublisher.publishRouteRefresh();
        
        log.info("移除路由限流策略成功 - policyId: {}, routeId: {}", policyId, routeId);
    }

    private RatelimitPolicyResponse toResponse(RatelimitPolicy policy) {
        RatelimitPolicyResponse response = new RatelimitPolicyResponse();
        BeanUtils.copyProperties(policy, response);
        
        // 统计应用的路由数量
        int appliedRoutes = routeRatelimitMapper.countByPolicyId(policy.getId());
        response.setAppliedRoutes(appliedRoutes);
        
        return response;
    }
}
