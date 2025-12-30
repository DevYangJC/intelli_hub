package com.intellihub.search.constant;

/**
 * 应用状态枚举
 *
 * @author IntelliHub
 */
public enum AppStatus {

    ACTIVE("active", "正常"),
    DISABLED("disabled", "禁用"),
    EXPIRED("expired", "过期");

    private final String code;
    private final String name;

    AppStatus(String code, String name) {
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
        for (AppStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status.name;
            }
        }
        return "未知";
    }
}
