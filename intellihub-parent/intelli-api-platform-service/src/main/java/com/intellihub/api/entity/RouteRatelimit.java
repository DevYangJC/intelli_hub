package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 路由限流策略关联实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("gateway_route_ratelimit")
public class RouteRatelimit {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String routeId;

    private String policyId;

    private LocalDateTime createdAt;
}
