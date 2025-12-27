package com.intellihub.governance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警请求详情实体
 * <p>
 * 存储触发告警的具体请求信息
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("alert_request_detail")
public class AlertRequestDetail {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 关联的告警记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alertRecordId;

    /**
     * 请求ID(UUID)
     */
    private String requestId;

    /**
     * API路径
     */
    private String apiPath;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应延迟(ms)
     */
    private Integer latency;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
