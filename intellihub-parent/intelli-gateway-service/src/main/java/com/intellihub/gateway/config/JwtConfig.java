package com.intellihub.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "intellihub.gateway.auth")
public class JwtConfig {

    /**
     * 是否启用认证
     */
    private boolean enabled = true;

    /**
     * JWT密钥（需要与Auth服务保持一致）
     */
    private String secret = "intellihub-iam-jwt-secret-key-2024";

    /**
     * Token过期时间（小时）
     */
    private Integer tokenExpiration = 24;
}
