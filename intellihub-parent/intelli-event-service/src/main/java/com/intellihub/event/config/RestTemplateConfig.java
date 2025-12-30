package com.intellihub.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 配置 HTTP 客户端，用于 Webhook 订阅的 HTTP 回调
 * 设置连接超时和读取超时，防止请求卡死
 *
 * @author IntelliHub
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建 RestTemplate Bean
     * 配置超时参数：
     * - 连接超时：5 秒
     * - 读取超时：30 秒
     *
     * @return 配置好的 RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 连接超时 5 秒
        factory.setReadTimeout(30000);    // 读取超时 30 秒
        return new RestTemplate(factory);
    }
}
