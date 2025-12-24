package com.intellihub.kafka.constant;

/**
 * Kafka主题常量
 *
 * @author intellihub
 * @since 1.0.0
 */
public interface KafkaTopics {

    /**
     * 调用日志主题
     */
    String CALL_LOG = "intellihub-call-log";

    /**
     * 告警主题
     */
    String ALERT = "intellihub-alert";

    /**
     * 事件主题
     */
    String EVENT = "intellihub-event";

    /**
     * 审计日志主题
     */
    String AUDIT_LOG = "intellihub-audit-log";
}
