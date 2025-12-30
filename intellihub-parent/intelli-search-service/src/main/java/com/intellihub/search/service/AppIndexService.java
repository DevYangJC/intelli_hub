package com.intellihub.search.service;

import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import com.intellihub.elasticsearch.model.SearchRequest;
import com.intellihub.elasticsearch.model.SearchResponse;
import com.intellihub.search.constant.IndexConstants;
import com.intellihub.search.model.doc.AppDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用索引服务
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppIndexService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @PostConstruct
    public void initIndex() {
        try {
            if (!elasticsearchTemplate.indexExists(IndexConstants.INDEX_APP)) {
                Map<String, Object> mappings = new HashMap<>();
                elasticsearchTemplate.createIndex(IndexConstants.INDEX_APP, mappings);
                log.info("应用索引创建成功: {}", IndexConstants.INDEX_APP);
            } else {
                log.info("应用索引已存在: {}", IndexConstants.INDEX_APP);
            }
        } catch (Exception e) {
            log.warn("初始化应用索引失败（ES 可能未启动）: {}", e.getMessage());
        }
    }

    public void indexApp(AppDoc appDoc) {
        elasticsearchTemplate.index(IndexConstants.INDEX_APP, appDoc.getId(), appDoc);
        log.debug("索引应用文档: id={}", appDoc.getId());
    }

    public void bulkIndexApps(List<AppDoc> appDocs) {
        if (appDocs == null || appDocs.isEmpty()) {
            return;
        }
        Map<String, AppDoc> documents = new HashMap<>();
        appDocs.forEach(doc -> documents.put(doc.getId(), doc));
        elasticsearchTemplate.bulkIndex(IndexConstants.INDEX_APP, documents);
        log.info("批量索引应用文档完成: count={}", appDocs.size());
    }

    public void deleteApp(String appId) {
        elasticsearchTemplate.delete(IndexConstants.INDEX_APP, appId);
        log.debug("删除应用文档: id={}", appId);
    }

    public SearchResponse<AppDoc> searchApps(String keyword, String tenantId,
                                              Map<String, Object> filters,
                                              int page, int size,
                                              boolean highlight) {
        SearchRequest request = new SearchRequest()
                .setKeyword(keyword)
                .setSearchFields(Arrays.asList(IndexConstants.APP_SEARCH_FIELDS))
                .setPage(page)
                .setSize(size)
                .setHighlight(highlight);

        Map<String, Object> allFilters = new HashMap<>();
        if (filters != null) {
            allFilters.putAll(filters);
        }
        if (tenantId != null) {
            allFilters.put("tenantId", tenantId);
        }
        request.setFilters(allFilters);

        Map<String, String> sorts = new HashMap<>();
        sorts.put("updatedAt", "desc");
        request.setSorts(sorts);

        return elasticsearchTemplate.search(IndexConstants.INDEX_APP, request, AppDoc.class);
    }

    public AppDoc getApp(String appId) {
        return elasticsearchTemplate.get(IndexConstants.INDEX_APP, appId, AppDoc.class);
    }
}
