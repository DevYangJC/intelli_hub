package com.intellihub.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证配置属性
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "intellihub.auth")
public class AuthProperties {

    private JwtProperties jwt = new JwtProperties();
    private CaptchaProperties captcha = new CaptchaProperties();
    private RegisterProperties register = new RegisterProperties();

    @Data
    public static class JwtProperties {
        /**
         * JWT密钥
         */
        private String secret = "intellihub-iam-jwt-secret-key-2024";

        /**
         * 访问令牌过期时间（秒）
         */
        private Long accessTokenExpiration = 7200L;

        /**
         * 刷新令牌过期时间（秒）
         */
        private Long refreshTokenExpiration = 604800L;
    }

    @Data
    public static class CaptchaProperties {
        /**
         * 是否启用验证码
         */
        private Boolean enabled = true;

        private ImageCaptcha image = new ImageCaptcha();

        @Data
        public static class ImageCaptcha {
            /**
             * 验证码长度
             */
            private Integer length = 4;

            /**
             * 过期时间（秒）
             */
            private Integer expiration = 300;

            /**
             * 宽度
             */
            private Integer width = 120;

            /**
             * 高度
             */
            private Integer height = 40;
        }
    }

    @Data
    public static class RegisterProperties {
        /**
         * 默认租户ID（新用户注册时使用）
         */
        private String defaultTenantId = "2";

        /**
         * 默认角色ID（新用户注册时分配）
         */
        private String defaultRoleId = "5";
    }
}
