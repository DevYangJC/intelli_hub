package com.intellihub.gateway.config;

import com.intellihub.constants.RedisKeyConstants;
import com.intellihub.gateway.listener.ApiRouteChangeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;

/**
 * Redis消息监听配置
 * <p>
 * 配置Redis Pub/Sub订阅，用于接收路由变更通知
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisMessageConfig {

    private final ApiRouteChangeListener apiRouteChangeListener;

    /**
     * 配置响应式Redis消息监听容器
     */
    @Bean
    public ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        ReactiveRedisMessageListenerContainer container = 
                new ReactiveRedisMessageListenerContainer(connectionFactory);

        // 订阅API路由变更频道
        container.receive(ChannelTopic.of(RedisKeyConstants.CHANNEL_API_ROUTE_CHANGE))
                .doOnNext(message -> {
                    String body = message.getMessage();
                    log.info("收到路由变更通知: {}", body);
                    apiRouteChangeListener.onRouteChange(body);
                })
                .doOnError(e -> log.error("路由变更监听异常", e))
                .subscribe();

        // 订阅应用状态变更频道
        container.receive(ChannelTopic.of(RedisKeyConstants.CHANNEL_APP_STATUS_CHANGE))
                .doOnNext(message -> {
                    String body = message.getMessage();
                    log.info("收到应用状态变更通知: {}", body);
                    apiRouteChangeListener.onAppStatusChange(body);
                })
                .doOnError(e -> log.error("应用状态变更监听异常", e))
                .subscribe();

        log.info("Redis消息监听容器已启动，订阅频道: {}, {}", 
                RedisKeyConstants.CHANNEL_API_ROUTE_CHANGE,
                RedisKeyConstants.CHANNEL_APP_STATUS_CHANGE);

        return container;
    }
}
