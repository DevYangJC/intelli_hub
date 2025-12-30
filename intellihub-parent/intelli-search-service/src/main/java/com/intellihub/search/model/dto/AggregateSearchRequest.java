package com.intellihub.search.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * 聚合搜索请求
 *
 * @author IntelliHub
 */
@Data
public class AggregateSearchRequest {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索类型（api, app, user, audit, alert）
     */
    private List<String> types;

    /**
     * 过滤条件
     */
    private Map<String, Object> filters;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private int size = 20;

    /**
     * 是否高亮
     */
    private boolean highlight = true;
}
