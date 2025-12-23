package com.intellihub.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 公开接口
                .antMatchers(
                        // Swagger相关
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/doc.html",
                        // 认证相关接口
                        "/iam/v1/auth/**",
                        // 管理接口（网关已做认证，这里放开）
                        "/iam/v1/tenants/**",
                        "/iam/v1/users/**",
                        "/iam/v1/roles/**",
                        "/iam/v1/permissions/**",
                        "/iam/v1/menus/**",
                        "/actuator/**",
                        "/error"
                ).permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
                .and()
                .httpBasic().disable()
                .formLogin().disable();

        return http.build();
    }
}
