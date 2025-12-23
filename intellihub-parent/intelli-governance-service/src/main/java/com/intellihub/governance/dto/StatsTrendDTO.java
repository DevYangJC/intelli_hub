package com.intellihub.governance.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统计趋势DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class StatsTrendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 时间点列表
     */
    private List<String> timePoints;

    /**
     * 调用量列表
     */
    private List<Long> totalCounts;

    /**
     * 成功量列表
     */
    private List<Long> successCounts;

    /**
     * 失败量列表
     */
    private List<Long> failCounts;

    /**
     * 平均延迟列表
     */
    private List<Integer> avgLatencies;

    /**
     * 成功率列表(%)
     */
    private List<Double> successRates;
}
