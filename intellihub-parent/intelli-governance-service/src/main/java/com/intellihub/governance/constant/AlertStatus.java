package com.intellihub.governance.constant;

import lombok.Getter;

/**
 * 告警状态枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum AlertStatus {
    
    FIRING("firing", "告警中"),
    RESOLVED("resolved", "已恢复");
    
    private final String code;
    private final String desc;
    
    AlertStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static AlertStatus fromCode(String code) {
        if (code == null) {
            return FIRING;
        }
        for (AlertStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return FIRING;
    }
}
