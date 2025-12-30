package com.intellihub.elasticsearch.core;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.indices.*;
import com.intellihub.elasticsearch.config.ElasticsearchProperties;
import com.intellihub.elasticsearch.exception.ElasticsearchException;
import com.intellihub.elasticsearch.model.SearchRequest;
import com.intellihub.elasticsearch.model.SearchResponse;
import lombok.extern.slf4j.Slf4j;
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

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;

    public ElasticsearchTemplate(ElasticsearchClient client, ElasticsearchProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    /**
     * 获取原生客户端
     */
    public ElasticsearchClient getClient() {
        return client;
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

            CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder()
                    .index(fullIndexName)
                    .settings(s -> s
                            .numberOfShards(String.valueOf(properties.getDefaultShards()))
                            .numberOfReplicas(String.valueOf(properties.getDefaultReplicas())));

            CreateIndexResponse response = client.indices().create(builder.build());
            log.info("创建索引成功: {}", fullIndexName);
            return response.acknowledged();
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

            DeleteIndexResponse response = client.indices().delete(d -> d.index(fullIndexName));
            log.info("删除索引成功: {}", fullIndexName);
            return response.acknowledged();
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
            return client.indices().exists(e -> e.index(fullIndexName)).value();
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
            IndexRequest.Builder<T> builder = new IndexRequest.Builder<T>()
                    .index(fullIndexName)
                    .document(document)
                    .refresh(getRefresh());

            if (StringUtils.hasText(id)) {
                builder.id(id);
            }

            IndexResponse response = client.index(builder.build());
            log.debug("索引文档成功: index={}, id={}", fullIndexName, response.id());
            return response.id();
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
        List<BulkOperation> operations = documents.entrySet().stream()
                .map(entry -> BulkOperation.of(op -> op
                        .index(idx -> idx
                                .index(fullIndexName)
                                .id(entry.getKey())
                                .document(entry.getValue()))))
                .collect(Collectors.toList());

        try {
            BulkResponse response = client.bulk(b -> b
                    .operations(operations)
                    .refresh(getRefresh()));

            if (response.errors()) {
                long failedCount = response.items().stream()
                        .filter(item -> item.error() != null)
                        .count();
                log.warn("批量索引部分失败: total={}, failed={}", documents.size(), failedCount);
                throw new ElasticsearchException(
                        ElasticsearchException.ErrorCode.BULK_PARTIAL_FAILURE,
                        "批量索引部分失败，失败数: " + failedCount);
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
            GetResponse<T> response = client.get(g -> g
                    .index(fullIndexName)
                    .id(id), clazz);

            if (!response.found()) {
                return null;
            }
            return response.source();
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
            DeleteResponse response = client.delete(d -> d
                    .index(fullIndexName)
                    .id(id)
                    .refresh(getRefresh()));

            return "deleted".equals(response.result().jsonValue());
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
            Query query = buildFilterQuery(filters);

            DeleteByQueryResponse response = client.deleteByQuery(d -> d
                    .index(fullIndexName)
                    .query(query)
                    .refresh(true));

            return response.deleted() != null ? response.deleted() : 0;
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
    public <T> SearchResponse<T> search(String indexName, SearchRequest request, Class<T> clazz) {
        String fullIndexName = getFullIndexName(indexName);
        try {
            co.elastic.clients.elasticsearch.core.SearchRequest.Builder searchBuilder =
                    new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                            .index(fullIndexName)
                            .from(request.getFrom())
                            .size(request.getSize());

            // 构建查询
            Query query = buildSearchQuery(request);
            searchBuilder.query(query);

            // 排序
            if (!CollectionUtils.isEmpty(request.getSorts())) {
                request.getSorts().forEach((field, order) ->
                        searchBuilder.sort(s -> s.field(f -> f
                                .field(field)
                                .order("desc".equalsIgnoreCase(order) ? SortOrder.Desc : SortOrder.Asc))));
            }

            // 高亮
            if (request.isHighlight() && !CollectionUtils.isEmpty(request.getSearchFields())) {
                searchBuilder.highlight(h -> {
                    h.preTags(request.getHighlightPreTag())
                            .postTags(request.getHighlightPostTag());
                    request.getSearchFields().forEach(field ->
                            h.fields(field, HighlightField.of(hf -> hf)));
                    return h;
                });
            }

            // 执行搜索
            co.elastic.clients.elasticsearch.core.SearchResponse<T> response =
                    client.search(searchBuilder.build(), clazz);

            // 构建响应
            return buildSearchResponse(response, request);
        } catch (IOException e) {
            throw new ElasticsearchException(
                    ElasticsearchException.ErrorCode.UNKNOWN,
                    "搜索失败: " + fullIndexName, e);
        }
    }

    /**
     * 构建搜索查询
     */
    private Query buildSearchQuery(SearchRequest request) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            if (!CollectionUtils.isEmpty(request.getSearchFields())) {
                boolBuilder.must(m -> m.multiMatch(mm -> mm
                        .query(request.getKeyword())
                        .fields(request.getSearchFields())));
            } else {
                boolBuilder.must(m -> m.queryString(qs -> qs
                        .query("*" + request.getKeyword() + "*")));
            }
        }

        // 精确过滤
        if (!CollectionUtils.isEmpty(request.getFilters())) {
            request.getFilters().forEach((field, value) -> {
                if (value instanceof Collection) {
                    Collection<?> values = (Collection<?>) value;
                    List<FieldValue> fieldValues = values.stream()
                            .map(v -> FieldValue.of(String.valueOf(v)))
                            .collect(Collectors.toList());
                    boolBuilder.filter(f -> f.terms(t -> t
                            .field(field)
                            .terms(tv -> tv.value(fieldValues))));
                } else {
                    boolBuilder.filter(f -> f.term(t -> t
                            .field(field)
                            .value(String.valueOf(value))));
                }
            });
        }

        // 租户隔离
        if (request.getTenantId() != null) {
            boolBuilder.filter(f -> f.term(t -> t
                    .field("tenantId")
                    .value(request.getTenantId())));
        }

        // 范围过滤
        if (!CollectionUtils.isEmpty(request.getRangeFilters())) {
            request.getRangeFilters().forEach((field, range) -> {
                if (range != null && range.length == 2) {
                    boolBuilder.filter(f -> f.range(r -> {
                        r.field(field);
                        if (range[0] != null) {
                            r.gte(co.elastic.clients.json.JsonData.of(range[0]));
                        }
                        if (range[1] != null) {
                            r.lte(co.elastic.clients.json.JsonData.of(range[1]));
                        }
                        return r;
                    }));
                }
            });
        }

        return Query.of(q -> q.bool(boolBuilder.build()));
    }

    /**
     * 构建过滤查询
     */
    private Query buildFilterQuery(Map<String, Object> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        filters.forEach((field, value) ->
                boolBuilder.filter(f -> f.term(t -> t
                        .field(field)
                        .value(String.valueOf(value)))));

        return Query.of(q -> q.bool(boolBuilder.build()));
    }

    /**
     * 构建搜索响应
     */
    private <T> SearchResponse<T> buildSearchResponse(
            co.elastic.clients.elasticsearch.core.SearchResponse<T> response,
            SearchRequest request) {

        SearchResponse<T> result = new SearchResponse<>();
        result.setTotal(response.hits().total() != null ? response.hits().total().value() : 0);
        result.setPage(request.getPage());
        result.setSize(request.getSize());
        result.setTook(response.took());

        List<SearchResponse.SearchHit<T>> hits = new ArrayList<>();
        for (Hit<T> hit : response.hits().hits()) {
            SearchResponse.SearchHit<T> searchHit = new SearchResponse.SearchHit<>();
            searchHit.setId(hit.id());
            searchHit.setIndex(hit.index());
            searchHit.setScore(hit.score());
            searchHit.setSource(hit.source());

            // 处理高亮
            if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                searchHit.setHighlights(hit.highlight());
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
            return client.ping().value();
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
            CountResponse response = client.count(c -> c.index(fullIndexName));
            return response.count();
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
    private Refresh getRefresh() {
        switch (properties.getRefreshPolicy()) {
            case "true":
                return Refresh.True;
            case "wait_for":
                return Refresh.WaitFor;
            default:
                return Refresh.False;
        }
    }
}
