package com.intellihub.search.service;

import com.intellihub.elasticsearch.core.ElasticsearchTemplate;
import com.intellihub.elasticsearch.model.SearchRequest;
import com.intellihub.elasticsearch.model.SearchResponse;
import com.intellihub.search.constant.IndexConstants;
import com.intellihub.search.model.doc.UserDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户索引服务
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserIndexService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @PostConstruct
    public void initIndex() {
        try {
            if (!elasticsearchTemplate.indexExists(IndexConstants.INDEX_USER)) {
                Map<String, Object> mappings = new HashMap<>();
                elasticsearchTemplate.createIndex(IndexConstants.INDEX_USER, mappings);
                log.info("用户索引创建成功: {}", IndexConstants.INDEX_USER);
            } else {
                log.info("用户索引已存在: {}", IndexConstants.INDEX_USER);
            }
        } catch (Exception e) {
            log.warn("初始化用户索引失败（ES 可能未启动）: {}", e.getMessage());
        }
    }

    public void indexUser(UserDoc userDoc) {
        elasticsearchTemplate.index(IndexConstants.INDEX_USER, userDoc.getId(), userDoc);
        log.debug("索引用户文档: id={}", userDoc.getId());
    }

    public void bulkIndexUsers(List<UserDoc> userDocs) {
        if (userDocs == null || userDocs.isEmpty()) {
            return;
        }
        Map<String, UserDoc> documents = new HashMap<>();
        userDocs.forEach(doc -> documents.put(doc.getId(), doc));
        elasticsearchTemplate.bulkIndex(IndexConstants.INDEX_USER, documents);
        log.info("批量索引用户文档完成: count={}", userDocs.size());
    }

    public void deleteUser(String userId) {
        elasticsearchTemplate.delete(IndexConstants.INDEX_USER, userId);
        log.debug("删除用户文档: id={}", userId);
    }

    public SearchResponse<UserDoc> searchUsers(String keyword, String tenantId,
                                                Map<String, Object> filters,
                                                int page, int size,
                                                boolean highlight) {
        SearchRequest request = new SearchRequest()
                .setKeyword(keyword)
                .setSearchFields(Arrays.asList(IndexConstants.USER_SEARCH_FIELDS))
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

        return elasticsearchTemplate.search(IndexConstants.INDEX_USER, request, UserDoc.class);
    }

    public UserDoc getUser(String userId) {
        return elasticsearchTemplate.get(IndexConstants.INDEX_USER, userId, UserDoc.class);
    }
}
