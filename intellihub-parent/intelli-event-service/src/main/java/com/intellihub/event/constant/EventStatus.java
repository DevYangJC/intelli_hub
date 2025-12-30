package com.intellihub.event.constant;

/**
 * 事件状态枚举
 *
 * @author IntelliHub
 */
public enum EventStatus {

    ACTIVE("ACTIVE", "激活"),
    INACTIVE("INACTIVE", "未激活"),
    PAUSED("PAUSED", "暂停");

    private final String code;
    private final String name;

    EventStatus(String code, String name) {
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
        for (EventStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status.name;
            }
        }
        return "未知";
    }
}
