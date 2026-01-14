package com.intellihub.aigc.controller;

import com.intellihub.aigc.model.AlertAnalysisRequest;
import com.intellihub.aigc.model.AlertAnalysisResponse;
import com.intellihub.aigc.service.AlertAnalysisService;
import com.intellihub.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 告警智能分析接口
 *
 * @author IntelliHub
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/aigc/alert")
@RequiredArgsConstructor
@Validated
@Tag(name = "告警智能分析", description = "基于AI的告警分析接口")
public class AlertAnalysisController {

    private final AlertAnalysisService alertAnalysisService;

    /**
     * 分析告警
     */
    @PostMapping("/analyze")
    @Operation(summary = "分析告警", description = "基于AI分析告警信息，提供根因分析和处理建议")
    public ApiResponse<AlertAnalysisResponse> analyzeAlert(@Valid @RequestBody AlertAnalysisRequest request) {
        log.info("收到告警分析请求: alertId={}, ruleName={}", request.getAlertId(), request.getRuleName());
        
        AlertAnalysisResponse response = alertAnalysisService.analyzeAlert(request);
        
        return ApiResponse.success(response);
    }

    /**
     * 快速分析（简化版）
     */
    @PostMapping("/quick-analyze")
    @Operation(summary = "快速分析", description = "快速分析告警，只返回摘要和建议")
    public ApiResponse<AlertAnalysisResponse> quickAnalyze(
            @RequestParam String ruleName,
            @RequestParam String ruleType,
            @RequestParam String alertLevel,
            @RequestParam String alertMessage,
            @RequestParam(required = false) String apiPath) {
        
        log.info("收到快速告警分析请求: ruleName={}, ruleType={}", ruleName, ruleType);
        
        AlertAnalysisRequest request = AlertAnalysisRequest.builder()
                .ruleName(ruleName)
                .ruleType(ruleType)
                .alertLevel(alertLevel)
                .alertMessage(alertMessage)
                .apiPath(apiPath)
                .build();
        
        AlertAnalysisResponse response = alertAnalysisService.analyzeAlert(request);
        
        return ApiResponse.success(response);
    }
}
