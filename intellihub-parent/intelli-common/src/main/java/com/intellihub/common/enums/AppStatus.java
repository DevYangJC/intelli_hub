package com.intellihub.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用状态枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AppStatus {

    ACTIVE("active", "正常"),
    DISABLED("disabled", "禁用"),
    EXPIRED("expired", "已过期");

    /**
     * 状态编码，存储到数据库
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     */
    public static AppStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AppStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的应用状态: " + code);
    }
}
