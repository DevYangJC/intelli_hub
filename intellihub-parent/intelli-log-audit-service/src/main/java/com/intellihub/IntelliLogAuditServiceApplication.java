package com.intellihub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.vibe.intellihub", "com.intellihub.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableTransactionManagement
public class IntelliLogAuditServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliLogAuditServiceApplication.class, args);
    }
}
