package com.intellihub.event.constant;

/**
 * 事件类型枚举
 *
 * @author IntelliHub
 */
public enum EventType {

    SYSTEM("SYSTEM", "系统事件"),
    BUSINESS("BUSINESS", "业务事件"),
    CUSTOM("CUSTOM", "自定义事件");

    private final String code;
    private final String name;

    EventType(String code, String name) {
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
        for (EventType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type.name;
            }
        }
        return "未知";
    }
}
