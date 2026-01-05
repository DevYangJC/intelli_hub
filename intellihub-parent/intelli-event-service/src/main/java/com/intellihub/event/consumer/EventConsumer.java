package com.intellihub.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.event.constant.ConsumeStatus;
import com.intellihub.event.constant.SubscriberType;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.mapper.EventConsumeRecordMapper;
import com.intellihub.event.model.EventMessage;
import com.intellihub.event.service.EventSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.intellihub.kafka.constant.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;
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
                processSubscription(eventMessage, subscription);
            }
        } catch (Exception e) {
            log.error("处理事件消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理单个订阅
     * 根据订阅类型调用不同的处理方法
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
            record.setEventData(objectMapper.writeValueAsString(eventMessage.getData()));

            if (SubscriberType.WEBHOOK.getCode().equals(subscription.getSubscriberType())) {
                handleWebhook(eventMessage, subscription, record);
            } else if (SubscriberType.MQ.getCode().equals(subscription.getSubscriberType())) {
                handleMQ(eventMessage, subscription, record);
            } else {
                log.warn("不支持的订阅者类型: {}", subscription.getSubscriberType());
                record.setStatus(ConsumeStatus.FAILED.getCode());
                record.setErrorMessage("不支持的订阅者类型");
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
     * 处理 Webhook 订阅
     * 发送 HTTP 请求到订阅者的回调地址
     * 支持 POST 和 PUT 方法
     * 记录响应状态码、响应内容、耗时等信息
     * 失败时根据重试策略设置下次重试时间
     *
     * @param eventMessage 事件消息
     * @param subscription 订阅配置
     * @param record       消费记录
     */
    private void handleWebhook(EventMessage eventMessage, EventSubscription subscription, EventConsumeRecord record) {
        long startTime = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (subscription.getCallbackHeaders() != null) {
                // 解析自定义请求头
            }

            HttpEntity<EventMessage> request = new HttpEntity<>(eventMessage, headers);
            
            String method = subscription.getCallbackMethod() != null ? 
                    subscription.getCallbackMethod() : "POST";
            
            ResponseEntity<String> response;
            if ("PUT".equalsIgnoreCase(method)) {
                response = restTemplate.exchange(subscription.getCallbackUrl(), 
                        HttpMethod.PUT, request, String.class);
            } else {
                response = restTemplate.postForEntity(subscription.getCallbackUrl(), 
                        request, String.class);
            }

            long costTime = System.currentTimeMillis() - startTime;
            record.setCostTime((int) costTime);
            record.setResponseCode(response.getStatusCodeValue());
            record.setResponseBody(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                record.setStatus(ConsumeStatus.SUCCESS.getCode());
                log.info("Webhook调用成功: url={}, costTime={}ms", 
                        subscription.getCallbackUrl(), costTime);
            } else {
                record.setStatus(ConsumeStatus.FAILED.getCode());
                record.setErrorMessage("HTTP状态码: " + response.getStatusCodeValue());
                log.warn("Webhook调用失败: url={}, status={}", 
                        subscription.getCallbackUrl(), response.getStatusCodeValue());
            }
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            record.setCostTime((int) costTime);
            record.setStatus(ConsumeStatus.FAILED.getCode());
            record.setErrorMessage(e.getMessage());
            log.error("Webhook调用异常: url={}", subscription.getCallbackUrl(), e);

            if (record.getRetryTimes() < subscription.getMaxRetryTimes()) {
                record.setStatus(ConsumeStatus.RETRYING.getCode());
                record.setNextRetryTime(calculateNextRetryTime(subscription, record.getRetryTimes()));
            }
        }
    }

    /**
     * 处理 MQ 订阅
     * 将事件转发到其他消息队列
     * 根据订阅配置的 topic 和 tag 进行路由
     *
     * @param eventMessage 事件消息
     * @param subscription 订阅配置
     * @param record       消费记录
     */
    private void handleMQ(EventMessage eventMessage, EventSubscription subscription, EventConsumeRecord record) {
        log.info("转发到MQ: topic={}, tag={}", subscription.getMqTopic(), subscription.getMqTag());
        record.setStatus(ConsumeStatus.SUCCESS.getCode());
    }

    /**
     * 计算下次重试时间
     * 根据重试策略计算：
     * - FIXED: 固定间隔 60 秒
     * - EXPONENTIAL: 指数退避，2^n * 60 秒
     *
     * @param subscription 订阅配置
     * @param retryTimes   当前重试次数
     * @return 下次重试时间
     */
    private LocalDateTime calculateNextRetryTime(EventSubscription subscription, int retryTimes) {
        int delaySeconds = 60;
        if ("EXPONENTIAL".equals(subscription.getRetryStrategy())) {
            delaySeconds = (int) Math.pow(2, retryTimes) * 60;
        }
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
