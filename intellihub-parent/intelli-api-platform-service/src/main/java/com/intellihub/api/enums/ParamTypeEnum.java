package com.intellihub.api.enums;

import lombok.Getter;

/**
 * API参数类型枚举
 * 定义API参数的类型（请求参数/响应参数）
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum ParamTypeEnum {

    /**
     * 请求参数：API的输入参数
     */
    REQUEST("REQUEST", "请求参数"),

    /**
     * 响应参数：API的输出参数
     */
    RESPONSE("RESPONSE", "响应参数");

    private final String code;
    private final String description;

    ParamTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static ParamTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ParamTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的类型码
     *
     * @param code 类型码
     * @return true-有效，false-无效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
