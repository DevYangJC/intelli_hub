package com.intellihub.governance.notify;

import com.intellihub.governance.entity.AlertRecord;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知渠道抽象基类
 * 提供通用的工具方法
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractNotifyChannel implements NotifyChannel {

    protected static final DateTimeFormatter DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 构建告警消息体
     */
    protected Map<String, Object> buildAlertPayload(AlertRecord record) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", record.getId());
        payload.put("tenantId", record.getTenantId());
        payload.put("ruleId", record.getRuleId());
        payload.put("ruleName", record.getRuleName());
        payload.put("apiId", record.getApiId());
        payload.put("apiPath", record.getApiPath());
        payload.put("alertLevel", record.getAlertLevel());
        payload.put("alertMessage", record.getAlertMessage());
        payload.put("currentValue", record.getCurrentValue());
        payload.put("thresholdValue", record.getThresholdValue());
        payload.put("status", record.getStatus());
        payload.put("firedAt", record.getFiredAt() != null 
                ? record.getFiredAt().format(DATETIME_FORMATTER) : null);
        return payload;
    }

    /**
     * 获取告警级别对应的Emoji
     */
    protected String getAlertLevelEmoji(String level) {
        if (level == null) return "⚠️";
        switch (level.toLowerCase()) {
            case "critical":
                return "🔴";
            case "warning":
                return "🟡";
            case "info":
                return "🔵";
            default:
                return "⚠️";
        }
    }

    /**
     * 获取告警级别中文描述
     */
    protected String getAlertLevelDesc(String level) {
        if (level == null) return "未知";
        switch (level.toLowerCase()) {
            case "critical":
                return "严重";
            case "warning":
                return "警告";
            case "info":
                return "信息";
            default:
                return level;
        }
    }
}
