package com.intellihub.event.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 异步任务配置类
 * 启用 Spring 的异步支持和定时任务支持
 * @EnableAsync - 支持 @Async 注解，实现异步方法调用
 * @EnableScheduling - 支持 @Scheduled 注解，实现定时任务
 *
 * @author IntelliHub
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
