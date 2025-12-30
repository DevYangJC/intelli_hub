package com.intellihub.search.constant;

/**
 * 应用类型枚举
 *
 * @author IntelliHub
 */
public enum AppType {

    INTERNAL("internal", "内部应用"),
    EXTERNAL("external", "外部应用");

    private final String code;
    private final String name;

    AppType(String code, String name) {
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
        for (AppType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type.name;
            }
        }
        return "未知";
    }
}
