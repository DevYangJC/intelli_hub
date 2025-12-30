package com.intellihub.event.constant;

/**
 * 订阅者类型枚举
 *
 * @author IntelliHub
 */
public enum SubscriberType {

    SERVICE("SERVICE", "服务订阅"),
    WEBHOOK("WEBHOOK", "Webhook回调"),
    MQ("MQ", "消息队列");

    private final String code;
    private final String name;

    SubscriberType(String code, String name) {
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
        for (SubscriberType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type.name;
            }
        }
        return "未知";
    }
}
