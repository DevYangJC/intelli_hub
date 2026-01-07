package com.intellihub.aigc.enums;

import lombok.Getter;

/**
 * 请求状态枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum RequestStatus {

    /**
     * 成功
     */
    SUCCESS("success", "成功"),

    /**
     * 失败
     */
    FAILED("failed", "失败"),

    /**
     * 处理中
     */
    PROCESSING("processing", "处理中");

    private final String code;
    private final String name;

    RequestStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
