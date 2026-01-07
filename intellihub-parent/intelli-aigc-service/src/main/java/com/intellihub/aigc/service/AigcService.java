package com.intellihub.aigc.service;

import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;

/**
 * AIGC服务接口
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface AigcService {

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
     * 获取支持的模型列表
     *
     * @return 模型名称列表
     */
    java.util.List<String> getSupportedModels();

    /**
     * 获取模型详细信息列表
     *
     * @return 模型信息列表
     */
    java.util.List<com.intellihub.aigc.dto.response.ModelInfo> getModelInfoList();
}
