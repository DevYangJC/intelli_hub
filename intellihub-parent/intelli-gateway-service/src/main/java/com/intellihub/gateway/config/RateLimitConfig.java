package com.intellihub.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流配置类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "intellihub.gateway.rate-limit")
@Data
public class RateLimitConfig {

    /**
     * 是否启用限流
     */
    private boolean enabled = true;

    /**
     * 默认限流配置
     */
    private Limit defaultLimit = new Limit(100, 60); // 每分钟100个请求

    /**
     * 针对特定路径的限流配置
     */
    private Map<String, Limit> limits = new HashMap<>();

    /**
     * 限流算法类型
     */
    private Algorithm algorithm = Algorithm.SLIDING_WINDOW;

    /**
     * 限流Key的前缀
     */
    private String keyPrefix = "rate_limit:";

    /**
     * 错误提示消息
     */
    private String errorMessage = "请求过于频繁，请稍后再试";

    /**
     * 限流算法枚举
     */
    public enum Algorithm {
        SLIDING_WINDOW,  // 滑动窗口
        FIXED_WINDOW,    // 固定窗口
        TOKEN_BUCKET     // 令牌桶
    }

    /**
     * 限流配置内部类
     */
    @Data
    public static class Limit {
        /**
         * 限流次数
         */
        private int requests;

        /**
         * 时间窗口（秒）
         */
        private int window;

        public Limit() {}

        public Limit(int requests, int window) {
            this.requests = requests;
            this.window = window;
        }
    }
}