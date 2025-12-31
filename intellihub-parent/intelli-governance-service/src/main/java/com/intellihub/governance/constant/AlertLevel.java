package com.intellihub.governance.constant;

import lombok.Getter;

/**
 * 告警级别枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum AlertLevel {
    
    CRITICAL("critical", "严重", "🔴"),
    WARNING("warning", "警告", "🟡"),
    INFO("info", "信息", "🔵");
    
    private final String code;
    private final String desc;
    private final String emoji;
    
    AlertLevel(String code, String desc, String emoji) {
        this.code = code;
        this.desc = desc;
        this.emoji = emoji;
    }
    
    public static AlertLevel fromCode(String code) {
        if (code == null) {
            return INFO;
        }
        for (AlertLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        return INFO;
    }
    
    public static AlertLevel determineLevel(double ratio) {
        if (ratio >= 2.0) {
            return CRITICAL;
        } else if (ratio >= 1.5) {
            return WARNING;
        } else {
            return INFO;
        }
    }
}
