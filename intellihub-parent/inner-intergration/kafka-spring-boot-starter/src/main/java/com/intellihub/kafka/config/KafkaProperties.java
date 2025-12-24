package com.intellihub.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka配置属性
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "intellihub.kafka")
public class KafkaProperties {

    /**
     * 是否启用Kafka
     */
    private boolean enabled = true;

    /**
     * 调用日志Topic
     */
    private String callLogTopic = "intellihub-call-log";

    /**
     * 告警Topic
     */
    private String alertTopic = "intellihub-alert";

    /**
     * 事件Topic
     */
    private String eventTopic = "intellihub-event";
}
