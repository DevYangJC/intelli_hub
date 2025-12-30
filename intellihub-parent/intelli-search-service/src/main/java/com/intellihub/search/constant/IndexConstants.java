package com.intellihub.search.constant;

/**
 * 索引常量
 *
 * @author IntelliHub
 */
public final class IndexConstants {

    private IndexConstants() {}

    /**
     * API 索引
     */
    public static final String INDEX_API = "idx_api";

    /**
     * 应用索引
     */
    public static final String INDEX_APP = "idx_app";

    /**
     * 用户索引
     */
    public static final String INDEX_USER = "idx_user";

    /**
     * 审计日志索引
     */
    public static final String INDEX_AUDIT = "idx_audit";

    /**
     * 告警索引
     */
    public static final String INDEX_ALERT = "idx_alert";

    /**
     * API 搜索字段
     */
    public static final String[] API_SEARCH_FIELDS = {"name", "path", "description", "tags"};

    /**
     * 应用搜索字段
     */
    public static final String[] APP_SEARCH_FIELDS = {"appName", "description", "appKey"};

    /**
     * 用户搜索字段
     */
    public static final String[] USER_SEARCH_FIELDS = {"username", "nickname", "email"};
}
