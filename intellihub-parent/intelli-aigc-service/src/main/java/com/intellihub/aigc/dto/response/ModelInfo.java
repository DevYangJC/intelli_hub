package com.intellihub.aigc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI模型信息DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo {

    /**
     * 模型标识（用于API调用）
     */
    private String id;

    /**
     * 模型显示名称
     */
    private String name;

    /**
     * 模型提供商
     * aliyun / baidu / tencent
     */
    private String provider;

    /**
     * 提供商显示名称
     */
    private String providerName;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 最大上下文长度
     */
    private Integer maxContextLength;

    /**
     * 是否支持流式输出
     */
    private Boolean supportStream;

    /**
     * 每千Token价格（元）
     */
    private Double pricePerThousandTokens;
}
