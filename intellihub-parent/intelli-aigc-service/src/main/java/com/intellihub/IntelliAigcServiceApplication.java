package com.intellihub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.intellihub"})
@EnableDiscoveryClient
@EnableTransactionManagement
public class IntelliAigcServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliAigcServiceApplication.class, args);
    }
}
