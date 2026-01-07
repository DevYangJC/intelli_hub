package com.intellihub.aigc.provider;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;

/**
 * AI模型提供商接口
 * 统一不同厂商的API调用
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface ModelProviderService {

    /**
     * 文本生成
     *
     * @param request 请求参数
     * @return 生成结果
     */
    TextGenerationResponse generateText(TextGenerationRequest request);

    /**
     * 对话聊天
     *
     * @param request 请求参数
     * @return 对话结果
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProviderName();

    /**
     * 判断是否支持指定模型
     *
     * @param modelName 模型名称
     * @return 是否支持
     */
    boolean supportsModel(String modelName);
}
