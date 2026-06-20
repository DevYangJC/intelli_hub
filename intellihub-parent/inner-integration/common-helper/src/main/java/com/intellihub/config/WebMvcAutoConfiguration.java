package com.intellihub.config;

import com.intellihub.interceptor.UserContextInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 自动配置
 * 自动注册 UserContextInterceptor 以支持多租户上下文传递
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcAutoConfiguration implements WebMvcConfigurer {

    /**
     * 创建 UserContextInterceptor Bean
     * 确保在没有被组件扫描到时也能正常工作
     */
    @Bean
    @ConditionalOnMissingBean
    public UserContextInterceptor userContextInterceptor() {
        log.info("创建多租户上下文拦截器 Bean");
        return new UserContextInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册多租户上下文拦截器到 MVC");
        registry.addInterceptor(userContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                    // 健康检查端点
                    "/actuator/**",
                    "/*/health",
                    "/health",
                    // API 文档
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    // 静态资源
                    "/static/**",
                    "/favicon.ico",
                    // 内部日志上报接口（无需租户上下文，从消息体获取）
                    "/governance/v1/logs/report",
                    "/governance/v1/logs/report/batch"
                );
    }
}
