package com.intellihub.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置类
 * 配置 JSON 序列化和反序列化
 * 注册 JavaTimeModule 支持 Java 8 时间类型
 * 禁用将日期序列化为时间戳，使用 ISO-8601 格式
 *
 * @author IntelliHub
 */
@Configuration
public class JacksonConfig {

    /**
     * 创建 ObjectMapper Bean
     * 配置项：
     * 1. 注册 JavaTimeModule，支持 LocalDateTime 等 Java 8 时间类型
     * 2. 禁用 WRITE_DATES_AS_TIMESTAMPS，使用 ISO-8601 格式序列化日期
     *
     * @return 配置好的 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 支持 Java 8 时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 使用 ISO-8601 格式而不是时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
