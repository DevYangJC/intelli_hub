package com.intellihub.constants;

/**
 * 通用常量
 *
 * @author intellihub
 * @since 1.0.0
 */
public class CommonConstants {

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户ID请求头
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 租户ID请求头
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";

    private CommonConstants() {
    }
}
