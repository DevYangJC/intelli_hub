package com.intellihub.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AppKey认证配置
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.appkey")
public class AppKeyConfig {

    /**
     * 是否启用AppKey认证
     */
    private boolean enabled = true;

    /**
     * 时间戳允许的误差范围（秒），用于防重放攻击
     */
    private long timestampTolerance = 300;

    /**
     * Nonce缓存过期时间（秒）
     */
    private long nonceExpireSeconds = 600;

    /**
     * 需要AppKey认证的路径前缀
     */
    private List<String> authPaths = new ArrayList<>();

    /**
     * 跳过AppKey认证的路径
     */
    private List<String> skipPaths = new ArrayList<>();

    /**
     * 检查路径是否需要AppKey认证
     */
    public boolean requiresAppKeyAuth(String path) {
        // 先检查是否在跳过列表中
        for (String skipPath : skipPaths) {
            if (matchPath(path, skipPath)) {
                return false;
            }
        }
        
        // 检查是否在需要认证的路径中
        for (String authPath : authPaths) {
            if (matchPath(path, authPath)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 路径匹配（支持通配符）
     */
    private boolean matchPath(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        } else if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
        } else {
            return path.equals(pattern);
        }
    }
}
