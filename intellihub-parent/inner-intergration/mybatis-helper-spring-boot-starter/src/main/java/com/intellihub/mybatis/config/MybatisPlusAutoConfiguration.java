package com.intellihub.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.intellihub.mybatis.handler.AutoFillMetaObjectHandler;
import com.intellihub.mybatis.handler.IntelliHubTenantLineHandler;
import com.intellihub.mybatis.properties.TenantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 自动配置
 *
 * @author IntelliHub
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class MybatisPlusAutoConfiguration {

    /**
     * MyBatis-Plus 拦截器
     * 包含多租户拦截器和分页拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantProperties tenantProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 多租户拦截器（必须在分页拦截器之前）
        if (tenantProperties.isEnabled()) {
            log.info("启用多租户拦截器 - 租户字段: {}, 忽略表数量: {}", 
                    tenantProperties.getColumn(), 
                    tenantProperties.getIgnoreTables().size());
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(new IntelliHubTenantLineHandler(tenantProperties));
            interceptor.addInnerInterceptor(tenantInterceptor);
        } else {
            log.info("多租户拦截器已禁用");
        }
        
        // 2. 分页拦截器 - 默认MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler metaObjectHandler() {
        return new AutoFillMetaObjectHandler();
    }
}
