package com.intellihub.governance.notify.impl;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.notify.AbstractNotifyChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Webhook通知渠道
 * targets格式: https://your-webhook-url.com/alert
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookNotifyChannel extends AbstractNotifyChannel {

    private final RestTemplate restTemplate;

    @Override
    public String getChannelType() {
        return "webhook";
    }

    @Override
    public boolean send(AlertRecord record, String webhookUrl) {
        try {
            Map<String, Object> payload = buildAlertPayload(record);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Webhook] 告警通知发送成功 - url: {}, recordId: {}", webhookUrl, record.getId());
                return true;
            } else {
                log.warn("[Webhook] 告警通知响应异常 - url: {}, status: {}", webhookUrl, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("[Webhook] 告警通知发送失败 - url: {}, error: {}", webhookUrl, e.getMessage());
            return false;
        }
    }
}
