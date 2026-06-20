package com.intellihub.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 百度千帆配置属性
 *
 * @author IntelliHub
 */
@Data
@ConfigurationProperties(prefix = "aigc.qianfan")
public class QianfanProperties {

    /**
     * API Key
     * 从百度智能云控制台获取
     */
    private String apiKey;

    /**
     * API Base URL
     */
    private String baseUrl = "https://qianfan.baidubce.com";

    /**
     * 默认模型
     */
    private String defaultModel = "ernie-4.0-8k";

    /**
     * 请求超时时间（秒）
     */
    private Integer timeout = 60;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 是否启用
     */
    private Boolean enabled = true;
}
