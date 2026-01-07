package com.intellihub.aigc.enums;

import lombok.Getter;

/**
 * AI模型类型枚举
 *
 * @author intellihub
 * @since 1.0.0
 */
@Getter
public enum ModelType {

    /**
     * 文本生成
     */
    TEXT("text", "文本生成"),

    /**
     * 对话聊天
     */
    CHAT("chat", "对话聊天"),

    /**
     * 图片生成
     */
    IMAGE("image", "图片生成"),

    /**
     * 向量化
     */
    EMBEDDING("embedding", "向量化");

    private final String code;
    private final String name;

    ModelType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
