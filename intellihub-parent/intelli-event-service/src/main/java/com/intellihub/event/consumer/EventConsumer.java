package com.intellihub.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.event.constant.ConsumeStatus;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.filter.EventFilterService;
import com.intellihub.event.handler.HandleResult;
import com.intellihub.event.handler.SubscriptionHandler;
import com.intellihub.event.handler.SubscriptionHandlerFactory;
import com.intellihub.event.mapper.EventConsumeRecordMapper;
import com.intellihub.event.model.EventMessage;
import com.intellihub.event.service.EventSubscriptionService;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.intellihub.kafka.constant.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.intellihub.context.UserContextHolder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件消费者
 * 监听 Kafka 消息队列中的事件，并根据订阅配置分发给各个订阅者
 * 支持多种订阅类型：
 * 1. Webhook - HTTP 回调，支持 POST/PUT 方法
 * 2. MQ - 转发到其他消息队列
 * 3. Service - 内部服务调用（预留）
 * 
 * 消费过程会记录详细的消费日志，包括成功/失败、耗时、响应等
 * 支持失败重试机制，可配置重试策略和最大重试次数
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventSubscriptionService subscriptionService;
    private final EventConsumeRecordMapper consumeRecordMapper;
    private final SubscriptionHandlerFactory handlerFactory;
    private final EventFilterService filterService;
    private final ObjectMapper objectMapper;

    /**
     * 处理事件消息
     * 使用 @KafkaListener 注解监听 Kafka 消息
     * 执行流程：
     * 1. 查询事件的所有激活订阅
     * 2. 按优先级顺序处理每个订阅
     * 3. 记录消费日志
     *
     * @param record Kafka 消费记录
     */
    @KafkaListener(topics = KafkaTopics.EVENT, groupId = "event-service-group")
    public void handleEvent(ConsumerRecord<String, String> record) {
        try {
            String eventJson = record.value();
            EventMessage eventMessage = objectMapper.readValue(eventJson, EventMessage.class);
            log.info("收到事件: eventId={}, eventCode={}", eventMessage.getEventId(), eventMessage.getEventCode());

            // 从消息中设置租户上下文（异步消息处理场景）
            UserContextHolder.setCurrentTenantId(eventMessage.getTenantId());
            
            List<EventSubscription> subscriptions = subscriptionService.getSubscriptionsByEvent(
                    eventMessage.getEventCode());

            if (subscriptions.isEmpty()) {
                log.warn("事件无订阅者: eventCode={}", eventMessage.getEventCode());
                return;
            }

            for (EventSubscription subscription : subscriptions) {
                // 过滤表达式检查
                if (!shouldProcess(eventMessage, subscription)) {
                    log.debug("事件被过滤: eventCode={}, subscriberName={}, filterExpression={}",
                            eventMessage.getEventCode(), subscription.getSubscriberName(), 
                            subscription.getFilterExpression());
                    continue;
                }
                processSubscription(eventMessage, subscription);
            }
        } catch (Exception e) {
            log.error("处理事件消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查事件是否应该被处理
     * <p>
     * 使用订阅配置中的过滤表达式（SpEL）评估事件是否满足条件。
     * 如果表达式为空或评估结果为 true，则事件应该被处理。
     * </p>
     *
     * @param eventMessage 事件消息
     * @param subscription 订阅配置
     * @return true-应该处理，false-跳过
     */
    private boolean shouldProcess(EventMessage eventMessage, EventSubscription subscription) {
        String filterExpression = subscription.getFilterExpression();
        
        // 无过滤表达式，直接处理
        if (!StringUtils.hasText(filterExpression)) {
            return true;
        }
        
        // 使用过滤服务评估表达式
        return filterService.evaluate(eventMessage, filterExpression);
    }

    /**
     * 处理单个订阅
     * 使用策略模式根据订阅类型调用对应的处理器
     * 所有处理结果都会记录到消费记录表
     *
     * @param eventMessage 事件消息
     * @param subscription 订阅配置
     */
    private void processSubscription(EventMessage eventMessage, EventSubscription subscription) {
        EventConsumeRecord record = new EventConsumeRecord();
        record.setTenantId(eventMessage.getTenantId());
        record.setSubscriptionId(subscription.getId());
        record.setEventId(eventMessage.getEventId());
        record.setEventCode(eventMessage.getEventCode());
        record.setConsumeTime(LocalDateTime.now());
        record.setRetryTimes(0);

        try {
            // 存储完整的 EventMessage 对象，便于重试时反序列化
            record.setEventData(objectMapper.writeValueAsString(eventMessage));

            // 使用策略模式获取处理器
            SubscriptionHandler handler = handlerFactory.getHandler(subscription.getSubscriberType());
            if (handler == null) {
                log.warn("不支持的订阅者类型: {}", subscription.getSubscriberType());
                record.setStatus(ConsumeStatus.FAILED.getCode());
                record.setErrorMessage("不支持的订阅者类型: " + subscription.getSubscriberType());
            } else {
                // 执行处理
                HandleResult result = handler.handle(eventMessage, subscription);
                applyResult(record, result, subscription);
            }
        } catch (Exception e) {
            log.error("处理订阅失败: subscriptionId={}", subscription.getId(), e);
            record.setStatus(ConsumeStatus.FAILED.getCode());
            record.setErrorMessage(e.getMessage());
        } finally {
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            consumeRecordMapper.insert(record);
        }
    }

    /**
     * 应用处理结果到消费记录
     */
    private void applyResult(EventConsumeRecord record, HandleResult result, EventSubscription subscription) {
        record.setCostTime(result.getCostTime());
        record.setResponseCode(result.getResponseCode());
        record.setResponseBody(result.getResponseBody());

        if (result.isSuccess()) {
            record.setStatus(ConsumeStatus.SUCCESS.getCode());
        } else {
            record.setErrorMessage(result.getErrorMessage());
            if (result.isNeedRetry() && record.getRetryTimes() < subscription.getMaxRetryTimes()) {
                record.setStatus(ConsumeStatus.RETRYING.getCode());
                record.setNextRetryTime(calculateNextRetryTime(subscription, record.getRetryTimes()));
            } else {
                record.setStatus(ConsumeStatus.FAILED.getCode());
            }
        }
    }

    /**
     * 计算下次重试时间
     * 根据重试策略计算：
     * - FIXED: 固定间隔 60 秒
     * - EXPONENTIAL: 指数退避，2^n * 60 秒
     */
    private LocalDateTime calculateNextRetryTime(EventSubscription subscription, int retryTimes) {
        int delaySeconds = 60;
        if ("EXPONENTIAL".equals(subscription.getRetryStrategy())) {
            delaySeconds = (int) Math.pow(2, retryTimes) * 60;
        }
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
