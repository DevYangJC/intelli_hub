package com.intellihub.event.handler.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellihub.event.constant.SubscriberType;
import com.intellihub.event.entity.EventConsumeRecord;
import com.intellihub.event.entity.EventSubscription;
import com.intellihub.event.handler.HandleResult;
import com.intellihub.event.handler.SubscriptionHandler;
import com.intellihub.event.handler.http.HttpRequestStrategy;
import com.intellihub.event.handler.http.HttpRequestStrategyFactory;
import com.intellihub.event.model.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Webhook 订阅处理器
 * 通过 HTTP 回调通知订阅者
 *
 * @author IntelliHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookHandler implements SubscriptionHandler {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HttpRequestStrategyFactory httpRequestStrategyFactory;

    @Override
    public String getType() {
        return SubscriberType.WEBHOOK.getCode();
    }

    @Override
    public HandleResult handle(EventMessage eventMessage, EventSubscription subscription) {
        return doRequest(eventMessage, subscription);
    }

    @Override
    public HandleResult retry(EventConsumeRecord record, EventSubscription subscription) {
        try {
            EventMessage eventMessage = objectMapper.readValue(record.getEventData(), EventMessage.class);
            return doRequest(eventMessage, subscription);
        } catch (Exception e) {
            log.error("解析事件数据失败: recordId={}", record.getId(), e);
            return HandleResult.fail("解析事件数据失败: " + e.getMessage(), 0);
        }
    }

    /**
     * 执行 HTTP 请求
     */
    private HandleResult doRequest(EventMessage eventMessage, EventSubscription subscription) {
        long startTime = System.currentTimeMillis();
        try {
            // 构建请求头
            HttpHeaders headers = buildHeaders(subscription);
            HttpEntity<EventMessage> request = new HttpEntity<>(eventMessage, headers);

            // 获取 HTTP 请求策略
            HttpRequestStrategy strategy = httpRequestStrategyFactory.getStrategy(subscription.getCallbackMethod());

            // 执行请求
            ResponseEntity<String> response = strategy.execute(restTemplate, subscription.getCallbackUrl(), request);

            int costTime = (int) (System.currentTimeMillis() - startTime);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook调用成功: url={}, method={}, costTime={}ms",
                        subscription.getCallbackUrl(), strategy.getMethod(), costTime);
                return HandleResult.success(response.getStatusCodeValue(), response.getBody(), costTime);
            } else {
                log.warn("Webhook调用失败: url={}, status={}",
                        subscription.getCallbackUrl(), response.getStatusCodeValue());
                return HandleResult.failWithRetry("HTTP状态码: " + response.getStatusCodeValue(), costTime);
            }
        } catch (Exception e) {
            int costTime = (int) (System.currentTimeMillis() - startTime);
            log.error("Webhook调用异常: url={}", subscription.getCallbackUrl(), e);
            return HandleResult.failWithRetry(e.getMessage(), costTime);
        }
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders(EventSubscription subscription) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 解析自定义请求头
        if (StringUtils.hasText(subscription.getCallbackHeaders())) {
            try {
                Map<String, String> customHeaders = objectMapper.readValue(
                        subscription.getCallbackHeaders(),
                        new TypeReference<Map<String, String>>() {});
                customHeaders.forEach(headers::add);
            } catch (Exception e) {
                log.warn("解析自定义请求头失败: {}", e.getMessage());
            }
        }

        return headers;
    }
}
