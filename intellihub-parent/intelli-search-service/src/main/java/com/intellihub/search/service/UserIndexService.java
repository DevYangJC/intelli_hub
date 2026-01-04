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
                Map<String, Object> mappings = buildUserMappings();
                elasticsearchTemplate.createIndex(IndexConstants.INDEX_USER, mappings);
                log.info("用户索引创建成功: {}", IndexConstants.INDEX_USER);
            } else {
                log.info("用户索引已存在: {}", IndexConstants.INDEX_USER);
            }
        } catch (Exception e) {
            log.error("初始化用户索引失败: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch初始化失败,服务无法启动", e);
        }
    }

    /**
     * 构建用户索引映射
     */
    private Map<String, Object> buildUserMappings() {
        Map<String, Object> mappings = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        
        // 文本字段 - 使用中文分词
        properties.put("nickname", createTextMapping("ik_max_word", "ik_smart"));
        properties.put("bio", createTextMapping("ik_max_word", "ik_smart"));
        
        // 关键字字段
        properties.put("id", createKeywordMapping());
        properties.put("tenantId", createKeywordMapping());
        properties.put("username", createKeywordMapping());
        properties.put("email", createKeywordMapping());
        properties.put("phone", createKeywordMapping());
        properties.put("avatar", createKeywordMapping());
        properties.put("status", createKeywordMapping());
        properties.put("statusName", createKeywordMapping());
        properties.put("role", createKeywordMapping());
        
        // 数值字段 (可选,用于统计和排序)
        properties.put("loginCount", createTypeMapping("long"));
        
        // 布尔字段 (可选,用于筛选)
        properties.put("enabled", createTypeMapping("boolean"));
        
        // 日期字段
        properties.put("lastLoginAt", createDateMapping());
        properties.put("createdAt", createDateMapping());
        properties.put("updatedAt", createDateMapping());
        
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
        mapping.put("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
        return mapping;
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
