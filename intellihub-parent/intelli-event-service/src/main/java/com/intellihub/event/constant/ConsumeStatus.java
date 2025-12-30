package com.intellihub.event.constant;

/**
 * 消费状态枚举
 *
 * @author IntelliHub
 */
public enum ConsumeStatus {

    PENDING("PENDING", "待处理"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    RETRYING("RETRYING", "重试中");

    private final String code;
    private final String name;

    ConsumeStatus(String code, String name) {
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
        for (ConsumeStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status.name;
            }
        }
        return "未知";
    }
}
