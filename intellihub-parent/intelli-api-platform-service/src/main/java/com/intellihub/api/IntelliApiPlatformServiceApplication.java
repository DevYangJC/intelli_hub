package com.intellihub.api;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.intellihub"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableDubbo
@EnableTransactionManagement
@MapperScan("com.intellihub.api.mapper")
public class IntelliApiPlatformServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliApiPlatformServiceApplication.class, args);
    }
}
