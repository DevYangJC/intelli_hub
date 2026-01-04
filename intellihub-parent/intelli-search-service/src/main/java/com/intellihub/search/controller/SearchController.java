package com.intellihub.search.controller;

import com.intellihub.ApiResponse;
import com.intellihub.constants.ResultCode;
import com.intellihub.elasticsearch.model.SearchResponse;
import com.intellihub.search.model.doc.ApiDoc;
import com.intellihub.search.model.dto.AggregateSearchRequest;
import com.intellihub.search.model.dto.AggregateSearchResponse;
import com.intellihub.search.service.AggregateSearchService;
import com.intellihub.search.service.ApiIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 搜索控制器
 *
 * @author IntelliHub
 */
@Slf4j
@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final AggregateSearchService aggregateSearchService;
    private final ApiIndexService apiIndexService;

    /**
     * 聚合搜索
     */
    @PostMapping("/aggregate")
    public ApiResponse<AggregateSearchResponse> aggregateSearch(
            @Valid @RequestBody AggregateSearchRequest request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("聚合搜索请求: keyword={}, types={}, tenantId={}", 
                request.getKeyword(), request.getTypes(), tenantId);
        AggregateSearchResponse response = aggregateSearchService.aggregateSearch(request, tenantId);
        return ApiResponse.success(response);
    }

    /**
     * 搜索 API
     */
    @GetMapping("/api")
    public ApiResponse<SearchResponse<ApiDoc>> searchApis(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean highlight,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("搜索 API: keyword={}, tenantId={}", keyword, tenantId);

        java.util.Map<String, Object> filters = new java.util.HashMap<>();
        if (status != null) {
            filters.put("status", status);
        }
        if (groupId != null) {
            filters.put("groupId", groupId);
        }

        SearchResponse<ApiDoc> response = apiIndexService.searchApis(
                keyword, tenantId, filters, page, size, highlight);
        return ApiResponse.success(response);
    }

    /**
     * 获取单个 API 详情（从索引）
     */
    @GetMapping("/api/{id}")
    public ApiResponse<ApiDoc> getApi(@PathVariable String id) {
        ApiDoc apiDoc = apiIndexService.getApi(id);
        if (apiDoc == null) {
            return ApiResponse.failed(ResultCode.API_NOT_EXIST);
        }
        return ApiResponse.success(apiDoc);
    }

    /**
     * 手动索引单个 API（管理接口）
     */
    @PostMapping("/api/index")
    public ApiResponse<String> indexApi(@RequestBody ApiDoc apiDoc) {
        apiIndexService.indexApi(apiDoc);
        return ApiResponse.success("索引成功");
    }

    /**
     * 删除 API 索引（管理接口）
     */
    @DeleteMapping("/api/{id}")
    public ApiResponse<String> deleteApiIndex(@PathVariable String id) {
        apiIndexService.deleteApi(id);
        return ApiResponse.success("删除成功");
    }
}
