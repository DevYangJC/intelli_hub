package com.intellihub.enums;

import lombok.Getter;

/**
 * 应用类型枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum AppType {

    INTERNAL("internal", "内部应用"),
    EXTERNAL("external", "外部应用");

    private final String code;
    private final String desc;

    AppType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AppType fromCode(String code) {
        for (AppType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return EXTERNAL;
    }
}
