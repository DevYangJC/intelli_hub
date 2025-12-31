package com.intellihub.governance.service;

import com.intellihub.kafka.constant.KafkaTopics;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警事件发布器
 * <p>
 * 将告警生命周期事件发布到事件中心（Kafka）
 * 用于事件驱动架构，实现告警通知的解耦
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEventPublisher {

    private static final String EVENT_SOURCE = "governance-service";
    
    private final KafkaMessageProducer kafkaMessageProducer;

    public void publishAlertTriggered(Long alertId, Long ruleId, String ruleName, String alertLevel,
                                     String metric, BigDecimal currentValue, BigDecimal threshold,
                                     String apiId, String apiPath, String tenantId, String message) {
        publishEvent(tenantId, "alert.triggered", buildEventData()
                .put("alertId", alertId)
                .put("ruleId", ruleId)
                .put("ruleName", ruleName)
                .put("alertLevel", alertLevel)
                .put("metric", metric)
                .put("currentValue", currentValue.toString())
                .put("threshold", threshold.toString())
                .put("apiId", apiId)
                .put("apiPath", apiPath)
                .put("triggeredAt", LocalDateTime.now().toString())
                .put("message", message)
                .build());
        log.info("发布告警触发事件 - alertId: {}, ruleName: {}, level: {}", alertId, ruleName, alertLevel);
    }

    public void publishAlertResolved(Long alertId, Long ruleId, String ruleName,
                                    String tenantId, long durationSeconds) {
        publishEvent(tenantId, "alert.resolved", buildEventData()
                .put("alertId", alertId)
                .put("ruleId", ruleId)
                .put("ruleName", ruleName)
                .put("resolvedAt", LocalDateTime.now().toString())
                .put("duration", durationSeconds)
                .build());
        log.info("发布告警恢复事件 - alertId: {}, ruleName: {}, duration: {}s", alertId, ruleName, durationSeconds);
    }

    private void publishEvent(String tenantId, String eventCode, Map<String, Object> eventData) {
        try {
            Map<String, Object> eventMessage = buildEventData()
                    .put("tenantId", tenantId)
                    .put("eventCode", eventCode)
                    .put("source", EVENT_SOURCE)
                    .put("data", eventData)
                    .put("timestamp", LocalDateTime.now().toString())
                    .build();
            
            kafkaMessageProducer.send(KafkaTopics.EVENT, tenantId, eventMessage);
            log.debug("事件发布成功 - eventCode: {}, tenantId: {}", eventCode, tenantId);
        } catch (Exception e) {
            log.error("事件发布失败 - eventCode: {}, tenantId: {}", eventCode, tenantId, e);
        }
    }

    private EventDataBuilder buildEventData() {
        return new EventDataBuilder();
    }

    private static class EventDataBuilder {
        private final Map<String, Object> data = new HashMap<>();

        EventDataBuilder put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        Map<String, Object> build() {
            return data;
        }
    }
}
