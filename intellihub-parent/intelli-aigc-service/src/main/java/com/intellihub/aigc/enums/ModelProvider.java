package com.intellihub.aigc.enums;

import lombok.Getter;

/**
 * AI模型提供商枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum ModelProvider {

    /**
     * 阿里通义千问
     */
    ALIYUN_QWEN("aliyun_qwen", "阿里通义千问"),

    /**
     * 百度文心一言
     */
    BAIDU_ERNIE("baidu_ernie", "百度文心一言"),

    /**
     * 腾讯混元
     */
    TENCENT_HUNYUAN("tencent_hunyuan", "腾讯混元");

    private final String code;
    private final String name;

    ModelProvider(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ModelProvider fromCode(String code) {
        for (ModelProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("未知的模型提供商: " + code);
    }
}
