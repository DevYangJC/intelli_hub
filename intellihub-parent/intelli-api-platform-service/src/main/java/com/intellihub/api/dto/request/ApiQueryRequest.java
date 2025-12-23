package com.intellihub.api.dto.request;

import lombok.Data;

/**
 * API查询请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class ApiQueryRequest {

    /**
     * 关键词（名称/编码/路径）
     */
    private String keyword;

    /**
     * 分组ID
     */
    private String groupId;

    /**
     * 状态
     */
    private String status;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}
