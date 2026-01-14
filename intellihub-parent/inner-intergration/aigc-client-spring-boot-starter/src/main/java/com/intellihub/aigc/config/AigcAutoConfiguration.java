package com.intellihub.aigc.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.intellihub.aigc.client.QianfanClient;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * AIGC自动配置
 *
 * @author IntelliHub
 */
@Configuration
@EnableConfigurationProperties(QianfanProperties.class)
@ConditionalOnProperty(prefix = "aigc.qianfan", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AigcAutoConfiguration {

    /**
     * OkHttp客户端
     */
    @Bean
    @ConditionalOnMissingBean(name = "aigcOkHttpClient")
    public OkHttpClient aigcOkHttpClient(QianfanProperties properties) {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(properties.getTimeout() * 2L, TimeUnit.SECONDS)
                .writeTimeout(properties.getTimeout(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Jackson ObjectMapper（AIGC专用）
     */
    @Bean
    @ConditionalOnMissingBean(name = "aigcObjectMapper")
    public ObjectMapper aigcObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    /**
     * 千帆客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public QianfanClient qianfanClient(QianfanProperties properties,
                                       OkHttpClient aigcOkHttpClient,
                                       ObjectMapper aigcObjectMapper) {
        return new QianfanClient(properties, aigcOkHttpClient, aigcObjectMapper);
    }
}
