package com.intellihub.search.constant;

/**
 * 用户状态枚举
 *
 * @author IntelliHub
 */
public enum UserStatus {

    ACTIVE("active", "正常"),
    INACTIVE("inactive", "未激活"),
    LOCKED("locked", "已锁定");

    private final String code;
    private final String name;

    UserStatus(String code, String name) {
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
        for (UserStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status.name;
            }
        }
        return "未知";
    }
}
