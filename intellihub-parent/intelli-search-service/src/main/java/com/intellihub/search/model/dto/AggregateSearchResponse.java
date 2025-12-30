package com.intellihub.search.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚合搜索响应
 *
 * @author IntelliHub
 */
@Data
@Accessors(chain = true)
public class AggregateSearchResponse {

    /**
     * 总命中数
     */
    private long total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 搜索结果列表
     */
    private List<SearchItem> items = new ArrayList<>();

    /**
     * 分面聚合统计
     */
    private Map<String, Map<String, Long>> facets = new HashMap<>();

    /**
     * 搜索耗时（毫秒）
     */
    private long took;

    /**
     * 单条搜索结果
     */
    @Data
    @Accessors(chain = true)
    public static class SearchItem {
        /**
         * 类型（api, app, user, audit, alert）
         */
        private String type;

        /**
         * 文档ID
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 描述
         */
        private String description;

        /**
         * 相关性得分
         */
        private Double score;

        /**
         * 高亮内容
         */
        private Map<String, List<String>> highlights;

        /**
         * 原始数据
         */
        private Object data;
    }
}
