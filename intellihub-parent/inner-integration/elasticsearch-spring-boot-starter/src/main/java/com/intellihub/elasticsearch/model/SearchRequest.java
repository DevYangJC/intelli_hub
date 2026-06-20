package com.intellihub.elasticsearch.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一搜索请求
 *
 * @author IntelliHub
 */
@Data
@Accessors(chain = true)
public class SearchRequest {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索字段列表（为空则搜索所有字段）
     */
    private List<String> searchFields;

    /**
     * 精确匹配过滤条件
     */
    private Map<String, Object> filters = new HashMap<>();

    /**
     * 范围过滤条件（字段名 -> [min, max]）
     */
    private Map<String, Object[]> rangeFilters = new HashMap<>();

    /**
     * 排序字段（字段名 -> asc/desc）
     */
    private Map<String, String> sorts = new HashMap<>();

    /**
     * 页码（从1开始）
     */
    private int page = 1;

    /**
     * 每页大小
     */
    private int size = 20;

    /**
     * 是否高亮
     */
    private boolean highlight = false;

    /**
     * 高亮前缀标签
     */
    private String highlightPreTag = "<em>";

    /**
     * 高亮后缀标签
     */
    private String highlightPostTag = "</em>";

    /**
     * 租户ID（多租户隔离）
     */
    private Long tenantId;

    /**
     * 计算偏移量
     */
    public int getFrom() {
        return (page - 1) * size;
    }
}
