package com.intellihub.elasticsearch.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.intellihub.elasticsearch.config.ElasticsearchProperties;
import com.intellihub.elasticsearch.exception.ElasticsearchException;
import com.intellihub.elasticsearch.model.SearchRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch 操作模板类
 *
 * @author IntelliHub
 */
@Slf4j
public class ElasticsearchTemplate {

    /**
     * -- GETTER --
     *  获取原生客户端
     */
    @Getter
    private final RestHighLevelClient client;
    private final ElasticsearchProperties properties;
    private final ObjectMapper objectMapper;

    public ElasticsearchTemplate(RestHighLevelClient client, ElasticsearchProperties properties) {
        this.client = client;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ==================== 索引操作 ====================

    /**
     * 创建索引
     */
    public boolean createIndex(String indexName, Map<String, Object> mappings) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            if (indexExists(indexName)) {
                log.warn("索引已存在: {}", fullIndexName);
                return false;
            }

            CreateIndexRequest request = new CreateIndexRequest(fullIndexName);
            
            // 设置分片和副本
            Map<String, Object> settings = new HashMap<>();
            settings.put("number_of_shards", String.valueOf(properties.getDefaultShards()));
            settings.put("number_of_replicas", String.valueOf(properties.getDefaultReplicas()));
            request.settings(settings);

            // 设置映射
            if (mappings != null && !mappings.isEmpty()) {
                request.mapping(mappings);
            }

            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            log.info("创建索引成功: {}", fullIndexName);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "创建索引失败: " + fullIndexName, e);
        }
    }


    /**
     * 删除索引
     */
    public boolean deleteIndex(String indexName) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            if (!indexExists(indexName)) {
                log.warn("索引不存在: {}", fullIndexName);
                return false;
            }

            DeleteIndexRequest request = new DeleteIndexRequest(fullIndexName);
            org.elasticsearch.action.support.master.AcknowledgedResponse response = 
                    client.indices().delete(request, RequestOptions.DEFAULT);
            log.info("删除索引成功: {}", fullIndexName);
            return response.isAcknowledged();
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "删除索引失败: " + fullIndexName, e);
        }
    }

    /**
     * 检查索引是否存在
     */
    public boolean indexExists(String indexName) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            GetIndexRequest request = new GetIndexRequest(fullIndexName);
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.CONNECTION_FAILED,
                    "检查索引存在失败: " + fullIndexName, e);
        }
    }

    // ==================== 文档操作 ====================

    /**
     * 索引单个文档
     */
    public <T> String index(String indexName, String id, T document) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            IndexRequest request = new IndexRequest(fullIndexName);
            
            if (StringUtils.hasText(id)) {
                request.id(id);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = objectMapper.convertValue(document, Map.class);
            request.source(jsonMap);
            request.setRefreshPolicy(getRefreshPolicy());

            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            log.debug("索引文档成功: index={}, id={}", fullIndexName, response.getId());
            return response.getId();
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "索引文档失败: " + fullIndexName, e);
        }
    }

    /**
     * 批量索引文档
     */
    public <T> void bulkIndex(String indexName, Map<String, T> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        String fullIndexName = getFullIndexName(indexName);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(getRefreshPolicy());

        try {
            for (Map.Entry<String, T> entry : documents.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonMap = objectMapper.convertValue(entry.getValue(), Map.class);
                IndexRequest request = new IndexRequest(fullIndexName)
                        .id(entry.getKey())
                        .source(jsonMap);
                bulkRequest.add(request);
            }

            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (response.hasFailures()) {
                long failedCount = Arrays.stream(response.getItems())
                        .filter(org.elasticsearch.action.bulk.BulkItemResponse::isFailed)
                        .count();
                
                // 记录详细的失败信息
                Arrays.stream(response.getItems())
                        .filter(org.elasticsearch.action.bulk.BulkItemResponse::isFailed)
                        .limit(5)  // 只记录前5个错误
                        .forEach(item -> log.error("索引失败 [id={}]: {}", 
                                item.getId(), item.getFailureMessage()));
                
                log.warn("批量索引部分失败: total={}, failed={}", documents.size(), failedCount);
                throw new ElasticsearchException(
                        ElasticsearchException.ErrorCode.BULK_PARTIAL_FAILURE,
                        "批量索引部分失败，失败数: " + failedCount + "，详情见日志");
            }

            log.info("批量索引成功: index={}, count={}", fullIndexName, documents.size());
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "批量索引失败: " + fullIndexName, e);
        }
    }

    /**
     * 获取单个文档
     */
    public <T> T get(String indexName, String id, Class<T> clazz) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            GetRequest request = new GetRequest(fullIndexName, id);
            GetResponse response = client.get(request, RequestOptions.DEFAULT);

            if (!response.isExists()) {
                return null;
            }
            return objectMapper.readValue(response.getSourceAsString(), clazz);
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "获取文档失败: " + fullIndexName + "/" + id, e);
        }
    }

    /**
     * 删除单个文档
     */
    public boolean delete(String indexName, String id) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            DeleteRequest request = new DeleteRequest(fullIndexName, id);
            request.setRefreshPolicy(getRefreshPolicy());
            
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            return response.getResult() == DeleteResponse.Result.DELETED;
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "删除文档失败: " + fullIndexName + "/" + id, e);
        }
    }

    /**
     * 根据查询删除文档
     */
    public long deleteByQuery(String indexName, Map<String, Object> filters) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(fullIndexName);
            request.setQuery(buildFilterQuery(filters));
            request.setRefresh(true);

            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
            return response.getDeleted();
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "按条件删除失败: " + fullIndexName, e);
        }
    }

    // ==================== 搜索操作 ====================

    /**
     * 统一搜索接口
     */
    public <T> com.intellihub.elasticsearch.model.SearchResponse<T> search(String indexName, SearchRequest request, Class<T> clazz) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            org.elasticsearch.action.search.SearchRequest searchRequest = 
                    new org.elasticsearch.action.search.SearchRequest(fullIndexName);
            
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.from(request.getFrom());
            sourceBuilder.size(request.getSize());

            // 构建查询
            BoolQueryBuilder query = buildSearchQuery(request);
            sourceBuilder.query(query);

            // 排序
            if (!CollectionUtils.isEmpty(request.getSorts())) {
                request.getSorts().forEach((field, order) ->
                        sourceBuilder.sort(field, 
                                "desc".equalsIgnoreCase(order) ? SortOrder.DESC : SortOrder.ASC));
            }

            // 高亮
            if (request.isHighlight() && !CollectionUtils.isEmpty(request.getSearchFields())) {
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.preTags(request.getHighlightPreTag());
                highlightBuilder.postTags(request.getHighlightPostTag());
                request.getSearchFields().forEach(highlightBuilder::field);
                sourceBuilder.highlighter(highlightBuilder);
            }

            searchRequest.source(sourceBuilder);

            // 执行搜索
            org.elasticsearch.action.search.SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 构建响应
            return buildSearchResponse(response, request, clazz);
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "搜索失败: " + fullIndexName, e);
        }
    }

    /**
     * 构建搜索查询
     */
    private BoolQueryBuilder buildSearchQuery(SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            if (!CollectionUtils.isEmpty(request.getSearchFields())) {
                boolQuery.must(QueryBuilders.multiMatchQuery(request.getKeyword(), 
                        request.getSearchFields().toArray(new String[0])));
            } else {
                boolQuery.must(QueryBuilders.queryStringQuery("*" + request.getKeyword() + "*"));
            }
        }

        // 精确过滤
        if (!CollectionUtils.isEmpty(request.getFilters())) {
            request.getFilters().forEach((field, value) -> {
                if (value instanceof Collection) {
                    Collection<?> values = (Collection<?>) value;
                    boolQuery.filter(QueryBuilders.termsQuery(field, values));
                } else {
                    boolQuery.filter(QueryBuilders.termQuery(field, value));
                }
            });
        }

        // 租户隔离
        if (request.getTenantId() != null) {
            boolQuery.filter(QueryBuilders.termQuery("tenantId", request.getTenantId()));
        }

        // 范围过滤
        if (!CollectionUtils.isEmpty(request.getRangeFilters())) {
            request.getRangeFilters().forEach((field, range) -> {
                if (range != null && range.length == 2) {
                    org.elasticsearch.index.query.RangeQueryBuilder rangeQuery = 
                            QueryBuilders.rangeQuery(field);
                    if (range[0] != null) {
                        rangeQuery.gte(range[0]);
                    }
                    if (range[1] != null) {
                        rangeQuery.lte(range[1]);
                    }
                    boolQuery.filter(rangeQuery);
                }
            });
        }

        return boolQuery;
    }

    /**
     * 构建过滤查询
     */
    private BoolQueryBuilder buildFilterQuery(Map<String, Object> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        }

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        filters.forEach((field, value) ->
                boolQuery.filter(QueryBuilders.termQuery(field, value)));

        return boolQuery;
    }

    /**
     * 构建搜索响应
     */
    private <T> com.intellihub.elasticsearch.model.SearchResponse<T> buildSearchResponse(
            org.elasticsearch.action.search.SearchResponse response,
            SearchRequest request,
            Class<T> clazz) {

        com.intellihub.elasticsearch.model.SearchResponse<T> result = new com.intellihub.elasticsearch.model.SearchResponse<>();
        result.setTotal(response.getHits().getTotalHits() != null ? 
                response.getHits().getTotalHits().value : 0);
        result.setPage(request.getPage());
        result.setSize(request.getSize());
        result.setTook(0L);

        List<com.intellihub.elasticsearch.model.SearchResponse.SearchHit<T>> hits = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            com.intellihub.elasticsearch.model.SearchResponse.SearchHit<T> searchHit = new com.intellihub.elasticsearch.model.SearchResponse.SearchHit<>();
            searchHit.setId(hit.getId());
            searchHit.setIndex(hit.getIndex());
            searchHit.setScore(Double.valueOf(hit.getScore()));
            
            try {
                T source = objectMapper.readValue(hit.getSourceAsString(), clazz);
                searchHit.setSource(source);
            } catch (IOException e) {
                log.warn("解析文档失败: {}", hit.getId(), e);
            }

            // 处理高亮
            if (hit.getHighlightFields() != null && !hit.getHighlightFields().isEmpty()) {
                Map<String, List<String>> highlights = new HashMap<>();
                hit.getHighlightFields().forEach((field, highlightField) -> {
                    List<String> fragments = Arrays.stream(highlightField.fragments())
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    highlights.put(field, fragments);
                });
                searchHit.setHighlights(highlights);
            }

            hits.add(searchHit);
        }
        result.setHits(hits);

        return result;
    }

    // ==================== 健康检查 ====================

    /**
     * 检查 ES 连接是否正常
     */
    public boolean ping() {
        try {
            return client.ping(RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("ES ping 失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取索引文档数量
     */
    public long count(String indexName) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            org.elasticsearch.client.core.CountRequest request = 
                    new org.elasticsearch.client.core.CountRequest(fullIndexName);
            org.elasticsearch.client.core.CountResponse response = 
                    client.count(request, RequestOptions.DEFAULT);
            return response.getCount();
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "获取文档数量失败: " + fullIndexName, e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取完整索引名称（带前缀）
     */
    private String getFullIndexName(String indexName) {
        if (StringUtils.hasText(properties.getIndexPrefix())) {
            return properties.getIndexPrefix() + "_" + indexName;
        }
        return indexName;
    }

    /**
     * 获取刷新策略
     */
    private WriteRequest.RefreshPolicy getRefreshPolicy() {
        switch (properties.getRefreshPolicy()) {
            case "true":
                return WriteRequest.RefreshPolicy.IMMEDIATE;
            case "wait_for":
                return WriteRequest.RefreshPolicy.WAIT_UNTIL;
            default:
                return WriteRequest.RefreshPolicy.NONE;
        }
    }
}
