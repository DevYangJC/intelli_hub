package com.intellihub.governance.constant;

import lombok.Getter;

/**
 * 告警规则状态枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum RuleStatus {
    
    ACTIVE("active", "启用"),
    INACTIVE("inactive", "禁用"),
    DISABLED("disabled", "禁用");
    
    private final String code;
    private final String desc;
    
    RuleStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static RuleStatus fromCode(String code) {
        if (code == null) {
            return ACTIVE;
        }
        for (RuleStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
