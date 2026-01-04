package com.intellihub.gateway.service.dubbo.enums;

/**
 * 参数来源枚举
 * <p>
 * 定义Dubbo泛化调用参数的来源位置
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
public enum ParameterSource {

    /**
     * 路径参数，如 /open/app/{appkey} 中的 appkey
     */
    PATH("path", "路径参数"),

    /**
     * Query参数，如 ?name=test 中的 name
     */
    QUERY("query", "Query参数"),

    /**
     * 请求体参数，POST/PUT/PATCH 请求的 JSON Body
     */
    BODY("body", "请求体参数"),

    /**
     * 请求头参数
     */
    HEADER("header", "请求头参数");

    private final String code;
    private final String description;

    ParameterSource(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 来源代码
     * @return 对应的枚举值，未找到返回null
     */
    public static ParameterSource fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ParameterSource source : values()) {
            if (source.code.equalsIgnoreCase(code)) {
                return source;
            }
        }
        return null;
    }
}
