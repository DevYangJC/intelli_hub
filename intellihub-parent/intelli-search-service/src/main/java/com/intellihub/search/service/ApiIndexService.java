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
                Map<String, Object> mappings = new HashMap<>();
                elasticsearchTemplate.createIndex(IndexConstants.INDEX_API, mappings);
                log.info("API 索引创建成功: {}", IndexConstants.INDEX_API);
            } else {
                log.info("API 索引已存在: {}", IndexConstants.INDEX_API);
            }
        } catch (Exception e) {
            log.warn("初始化 API 索引失败（ES 可能未启动）: {}", e.getMessage());
        }
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
