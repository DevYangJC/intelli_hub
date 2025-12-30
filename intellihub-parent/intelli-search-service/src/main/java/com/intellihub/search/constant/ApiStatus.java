package com.intellihub.search.constant;

/**
 * API 状态枚举
 *
 * @author IntelliHub
 */
public enum ApiStatus {

    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布"),
    OFFLINE("offline", "已下线"),
    DEPRECATED("deprecated", "已废弃");

    private final String code;
    private final String name;

    ApiStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        if (code == null) {
            return "未知";
        }
        for (ApiStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status.name;
            }
        }
        return "未知";
    }
}
