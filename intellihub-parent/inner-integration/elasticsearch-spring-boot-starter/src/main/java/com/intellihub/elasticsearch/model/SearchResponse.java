package com.intellihub.elasticsearch.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一搜索响应
 *
 * @author IntelliHub
 * @param <T> 文档类型
 */
@Data
@Accessors(chain = true)
public class SearchResponse<T> {

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
    private List<SearchHit<T>> hits = new ArrayList<>();

    /**
     * 聚合结果
     */
    private Map<String, Map<String, Long>> aggregations = new HashMap<>();

    /**
     * 搜索耗时（毫秒）
     */
    private long took;

    /**
     * 计算总页数
     */
    public int getTotalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    /**
     * 单条搜索结果
     */
    @Data
    @Accessors(chain = true)
    public static class SearchHit<T> {
        /**
         * 文档ID
         */
        private String id;

        /**
         * 索引名称
         */
        private String index;

        /**
         * 相关性得分
         */
        private Double score;

        /**
         * 文档内容
         */
        private T source;

        /**
         * 高亮字段
         */
        private Map<String, List<String>> highlights = new HashMap<>();
    }
}
