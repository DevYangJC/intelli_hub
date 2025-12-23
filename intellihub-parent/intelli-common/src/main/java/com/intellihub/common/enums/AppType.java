package com.intellihub.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.intellihub.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用类型枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AppType {

    INTERNAL("internal", "内部应用"),
    EXTERNAL("external", "外部应用");

    /**
     * 类型编码，存储到数据库
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     */
    public static AppType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AppType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new BusinessException("未知的应用类型: " + code);
    }
}
