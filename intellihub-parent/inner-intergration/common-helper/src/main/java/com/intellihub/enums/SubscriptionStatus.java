package com.intellihub.enums;

import lombok.Getter;

/**
 * 订阅状态枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum SubscriptionStatus {

    PENDING(0, "待审批"),
    ACTIVE(1, "生效中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已拒绝"),
    EXPIRED(4, "已过期"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String desc;

    SubscriptionStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubscriptionStatus fromCode(int code) {
        for (SubscriptionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
