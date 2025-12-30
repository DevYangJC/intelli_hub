package com.intellihub.api.service;

import com.intellihub.kafka.constant.KafkaTopics;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API事件发布器
 * <p>
 * 将API生命周期事件发布到事件中心（Kafka）
 * 用于事件驱动架构，实现服务间解耦
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiEventPublisher {

    private static final String EVENT_SOURCE = "api-platform-service";
    
    private final KafkaMessageProducer kafkaMessageProducer;

    public void publishApiPublished(String apiId, String apiName, String path, String method, String tenantId) {
        publishEvent(tenantId, "api.published", buildEventData()
                .put("apiId", apiId)
                .put("apiName", apiName)
                .put("apiPath", path)
                .put("method", method)
                .put("publishedAt", LocalDateTime.now().toString())
                .build());
        log.info("发布API发布事件 - apiId: {}, path: {}", apiId, path);
    }

    public void publishApiUpdated(String apiId, String path, String method, String tenantId) {
        publishEvent(tenantId, "api.updated", buildEventData()
                .put("apiId", apiId)
                .put("apiPath", path)
                .put("method", method)
                .put("updatedAt", LocalDateTime.now().toString())
                .build());
        log.info("发布API更新事件 - apiId: {}, path: {}", apiId, path);
    }

    public void publishApiOffline(String apiId, String path, String method, String tenantId) {
        publishEvent(tenantId, "api.offline", buildEventData()
                .put("apiId", apiId)
                .put("apiPath", path)
                .put("method", method)
                .put("offlineAt", LocalDateTime.now().toString())
                .build());
        log.info("发布API下线事件 - apiId: {}, path: {}", apiId, path);
    }

    public void publishApiDeleted(String apiId, String path, String method, String tenantId) {
        publishEvent(tenantId, "api.deleted", buildEventData()
                .put("apiId", apiId)
                .put("apiPath", path)
                .put("method", method)
                .put("deletedAt", LocalDateTime.now().toString())
                .build());
        log.info("发布API删除事件 - apiId: {}, path: {}", apiId, path);
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
