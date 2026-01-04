package com.intellihub.api.constant;

/**
 * API平台服务常量类
 * 统一管理所有数字常量、默认值等，避免魔法数字
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 分页相关常量
     */
    public static final class Pagination {
        public static final int DEFAULT_PAGE = 1;
        public static final int DEFAULT_SIZE = 10;
        public static final int DEFAULT_SIZE_SMALL = 10;
        public static final int DEFAULT_SIZE_MEDIUM = 20;
        public static final int DEFAULT_SIZE_LARGE = 50;
        public static final int MAX_SIZE = 100;
    }

    /**
     * HTTP状态码常量
     */
    public static final class HttpStatus {
        public static final int OK = 200;
        public static final int BAD_REQUEST = 400;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    /**
     * 请求头常量
     */
    public static final class Headers {
        public static final String USER_ID = "X-User-Id";
        public static final String TENANT_ID = "X-Tenant-Id";
        public static final String USERNAME = "X-Username";
        public static final String TRACE_ID = "X-Trace-Id";
    }

    /**
     * 请求参数常量
     */
    public static final class RequestParams {
        public static final String PAGE = "page";
        public static final String SIZE = "size";
        public static final String KEYWORD = "keyword";
        public static final String STATUS = "status";
        public static final String LIMIT = "limit";
        public static final String NAME = "name";
        public static final String CODE = "code";
        public static final String DESCRIPTION = "description";
        public static final String COLOR = "color";
        public static final String SORT = "sort";
    }

    /**
     * API状态常量
     */
    public static final class ApiStatus {
        public static final String DRAFT = "DRAFT";
        public static final String PUBLISHED = "PUBLISHED";
        public static final String OFFLINE = "OFFLINE";
        public static final String DEPRECATED = "DEPRECATED";
    }

    /**
     * 参数类型常量
     */
    public static final class ParamType {
        public static final String REQUEST = "REQUEST";
        public static final String RESPONSE = "RESPONSE";
    }
}
