package com.intellihub.governance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 异步和定时任务配置
 *
 * @author intellihub
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
