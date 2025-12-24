package com.intellihub.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Kafka消息生产者
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送消息（异步）
     *
     * @param topic   主题
     * @param message 消息对象
     */
    public void send(String topic, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json);
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.debug("Kafka发送消息成功 - topic: {}, offset: {}", topic, result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Kafka发送消息失败 - topic: {}, error: {}", topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Kafka消息序列化失败 - topic: {}", topic, e);
        }
    }

    /**
     * 发送消息（带Key）
     *
     * @param topic   主题
     * @param key     消息Key
     * @param message 消息对象
     */
    public void send(String topic, String key, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.debug("Kafka发送消息成功 - topic: {}, key: {}, offset: {}", topic, key, result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("Kafka发送消息失败 - topic: {}, key: {}, error: {}",
                            topic, key, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Kafka消息序列化失败 - topic: {}, key: {}", topic, key, e);
        }
    }

    /**
     * 发送消息（同步，返回ListenableFuture）
     *
     * @param topic   主题
     * @param message 消息对象
     * @return ListenableFuture
     */
    public ListenableFuture<SendResult<String, String>> sendAsync(String topic, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            return kafkaTemplate.send(topic, json);
        } catch (Exception e) {
            log.error("Kafka消息序列化失败 - topic: {}", topic, e);
            throw new RuntimeException("Kafka消息序列化失败", e);
        }
    }

    /**
     * 发送原始字符串消息
     *
     * @param topic   主题
     * @param message 消息字符串
     */
    public void sendRaw(String topic, String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.debug("Kafka发送消息成功 - topic: {}, offset: {}", topic, result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Kafka发送消息失败 - topic: {}, error: {}", topic, ex.getMessage());
            }
        });
    }
}
