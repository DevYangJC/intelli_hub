package com.intellihub.search.constant;

/**
 * 搜索类型枚举
 *
 * @author IntelliHub
 */
public enum SearchType {

    /**
     * API
     */
    API("api", "API接口"),

    /**
     * 应用
     */
    APP("app", "应用"),

    /**
     * 用户
     */
    USER("user", "用户"),

    /**
     * 审计日志
     */
    AUDIT("audit", "审计日志"),

    /**
     * 告警
     */
    ALERT("alert", "告警");

    private final String code;
    private final String description;

    SearchType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static SearchType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SearchType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查 code 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
