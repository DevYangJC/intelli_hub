package com.intellihub.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.intellihub"})
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@MapperScan("com.intellihub.app.mapper")
public class IntelliAppCenterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliAppCenterServiceApplication.class, args);
    }
}
