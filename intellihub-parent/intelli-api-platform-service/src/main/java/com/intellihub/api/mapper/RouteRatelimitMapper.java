package com.intellihub.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intellihub.api.entity.RouteRatelimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 路由限流策略关联Mapper
 *
 * @author intellihub
 * @since 1.0.0
 */
@Mapper
public interface RouteRatelimitMapper extends BaseMapper<RouteRatelimit> {

    /**
     * 根据策略ID删除关联
     */
    int deleteByPolicyId(@Param("policyId") String policyId);

    /**
     * 根据路由ID删除关联
     */
    int deleteByRouteId(@Param("routeId") String routeId);

    /**
     * 统计策略应用的路由数量
     */
    int countByPolicyId(@Param("policyId") String policyId);
}
