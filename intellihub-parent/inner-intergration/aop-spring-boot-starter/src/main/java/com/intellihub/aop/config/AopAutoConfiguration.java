package com.intellihub.aop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.aop.aspect.ApiLogAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AOP自动配置类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
public class AopAutoConfiguration {

    /**
     * API日志切面
     * 通过 intellihub.aop.api-log.enabled=true 开启（默认开启）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "intellihub.aop.api-log", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApiLogAspect apiLogAspect(ObjectMapper objectMapper) {
        return new ApiLogAspect(objectMapper);
    }
}
