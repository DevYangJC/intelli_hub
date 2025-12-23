package com.intellihub.auth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.intellihub.auth"})
@EnableDiscoveryClient
@EnableDubbo
@MapperScan("com.intellihub.auth.mapper")
public class IntelliAuthIamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliAuthIamServiceApplication.class, args);
    }

}
