package com.intellihub.governance.notify.impl;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.notify.AbstractNotifyChannel;
import com.intellihub.kafka.constant.KafkaTopics;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka通知渠道
 * 将告警消息发送到Kafka的告警Topic
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotifyChannel extends AbstractNotifyChannel {

    private final KafkaMessageProducer kafkaMessageProducer;

    @Override
    public String getChannelType() {
        return "kafka";
    }

    @Override
    public boolean send(AlertRecord record, String target) {
        try {
            Map<String, Object> alertMessage = buildAlertPayload(record);
            kafkaMessageProducer.send(KafkaTopics.ALERT, record.getTenantId(), alertMessage);
            log.info("[Kafka] 告警通知发送成功 - topic: {}, recordId: {}", KafkaTopics.ALERT, record.getId());
            return true;
        } catch (Exception e) {
            log.error("[Kafka] 告警通知发送失败 - error: {}", e.getMessage());
            return false;
        }
    }
}
