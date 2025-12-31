package com.intellihub.auth.service;

import com.intellihub.kafka.constant.KafkaTopics;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户事件发布器
 * <p>
 * 将用户生命周期事件发布到事件中心（Kafka）
 * 用于事件驱动架构，实现用户管理的解耦
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private static final String EVENT_SOURCE = "auth-iam-service";
    
    private final KafkaMessageProducer kafkaMessageProducer;

    public void publishUserCreated(String userId, String username, String nickname, 
                                   String email, String phone, List<String> roleIds, String tenantId) {
        publishEvent(tenantId, "user.created", buildEventData()
                .put("userId", userId)
                .put("username", username)
                .put("nickname", nickname)
                .put("email", email)
                .put("phone", phone)
                .put("roleIds", roleIds)
                .put("createdAt", LocalDateTime.now().toString())
                .build());
        log.info("发布用户创建事件 - userId: {}, username: {}", userId, username);
    }

    public void publishUserUpdated(String userId, String username, String nickname, 
                                   String email, String phone, String tenantId) {
        publishEvent(tenantId, "user.updated", buildEventData()
                .put("userId", userId)
                .put("username", username)
                .put("nickname", nickname)
                .put("email", email)
                .put("phone", phone)
                .put("updatedAt", LocalDateTime.now().toString())
                .build());
        log.info("发布用户更新事件 - userId: {}, username: {}", userId, username);
    }

    public void publishUserDeleted(String userId, String username, String tenantId) {
        publishEvent(tenantId, "user.deleted", buildEventData()
                .put("userId", userId)
                .put("username", username)
                .put("deletedAt", LocalDateTime.now().toString())
                .build());
        log.info("发布用户删除事件 - userId: {}, username: {}", userId, username);
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
