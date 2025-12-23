package com.intellihub.gateway.util;

import com.intellihub.gateway.config.JwtConfig;
import com.intellihub.gateway.dto.UserContext;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 网关JWT工具类
 * 用于本地验证JWT Token，避免每次请求都调用Auth服务
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        String secret = jwtConfig.getSecret();
        // 确保密钥长度足够（至少256位，即32字节）
        if (secret.length() < 32) {
            secret = secret + "0000000000000000000000000000000000000000".substring(0, 32 - secret.length());
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT工具类初始化完成");
    }

    /**
     * 解析并验证Token，返回用户上下文
     *
     * @param token JWT Token（不含Bearer前缀）
     * @return 用户上下文信息
     * @throws JwtException 如果Token无效或已过期
     */
    @SuppressWarnings("unchecked")
    public UserContext parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 检查Token类型
            String tokenType = claims.get("tokenType", String.class);
            if (!"access".equals(tokenType)) {
                throw new JwtException("无效的Token类型: " + tokenType);
            }

            // 构建用户上下文
            return UserContext.builder()
                    .userId(claims.get("userId", String.class))
                    .username(claims.get("username", String.class))
                    .tenantId(claims.get("tenantId", String.class))
                    .roles((List<String>) claims.get("roles"))
                    .valid(true)
                    .expiresAt(claims.getExpiration() != null ? claims.getExpiration().toString() : null)
                    .build();

        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
}
