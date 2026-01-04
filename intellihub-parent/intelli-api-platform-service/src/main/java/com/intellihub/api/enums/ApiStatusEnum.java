package com.intellihub.api.enums;

import lombok.Getter;

/**
 * API状态枚举
 * 定义API的生命周期状态
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum ApiStatusEnum {

    /**
     * 草稿状态：API正在编辑中，未发布
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 已发布状态：API已发布，可供调用
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 已下线状态：API已下线，不可调用
     */
    OFFLINE("OFFLINE", "已下线"),

    /**
     * 已废弃状态：API已废弃，建议使用新版本
     */
    DEPRECATED("DEPRECATED", "已废弃");

    private final String code;
    private final String description;

    ApiStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static ApiStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ApiStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的状态码
     *
     * @param code 状态码
     * @return true-有效，false-无效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
