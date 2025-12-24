package com.intellihub.enums;

import lombok.Getter;

/**
 * 应用状态枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum AppStatus {

    ACTIVE("active", "正常"),
    DISABLED("disabled", "禁用"),
    EXPIRED("expired", "已过期");

    private final String code;
    private final String desc;

    AppStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AppStatus fromCode(String code) {
        for (AppStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
