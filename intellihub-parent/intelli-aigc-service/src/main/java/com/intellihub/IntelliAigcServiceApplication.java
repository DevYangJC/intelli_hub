package com.intellihub;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
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
@EnableDubbo
@EnableAsync
@EnableScheduling
@MapperScan("com.intellihub.aigc.mapper")
public class IntelliAigcServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliAigcServiceApplication.class, args);
    }
}
