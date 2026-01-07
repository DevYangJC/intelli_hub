package com.intellihub.aigc.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 文本生成请求DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class TextGenerationRequest {

    /**
     * 输入提示词
     */
    @NotBlank(message = "输入提示词不能为空")
    private String prompt;

    /**
     * 模型名称（如：qwen-turbo, ernie-3.5-8k, hunyuan-lite）
     */
    @NotBlank(message = "模型名称不能为空")
    private String model;

    /**
     * Provider名称（可选，如果不指定则根据model自动选择）
     * aliyunQwenProvider / baiduErnieProvider / tencentHunyuanProvider
     */
    private String provider;

    /**
     * 最大生成Token数
     */
    private Integer maxTokens = 2000;

    /**
     * 温度参数 (0-1)
     */
    private Float temperature = 0.7f;

    /**
     * 多样性参数 (0-1)
     */
    private Float topP = 0.9f;

    /**
     * 是否启用流式输出
     */
    private Boolean stream = false;
}
