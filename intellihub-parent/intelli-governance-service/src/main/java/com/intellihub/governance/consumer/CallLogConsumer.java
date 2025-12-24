package com.intellihub.governance.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.governance.dto.CallLogDTO;
import com.intellihub.governance.service.CallLogService;
import com.intellihub.kafka.constant.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 调用日志消费者
 * <p>
 * 从Kafka消费Gateway上报的调用日志，保存到数据库
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallLogConsumer {

    private final CallLogService callLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 消费调用日志
     */
    @KafkaListener(topics = KafkaTopics.CALL_LOG, groupId = "governance-call-log-group")
    public void consumeCallLog(ConsumerRecord<String, String> record) {
        try {
            String logJson = record.value();
            log.debug("收到调用日志消息 - offset: {}, partition: {}", record.offset(), record.partition());

            // 解析日志数据
            @SuppressWarnings("unchecked")
            Map<String, Object> logData = objectMapper.readValue(logJson, Map.class);

            // 转换为DTO
            CallLogDTO dto = convertToDTO(logData);

            // 保存调用日志
            callLogService.saveCallLog(dto);

            log.debug("调用日志保存成功 - path: {}", dto.getApiPath());
        } catch (Exception e) {
            log.error("消费调用日志失败: {}", record.value(), e);
        }
    }

    /**
     * 将Map转换为CallLogDTO
     */
    private CallLogDTO convertToDTO(Map<String, Object> logData) {
        CallLogDTO dto = new CallLogDTO();

        dto.setTenantId(getString(logData, "tenantId"));
        dto.setApiId(getString(logData, "apiId"));
        dto.setApiPath(getString(logData, "apiPath"));
        dto.setApiMethod(getString(logData, "apiMethod"));
        dto.setAppId(getString(logData, "appId"));
        dto.setAppKey(getString(logData, "appKey"));
        dto.setClientIp(getString(logData, "clientIp"));
        dto.setStatusCode(getInteger(logData, "statusCode"));
        dto.setSuccess(getBoolean(logData, "success"));
        dto.setLatency(getInteger(logData, "latency"));
        dto.setErrorMessage(getString(logData, "errorMessage"));
        dto.setUserAgent(getString(logData, "userAgent"));

        // 解析请求时间
        String requestTimeStr = getString(logData, "requestTime");
        if (requestTimeStr != null) {
            try {
                dto.setRequestTime(LocalDateTime.parse(requestTimeStr));
            } catch (Exception e) {
                dto.setRequestTime(LocalDateTime.now());
            }
        } else {
            dto.setRequestTime(LocalDateTime.now());
        }

        return dto;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
