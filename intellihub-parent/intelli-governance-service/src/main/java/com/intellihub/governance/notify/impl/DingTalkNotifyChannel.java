package com.intellihub.governance.notify.impl;

import com.intellihub.governance.entity.AlertRecord;
import com.intellihub.governance.notify.AbstractNotifyChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉机器人通知渠道
 * targets格式: https://oapi.dingtalk.com/robot/send?access_token=xxx
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkNotifyChannel extends AbstractNotifyChannel {

    private final RestTemplate restTemplate;

    @Override
    public String getChannelType() {
        return "dingtalk";
    }

    @Override
    public boolean send(AlertRecord record, String robotUrl) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "markdown");
            
            Map<String, String> markdown = new HashMap<>();
            markdown.put("title", getAlertLevelEmoji(record.getAlertLevel()) + " " + record.getRuleName());
            markdown.put("text", buildDingTalkContent(record));
            message.put("markdown", markdown);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(robotUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[DingTalk] 告警通知发送成功 - recordId: {}", record.getId());
                return true;
            } else {
                log.warn("[DingTalk] 告警通知响应异常 - status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("[DingTalk] 告警通知发送失败 - error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建钉钉Markdown内容
     */
    private String buildDingTalkContent(AlertRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(getAlertLevelEmoji(record.getAlertLevel()))
          .append(" ").append(record.getRuleName()).append("\n\n");
        sb.append("> **告警级别**: ").append(getAlertLevelDesc(record.getAlertLevel())).append("\n\n");
        sb.append("> **API路径**: ").append(record.getApiPath() != null ? record.getApiPath() : "全局").append("\n\n");
        sb.append("> **当前值**: ").append(record.getCurrentValue()).append("\n\n");
        sb.append("> **阈值**: ").append(record.getThresholdValue()).append("\n\n");
        sb.append("> **告警内容**: ").append(record.getAlertMessage()).append("\n\n");
        sb.append("> **触发时间**: ").append(record.getFiredAt() != null 
                ? record.getFiredAt().format(DATETIME_FORMATTER) : "-").append("\n");
        return sb.toString();
    }
}
