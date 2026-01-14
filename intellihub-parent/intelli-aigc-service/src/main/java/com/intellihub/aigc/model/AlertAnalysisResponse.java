package com.intellihub.aigc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 告警分析响应
 *
 * @author IntelliHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertAnalysisResponse {

    /**
     * 告警ID
     */
    private String alertId;

    /**
     * 严重程度评估 (低/中/高/紧急)
     */
    private String severityAssessment;

    /**
     * 根因分析
     */
    private String rootCauseAnalysis;

    /**
     * 影响范围
     */
    private String impactScope;

    /**
     * 处理建议列表
     */
    private List<String> recommendations;

    /**
     * 预防措施
     */
    private List<String> preventiveMeasures;

    /**
     * 相关知识/参考
     */
    private String relatedKnowledge;

    /**
     * 分析摘要
     */
    private String summary;

    /**
     * 分析耗时(ms)
     */
    private Long analysisTimeMs;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * Token使用量
     */
    private Integer tokensUsed;
}
