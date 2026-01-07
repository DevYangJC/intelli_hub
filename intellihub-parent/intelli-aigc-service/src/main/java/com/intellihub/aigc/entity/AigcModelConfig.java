package com.intellihub.aigc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AIGC模型配置实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@TableName("aigc_model_config")
public class AigcModelConfig {

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
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型（text/chat/image/embedding）
     */
    private String modelType;

    /**
     * 提供商（aliyun_qwen/baidu_ernie/tencent_hunyuan）
     */
    private String provider;

    /**
     * API密钥（加密存储）
     */
    private String apiKey;

    /**
     * API端点
     */
    private String apiEndpoint;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 默认温度参数
     */
    private Float temperature;

    /**
     * 状态（active/disabled）
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
