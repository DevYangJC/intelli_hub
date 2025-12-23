package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API调用日志实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("api_call_log")
public class ApiCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * API ID
     */
    private String apiId;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 请求方法
     */
    private String apiMethod;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应时间(ms)
     */
    private Integer latency;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
