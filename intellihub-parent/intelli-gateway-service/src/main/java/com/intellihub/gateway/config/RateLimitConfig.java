package com.intellihub.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流配置类（简化版）
 * <p>
 * 使用 IP+Path 组合维度限流，性能最优，配置简单
 * </p>
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
     * 默认限流配置（每分钟100次请求）
     * 限流维度：IP+Path 组合，即同一IP访问同一路径的限制
     */
    private Limit defaultLimit = new Limit(100, 60);

    /**
     * 针对特定路径的限流配置
     * 示例：
     * "/api/auth/**": {requests: 5, window: 60}  - 登录接口每分钟5次
     * "/api/search/**": {requests: 200, window: 60}  - 搜索接口每分钟200次
     */
    private Map<String, Limit> limits = new HashMap<>();

    /**
     * 限流算法类型
     * SLIDING_WINDOW: 滑动窗口（推荐，流量平滑，无临界突刺）
     * FIXED_WINDOW: 固定窗口（性能高，但有临界突刺问题）
     * TOKEN_BUCKET: 令牌桶（支持突发流量）
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
        /**
         * 滑动窗口算法（推荐）
         * 优点：流量平滑，无临界突刺问题
         * 缺点：需要存储时间戳，内存占用略高
         */
        SLIDING_WINDOW,
        
        /**
         * 固定窗口算法
         * 优点：实现简单，性能高
         * 缺点：窗口边界可能出现2倍流量
         */
        FIXED_WINDOW,
        
        /**
         * 令牌桶算法
         * 优点：支持突发流量，流量整形效果好
         * 缺点：实现复杂，参数调优难度大
         */
        TOKEN_BUCKET
    }

    /**
     * 限流配置内部类
     */
    @Data
    public static class Limit {
        /**
         * 限流次数（同一IP访问同一路径在时间窗口内的最大请求数）
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