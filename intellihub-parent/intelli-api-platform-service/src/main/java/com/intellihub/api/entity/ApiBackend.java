package com.intellihub.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API后端配置实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_backend")
public class ApiBackend {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * API ID
     */
    private String apiId;

    /**
     * 后端类型：http/mock/function
     */
    private String type;

    /**
     * 协议：HTTP/HTTPS
     */
    private String protocol;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 后端地址
     */
    private String host;

    /**
     * 后端路径
     */
    private String path;

    /**
     * 超时时间(ms)
     */
    private Integer timeout;

    /**
     * 连接超时(ms)
     */
    private Integer connectTimeout;

    /**
     * Dubbo注册中心地址
     */
    private String registry;

    /**
     * Dubbo接口名称
     */
    private String interfaceName;

    /**
     * Dubbo方法名称
     */
    private String methodName;

    /**
     * Dubbo服务版本
     */
    private String dubboVersion;

    /**
     * Dubbo服务分组
     */
    private String dubboGroup;

    /**
     * 引用的内部API ID
     */
    private String refApiId;

    /**
     * Mock延迟时间(ms)
     */
    private Integer mockDelay;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
