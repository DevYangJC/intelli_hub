package com.intellihub.event.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.event.constant.SubscriberType;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.handler.HandleResult;
import com.intellihub.event.handler.SubscriptionHandler;
import com.intellihub.event.model.EventMessage;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * MQ 订阅处理器
 * 将事件转发到其他 Kafka Topic
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MQHandler implements SubscriptionHandler {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final ObjectMapper objectMapper;

    @Override
    public String getType() {
        return SubscriberType.MQ.getCode();
    }

    @Override
    public HandleResult handle(EventMessage eventMessage, EventSubscription subscription) {
        return doForward(eventMessage, subscription);
    }

    @Override
    public HandleResult retry(EventConsumeRecord record, EventSubscription subscription) {
        try {
            EventMessage eventMessage = objectMapper.readValue(record.getEventData(), EventMessage.class);
            return doForward(eventMessage, subscription);
        } catch (Exception e) {
            log.error("解析事件数据失败: recordId={}", record.getId(), e);
            return HandleResult.fail("解析事件数据失败: " + e.getMessage(), 0);
        }
    }

    /**
     * 转发消息到目标 Topic
     */
    private HandleResult doForward(EventMessage eventMessage, EventSubscription subscription) {
        long startTime = System.currentTimeMillis();
        try {
            String topic = subscription.getMqTopic();
            if (!StringUtils.hasText(topic)) {
                return HandleResult.fail("MQ Topic未配置", 0);
            }

            // 使用租户ID作为key，保证同一租户的消息有序
            String key = eventMessage.getTenantId();
            
            // 如果配置了tag，添加到消息header或作为key的一部分
            if (StringUtils.hasText(subscription.getMqTag())) {
                key = key + ":" + subscription.getMqTag();
            }

            kafkaMessageProducer.send(topic, key, eventMessage);

            int costTime = (int) (System.currentTimeMillis() - startTime);
            log.info("MQ转发成功: topic={}, tag={}, costTime={}ms",
                    topic, subscription.getMqTag(), costTime);
            
            return HandleResult.success(null, null, costTime);
        } catch (Exception e) {
            int costTime = (int) (System.currentTimeMillis() - startTime);
            log.error("MQ转发失败: topic={}", subscription.getMqTopic(), e);
            return HandleResult.failWithRetry(e.getMessage(), costTime);
        }
    }
}
