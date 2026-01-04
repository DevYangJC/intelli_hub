package com.intellihub.search.service;

import com.intellihub.elasticsearch.model.SearchResponse;
import com.intellihub.search.constant.SearchType;
import com.intellihub.search.model.doc.ApiDoc;
import com.intellihub.search.model.doc.AppDoc;
import com.intellihub.search.model.doc.UserDoc;
import com.intellihub.search.model.dto.AggregateSearchRequest;
import com.intellihub.search.model.dto.AggregateSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 聚合搜索服务
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregateSearchService {

    private final ApiIndexService apiIndexService;
    private final AppIndexService appIndexService;
    private final UserIndexService userIndexService;

    /**
     * 聚合搜索
     */
    public AggregateSearchResponse aggregateSearch(AggregateSearchRequest request, String tenantId) {
        long startTime = System.currentTimeMillis();

        AggregateSearchResponse response = new AggregateSearchResponse()
                .setPage(request.getPage())
                .setSize(request.getSize());

        List<AggregateSearchResponse.SearchItem> allItems = new ArrayList<>();
        Map<String, Long> typeCounts = new HashMap<>();

        List<String> types = request.getTypes();
        if (CollectionUtils.isEmpty(types)) {
            types = Arrays.asList(
                    SearchType.API.getCode(),
                    SearchType.APP.getCode(),
                    SearchType.USER.getCode()
            );
        }

        // 搜索各类型数据
        for (String type : types) {
            SearchType searchType = SearchType.fromCode(type);
            if (searchType == null) {
                log.warn("未知的搜索类型: {}", type);
                continue;
            }

            switch (searchType) {
                case API:
                    searchApis(request, tenantId, allItems, typeCounts);
                    break;
                case APP:
                    searchApps(request, tenantId, allItems, typeCounts);
                    break;
                case USER:
                    searchUsers(request, tenantId, allItems, typeCounts);
                    break;
                case AUDIT:
                    // TODO: 后续实现 AuditIndexService
                    typeCounts.put(SearchType.AUDIT.getCode(), 0L);
                    break;
                case ALERT:
                    // TODO: 后续实现 AlertIndexService
                    typeCounts.put(SearchType.ALERT.getCode(), 0L);
                    break;
                default:
                    log.warn("未处理的搜索类型: {}", searchType);
            }
        }

        // 按相关性得分排序
        allItems.sort((a, b) -> {
            Double scoreA = a.getScore() != null ? a.getScore() : 0.0;
            Double scoreB = b.getScore() != null ? b.getScore() : 0.0;
            return scoreB.compareTo(scoreA);
        });

        // ES已在各服务层面分页,这里直接使用结果
        // 计算总数(从各类型的count累加)
        long total = typeCounts.values().stream().mapToLong(Long::longValue).sum();
        
        response.setTotal(total);
        response.setItems(allItems);
        response.setTotalPages((int) Math.ceil((double) total / request.getSize()));

        // 分面统计
        Map<String, Map<String, Long>> facets = new HashMap<>();
        facets.put("types", typeCounts);
        response.setFacets(facets);

        response.setTook(System.currentTimeMillis() - startTime);
        log.info("聚合搜索完成: keyword={}, total={}, took={}ms", 
                request.getKeyword(), total, response.getTook());

        return response;
    }

    /**
     * 搜索 API
     */
    private void searchApis(AggregateSearchRequest request, String tenantId,
                            List<AggregateSearchResponse.SearchItem> items,
                            Map<String, Long> typeCounts) {
        try {
            SearchResponse<ApiDoc> apiResponse = apiIndexService.searchApis(
                    request.getKeyword(),
                    tenantId,
                    request.getFilters(),
                    request.getPage(),
                    request.getSize(),
                    request.isHighlight()
            );

            typeCounts.put(SearchType.API.getCode(), apiResponse.getTotal());

            for (SearchResponse.SearchHit<ApiDoc> hit : apiResponse.getHits()) {
                ApiDoc apiDoc = hit.getSource();
                AggregateSearchResponse.SearchItem item = new AggregateSearchResponse.SearchItem()
                        .setType(SearchType.API.getCode())
                        .setId(hit.getId())
                        .setName(apiDoc != null ? apiDoc.getName() : null)
                        .setDescription(apiDoc != null ? apiDoc.getDescription() : null)
                        .setScore(hit.getScore())
                        .setHighlights(hit.getHighlights())
                        .setData(apiDoc);
                items.add(item);
            }
        } catch (Exception e) {
            log.error("搜索 API 失败: {}", e.getMessage());
            typeCounts.put(SearchType.API.getCode(), 0L);
        }
    }

    /**
     * 搜索应用
     */
    private void searchApps(AggregateSearchRequest request, String tenantId,
                            List<AggregateSearchResponse.SearchItem> items,
                            Map<String, Long> typeCounts) {
        try {
            SearchResponse<AppDoc> appResponse = appIndexService.searchApps(
                    request.getKeyword(),
                    tenantId,
                    request.getFilters(),
                    request.getPage(),
                    request.getSize(),
                    request.isHighlight()
            );

            typeCounts.put(SearchType.APP.getCode(), appResponse.getTotal());

            for (SearchResponse.SearchHit<AppDoc> hit : appResponse.getHits()) {
                AppDoc appDoc = hit.getSource();
                AggregateSearchResponse.SearchItem item = new AggregateSearchResponse.SearchItem()
                        .setType(SearchType.APP.getCode())
                        .setId(hit.getId())
                        .setName(appDoc != null ? appDoc.getName() : null)
                        .setDescription(appDoc != null ? appDoc.getDescription() : null)
                        .setScore(hit.getScore())
                        .setHighlights(hit.getHighlights())
                        .setData(appDoc);
                items.add(item);
            }
        } catch (Exception e) {
            log.error("搜索应用失败: {}", e.getMessage());
            typeCounts.put(SearchType.APP.getCode(), 0L);
        }
    }

    /**
     * 搜索用户
     */
    private void searchUsers(AggregateSearchRequest request, String tenantId,
                             List<AggregateSearchResponse.SearchItem> items,
                             Map<String, Long> typeCounts) {
        try {
            SearchResponse<UserDoc> userResponse = userIndexService.searchUsers(
                    request.getKeyword(),
                    tenantId,
                    request.getFilters(),
                    request.getPage(),
                    request.getSize(),
                    request.isHighlight()
            );

            typeCounts.put(SearchType.USER.getCode(), userResponse.getTotal());

            for (SearchResponse.SearchHit<UserDoc> hit : userResponse.getHits()) {
                UserDoc userDoc = hit.getSource();
                AggregateSearchResponse.SearchItem item = new AggregateSearchResponse.SearchItem()
                        .setType(SearchType.USER.getCode())
                        .setId(hit.getId())
                        .setName(userDoc != null ? userDoc.getNickname() : null)
                        .setDescription(userDoc != null ? userDoc.getUsername() : null)
                        .setScore(hit.getScore())
                        .setHighlights(hit.getHighlights())
                        .setData(userDoc);
                items.add(item);
            }
        } catch (Exception e) {
            log.error("搜索用户失败: {}", e.getMessage());
            typeCounts.put(SearchType.USER.getCode(), 0L);
        }
    }
}
