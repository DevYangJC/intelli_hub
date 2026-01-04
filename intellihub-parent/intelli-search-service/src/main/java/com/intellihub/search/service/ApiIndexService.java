package com.intellihub.search.service;

import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import com.intellihub.elasticsearch.model.SearchRequest;
import com.intellihub.elasticsearch.model.SearchResponse;
import com.intellihub.search.constant.IndexConstants;
import com.intellihub.search.model.doc.ApiDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 索引服务
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiIndexService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 初始化索引
     */
    @PostConstruct
    public void initIndex() {
        try {
            if (!elasticsearchTemplate.indexExists(IndexConstants.INDEX_API)) {
                Map<String, Object> mappings = buildApiMappings();
                elasticsearchTemplate.createIndex(IndexConstants.INDEX_API, mappings);
                log.info("API 索引创建成功: {}", IndexConstants.INDEX_API);
            } else {
                log.info("API 索引已存在: {}", IndexConstants.INDEX_API);
            }
        } catch (Exception e) {
            log.error("初始化 API 索引失败: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch初始化失败,服务无法启动", e);
        }
    }

    /**
     * 构建API索引映射
     */
    private Map<String, Object> buildApiMappings() {
        Map<String, Object> mappings = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 文本字段 - 使用中文分词
        properties.put("name", createTextMapping("ik_max_word", "ik_smart"));
        properties.put("description", createTextMapping("ik_max_word", "ik_smart"));
        
        // 关键字字段
        properties.put("id", createKeywordMapping());
        properties.put("tenantId", createKeywordMapping());
        properties.put("code", createKeywordMapping());
        properties.put("path", createKeywordMapping());
        properties.put("method", createKeywordMapping());
        properties.put("protocol", createKeywordMapping());
        properties.put("groupId", createKeywordMapping());
        properties.put("groupName", createKeywordMapping());
        properties.put("status", createKeywordMapping());
        properties.put("statusName", createKeywordMapping());
        properties.put("authType", createKeywordMapping());
        properties.put("version", createKeywordMapping());
        properties.put("createdBy", createKeywordMapping());
        properties.put("creatorName", createKeywordMapping());
        
        // 数值字段 (可选,用于统计和排序)
        properties.put("timeout", createTypeMapping("integer"));
        properties.put("todayCalls", createTypeMapping("long"));
        properties.put("totalCalls", createTypeMapping("long"));
        
        // 布尔字段 (可选,用于筛选)
        properties.put("mockEnabled", createTypeMapping("boolean"));
        properties.put("rateLimitEnabled", createTypeMapping("boolean"));
        
        // 日期字段
        properties.put("createdAt", createDateMapping());
        properties.put("updatedAt", createDateMapping());
        properties.put("publishedAt", createDateMapping());
        
        mappings.put("properties", properties);
        return mappings;
    }
    
    private Map<String, Object> createTextMapping(String analyzer, String searchAnalyzer) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("type", "text");
        mapping.put("analyzer", analyzer);
        mapping.put("search_analyzer", searchAnalyzer);
        return mapping;
    }
    
    private Map<String, Object> createKeywordMapping() {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("type", "keyword");
        return mapping;
    }
    
    private Map<String, Object> createTypeMapping(String type) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("type", type);
        return mapping;
    }
    
    private Map<String, Object> createDateMapping() {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("type", "date");
        // 支持 ISO-8601 格式 (Jackson 默认格式) 和其他常见格式
        mapping.put("format", "strict_date_optional_time||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
        return mapping;
    }

    /**
     * 索引单个 API 文档
     */
    public void indexApi(ApiDoc apiDoc) {
        elasticsearchTemplate.index(IndexConstants.INDEX_API, apiDoc.getId(), apiDoc);
        log.debug("索引 API 文档: id={}", apiDoc.getId());
    }

    /**
     * 批量索引 API 文档
     */
    public void bulkIndexApis(List<ApiDoc> apiDocs) {
        if (apiDocs == null || apiDocs.isEmpty()) {
            return;
        }
        Map<String, ApiDoc> documents = new HashMap<>();
        apiDocs.forEach(doc -> documents.put(doc.getId(), doc));
        elasticsearchTemplate.bulkIndex(IndexConstants.INDEX_API, documents);
        log.info("批量索引 API 文档完成: count={}", apiDocs.size());
    }

    /**
     * 删除 API 文档
     */
    public void deleteApi(String apiId) {
        elasticsearchTemplate.delete(IndexConstants.INDEX_API, apiId);
        log.debug("删除 API 文档: id={}", apiId);
    }

    /**
     * 根据租户删除所有 API 文档
     */
    public long deleteApisByTenant(String tenantId) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("tenantId", tenantId);
        return elasticsearchTemplate.deleteByQuery(IndexConstants.INDEX_API, filters);
    }

    /**
     * 搜索 API
     */
    public SearchResponse<ApiDoc> searchApis(String keyword, String tenantId, 
                                              Map<String, Object> filters, 
                                              int page, int size, 
                                              boolean highlight) {
        SearchRequest request = new SearchRequest()
                .setKeyword(keyword)
                .setSearchFields(Arrays.asList(IndexConstants.API_SEARCH_FIELDS))
                .setPage(page)
                .setSize(size)
                .setHighlight(highlight);

        // 添加租户过滤
        Map<String, Object> allFilters = new HashMap<>();
        if (filters != null) {
            allFilters.putAll(filters);
        }
        if (tenantId != null) {
            allFilters.put("tenantId", tenantId);
        }
        request.setFilters(allFilters);

        // 默认按更新时间降序
        Map<String, String> sorts = new HashMap<>();
        sorts.put("updatedAt", "desc");
        request.setSorts(sorts);

        return elasticsearchTemplate.search(IndexConstants.INDEX_API, request, ApiDoc.class);
    }

    /**
     * 获取单个 API 文档
     */
    public ApiDoc getApi(String apiId) {
        return elasticsearchTemplate.get(IndexConstants.INDEX_API, apiId, ApiDoc.class);
    }
}
