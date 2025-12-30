package com.intellihub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.intellihub"})
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.intellihub.event.mapper")
public class IntelliEventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliEventServiceApplication.class, args);
    }
}
