package com.intellihub.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域配置
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfig = new CorsConfiguration();
        // 允许所有源
        corsConfig.addAllowedOriginPattern("*");
        // 允许所有头
        corsConfig.addAllowedHeader("*");
        // 允许所有方法
        corsConfig.addAllowedMethod("*");
        // 允许携带凭证
        corsConfig.setAllowCredentials(true);
        // 预检请求的有效期，单位为秒
        corsConfig.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}