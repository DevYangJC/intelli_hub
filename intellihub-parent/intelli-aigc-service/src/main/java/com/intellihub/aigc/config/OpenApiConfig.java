package com.intellihub.aigc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger/OpenAPI配置
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8084").description("本地环境"),
                        new Server().url("https://api.intellihub.com").description("生产环境")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("IntelliHub AIGC服务 API文档")
                .description("集成国内主流AI大模型的AIGC服务，支持文本生成、智能对话、流式响应等功能")
                .version("1.0.0")
                .contact(new Contact()
                        .name("IntelliHub Team")
                        .email("support@intellihub.com")
                        .url("https://www.intellihub.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }
}
