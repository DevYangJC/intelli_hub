package com.intellihub.aigc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AIGC请求日志实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("aigc_request_log")
public class AigcRequestLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 使用的模型
     */
    private String modelName;

    /**
     * 提供商
     */
    private String provider;

    /**
     * 输入提示词
     */
    private String prompt;

    /**
     * 响应内容
     */
    private String response;

    /**
     * 消耗Token数
     */
    private Integer tokensUsed;

    /**
     * 成本（元）
     */
    private BigDecimal cost;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 状态（success/failed）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
