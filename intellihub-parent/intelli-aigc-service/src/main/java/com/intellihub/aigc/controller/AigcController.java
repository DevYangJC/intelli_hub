package com.intellihub.aigc.controller;

import com.intellihub.ApiResponse;
import com.intellihub.aigc.dto.request.ChatRequest;
import com.intellihub.aigc.dto.request.TextGenerationRequest;
import com.intellihub.aigc.dto.response.ChatResponse;
import com.intellihub.aigc.dto.response.TextGenerationResponse;
import com.intellihub.aigc.entity.AigcConversation;
import com.intellihub.aigc.service.AigcService;
import com.intellihub.aigc.service.ConversationService;
import com.intellihub.aigc.service.QuotaService;
import com.intellihub.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * AIGC服务控制器
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/aigc")
@RequiredArgsConstructor
@Tag(name = "AIGC服务", description = "AI文本生成和对话接口")
public class AigcController {

    private final AigcService aigcService;
    private final QuotaService quotaService;
    private final ConversationService conversationService;

    /**
     * 文本生成
     */
    @PostMapping("/text/generate")
    @Operation(summary = "文本生成", description = "根据提示词生成文本内容")
    public ApiResponse<TextGenerationResponse> generateText(@Valid @RequestBody TextGenerationRequest request) {
        TextGenerationResponse response = aigcService.generateText(request);
        return ApiResponse.success(response);
    }

    /**
     * 对话聊天
     */
    @PostMapping("/chat/completions")
    @Operation(summary = "对话聊天", description = "支持多轮对话的智能聊天接口")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aigcService.chat(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取支持的模型列表（简单版）
     */
    @GetMapping("/models")
    @Operation(summary = "获取支持的模型列表", description = "返回所有可用的AI模型ID")
    public ApiResponse<List<String>> getSupportedModels() {
        List<String> models = aigcService.getSupportedModels();
        return ApiResponse.success(models);
    }

    /**
     * 获取模型详细信息列表
     */
    @GetMapping("/models/info")
    @Operation(summary = "获取模型详细信息", description = "返回所有可用AI模型的详细信息，包括名称、厂商、价格等")
    public ApiResponse<List<com.intellihub.aigc.dto.response.ModelInfo>> getModelInfoList() {
        List<com.intellihub.aigc.dto.response.ModelInfo> models = aigcService.getModelInfoList();
        return ApiResponse.success(models);
    }

    /**
     * 获取配额使用情况
     */
    @GetMapping("/quota/usage")
    @Operation(summary = "获取配额使用情况", description = "查询当前租户的配额使用统计")
    public ApiResponse<java.util.Map<String, Object>> getQuotaUsage() {
        String tenantId = UserContextHolder.getCurrentTenantId();
        java.util.Map<String, Object> usage = quotaService.getQuotaUsage(tenantId);
        return ApiResponse.success(usage);
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/conversation/{conversationId}/history")
    @Operation(summary = "获取对话历史", description = "查询指定会话的历史消息")
    public ApiResponse<List<AigcConversation>> getConversationHistory(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "20") int limit) {
        List<AigcConversation> history = conversationService.getHistory(conversationId, limit);
        return ApiResponse.success(history);
    }

    /**
     * 清空对话历史
     */
    @DeleteMapping("/conversation/{conversationId}/history")
    @Operation(summary = "清空对话历史", description = "删除指定会话的所有历史消息")
    public ApiResponse<Void> clearConversationHistory(@PathVariable String conversationId) {
        conversationService.clearHistory(conversationId);
        return ApiResponse.success(null);
    }
}
