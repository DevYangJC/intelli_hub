package com.intellihub.gateway.config;

import com.intellihub.common.event.ApiRouteChangeEvent;
import com.intellihub.gateway.listener.ApiRouteChangeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis消息监听器配置
 * <p>
 * 配置Redis发布订阅，监听API路由变更事件
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisListenerConfig {

    private final ApiRouteChangeListener apiRouteChangeListener;

    /**
     * 配置Redis消息监听器容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 订阅API路由变更频道
        container.addMessageListener(apiRouteChangeListener,
                new ChannelTopic(ApiRouteChangeEvent.CHANNEL));

        log.info("Redis消息监听器已配置 - channel: {}", ApiRouteChangeEvent.CHANNEL);

        return container;
    }
}
