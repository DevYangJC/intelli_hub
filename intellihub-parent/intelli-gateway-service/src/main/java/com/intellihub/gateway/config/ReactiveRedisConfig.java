package com.intellihub.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * 响应式Redis配置类
 * <p>
 * 配置ReactiveStringRedisTemplate用于网关的响应式Redis操作
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
public class ReactiveRedisConfig {

    /**
     * 配置ReactiveStringRedisTemplate
     * <p>
     * 用于网关的响应式Redis操作，包括限流、缓存等
     * </p>
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }
}
