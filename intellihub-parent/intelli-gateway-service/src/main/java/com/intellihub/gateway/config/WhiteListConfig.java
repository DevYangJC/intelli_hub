package com.intellihub.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Arrays;
import java.util.List;

/**
 * 白名单配置类
 * 统一管理不需要认证的路径
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "intellihub.gateway.whitelist")
@Data
public class WhiteListConfig {

    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径列表
     */
    private List<String> paths = Arrays.asList(
            // 健康检查
            "/actuator/**",
            "/health/**",
            
            // 认证相关接口
            "/api/auth/**",
            "/api/iam/v1/auth/login",
            "/api/iam/v1/auth/register",
            "/api/iam/v1/auth/captcha",
            "/api/iam/v1/auth/refresh",
            "/iam/v1/auth/login",
            "/iam/v1/auth/register",
            "/iam/v1/auth/captcha",
            "/iam/v1/auth/refresh",
            
            // API文档
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/doc.html",
            
            // 公开接口
            "/",
            "/api/public/**",
            
            // 开放API（跳过JWT认证，使用AppKey认证）
            "/open/**",
            "/external/**",
            "/api/open/**",
            "/api/external/**"
    );

    /**
     * 判断路径是否在白名单中
     *
     * @param path 请求路径
     * @return 是否在白名单中
     */
    public boolean isWhiteListPath(String path) {
        return paths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
