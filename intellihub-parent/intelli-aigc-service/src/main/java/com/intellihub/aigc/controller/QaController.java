package com.intellihub.aigc.controller;

import com.intellihub.aigc.model.QaRequest;
import com.intellihub.aigc.model.QaResponse;
import com.intellihub.aigc.service.QaService;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 智能问答接口
 *
 * @author IntelliHub
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/aigc/qa")
@RequiredArgsConstructor
@Validated
@Tag(name = "智能问答", description = "基于AI的问答助手接口")
public class QaController {

    private final QaService qaService;

    /**
     * 提问
     */
    @PostMapping("/ask")
    @Operation(summary = "提问", description = "向AI助手提问，获取关于API使用、错误排查等问题的解答")
    public ApiResponse<QaResponse> ask(@Valid @RequestBody QaRequest request) {
        log.info("收到问答请求: question={}", request.getQuestion());
        
        QaResponse response = qaService.ask(request);
        
        return ApiResponse.success(response);
    }

    /**
     * 快速提问（简化版）
     */
    @GetMapping("/quick")
    @Operation(summary = "快速提问", description = "快速提问，无需对话历史")
    public ApiResponse<QaResponse> quickAsk(
            @RequestParam @NotBlank(message = "问题不能为空") String question,
            @RequestParam(required = false) String questionType) {
        
        log.info("收到快速问答请求: question={}", question);
        
        QaRequest request = QaRequest.builder()
                .question(question)
                .questionType(questionType)
                .build();
        
        QaResponse response = qaService.ask(request);
        
        return ApiResponse.success(response);
    }
}
