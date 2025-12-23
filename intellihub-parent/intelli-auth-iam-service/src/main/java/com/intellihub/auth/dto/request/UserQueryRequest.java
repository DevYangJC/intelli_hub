package com.intellihub.auth.dto.request;

import lombok.Data;

/**
 * 用户查询请求
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
public class UserQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 20;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 角色筛选
     */
    private String role;

    /**
     * 状态筛选
     */
    private String status;
}
