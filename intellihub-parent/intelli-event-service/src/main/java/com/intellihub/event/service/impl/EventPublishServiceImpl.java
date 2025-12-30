package com.intellihub.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.event.constant.EventStatus;
import com.intellihub.event.entity.EventDefinition;
import com.intellihub.event.entity.EventPublishRecord;
import com.intellihub.event.mapper.EventDefinitionMapper;
import com.intellihub.event.mapper.EventPublishRecordMapper;
import com.intellihub.event.model.EventMessage;
import com.intellihub.event.service.EventPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.intellihub.kafka.constant.KafkaTopics;
import com.intellihub.kafka.producer.KafkaMessageProducer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 事件发布服务实现类
 * 实现事件发布的核心逻辑，包括：
 * 1. 校验事件定义是否存在且已激活
 * 2. 将事件发布到 Kafka 消息队列
 * 3. 记录事件发布日志到数据库
 * 4. 支持同步和异步两种发布方式
 *
 * @author IntelliHub
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublishServiceImpl implements EventPublishService {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final EventDefinitionMapper eventDefinitionMapper;
    private final EventPublishRecordMapper eventPublishRecordMapper;
    private final ObjectMapper objectMapper;


    /**
     * 同步发布事件
     * 执行流程：
     * 1. 查询事件定义，校验事件是否存在且已激活
     * 2. 发布消息到 Kafka，使用 tenantId 作为 key 保证同一租户的事件有序
     * 3. 记录发布日志（成功或失败）
     *
     * @param eventMessage 事件消息
     * @return true-发布成功，false-发布失败
     */
    @Override
    public boolean publish(EventMessage eventMessage) {
        try {
            EventDefinition definition = eventDefinitionMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EventDefinition>()
                            .eq(EventDefinition::getTenantId, eventMessage.getTenantId())
                            .eq(EventDefinition::getEventCode, eventMessage.getEventCode())
                            .eq(EventDefinition::getStatus, EventStatus.ACTIVE.getCode())
            );

            if (definition == null) {
                log.warn("事件定义不存在或未激活: tenantId={}, eventCode={}", 
                        eventMessage.getTenantId(), eventMessage.getEventCode());
                return false;
            }

            // 发布到 Kafka，使用 tenantId 作为 key 保证同一租户的事件有序
            kafkaMessageProducer.send(KafkaTopics.EVENT, eventMessage.getTenantId(), eventMessage);

            savePublishRecord(eventMessage, "PUBLISHED", null);
            log.info("事件发布成功: eventId={}, eventCode={}", 
                    eventMessage.getEventId(), eventMessage.getEventCode());
            return true;
        } catch (Exception e) {
            log.error("事件发布失败: eventId={}, eventCode={}", 
                    eventMessage.getEventId(), eventMessage.getEventCode(), e);
            savePublishRecord(eventMessage, "FAILED", e.getMessage());
            return false;
        }
    }

    /**
     * 异步发布事件
     * 使用 Spring 的 @Async 注解实现异步执行
     * 内部调用同步发布方法
     *
     * @param eventMessage 事件消息
     */
    @Async
    @Override
    public void publishAsync(EventMessage eventMessage) {
        publish(eventMessage);
    }

    /**
     * 保存事件发布记录
     * 记录事件发布的详细信息，包括事件数据、发布状态、错误信息等
     * 用于追踪事件的发布历史和排查问题
     *
     * @param eventMessage 事件消息
     * @param status       发布状态（PUBLISHED/FAILED）
     * @param errorMessage 错误信息（发布失败时记录）
     */
    private void savePublishRecord(EventMessage eventMessage, String status, String errorMessage) {
        try {
            EventPublishRecord record = new EventPublishRecord();
            record.setTenantId(eventMessage.getTenantId());
            record.setEventCode(eventMessage.getEventCode());
            record.setEventId(eventMessage.getEventId());
            record.setEventData(objectMapper.writeValueAsString(eventMessage.getData()));
            record.setSource(eventMessage.getSource());
            record.setPublishTime(eventMessage.getTimestamp());
            record.setStatus(status);
            record.setErrorMessage(errorMessage);
            record.setCreatedAt(LocalDateTime.now());
            eventPublishRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("保存事件发布记录失败", e);
        }
    }
}
